package com.eldercare.api.service.impl;

import com.eldercare.api.dto.AssessmentAnswerItem;
import com.eldercare.api.dto.SubmitAssessmentRequest;
import com.eldercare.api.service.AssessmentService;
import com.eldercare.api.vo.AssessmentVO;
import com.eldercare.api.vo.QuestionnaireDetailVO;
import com.eldercare.core.entity.AssessmentResult;
import com.eldercare.core.entity.Question;
import com.eldercare.core.entity.Questionnaire;
import com.eldercare.core.enums.*;
import com.eldercare.core.exception.BusinessException;
import com.eldercare.core.exception.ResourceNotFoundException;
import com.eldercare.core.mapper.AssessmentResultMapper;
import com.eldercare.core.mapper.QuestionMapper;
import com.eldercare.core.mapper.QuestionnaireMapper;
import com.eldercare.core.service.AiService;
import com.eldercare.core.service.PointService;
import com.eldercare.core.util.UserContext;
//import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 健康评测服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentServiceImpl implements AssessmentService {

    /**
     * 完成评测赠送积分
     */
    private static final int ASSESSMENT_BONUS_POINTS = 20;

    private final QuestionnaireMapper questionnaireMapper;
    private final QuestionMapper questionMapper;
    private final AssessmentResultMapper assessmentResultMapper;
    private final PointService pointService;
    private final AiService aiService;
    private final ObjectMapper objectMapper;

    // 注入自身代理
    @Lazy
    @Autowired
    private AssessmentServiceImpl self;

    @Override
    public List<Questionnaire> listQuestionnaires() {
        return questionnaireMapper.selectPublished();
    }

    /**
     * 查询问卷详情
     *
     * @param questionnaireId
     * @return
     */
    @Override
    public QuestionnaireDetailVO getQuestionnaireDetail(Long questionnaireId) {

        Questionnaire questionnaire = getPublishedQuestionnaire(questionnaireId);
        QuestionnaireDetailVO vo = new QuestionnaireDetailVO();
        vo.setId(questionnaire.getId());
        vo.setTitle(questionnaire.getTitle());
        vo.setDescription(questionnaire.getDescription());
        vo.setStatus(questionnaire.getStatus());
        vo.setQuestions(questionMapper.selectByQuestionnaireId(questionnaireId));
        return vo;
    }

    /**
     * 提交评测
     *
     * @param request
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssessmentVO submit(SubmitAssessmentRequest request) {
        Long userId = UserContext.currentUserId();
        Questionnaire questionnaire = getPublishedQuestionnaire(request.getQuestionnaireId());
        List<Question> questions = questionMapper.selectByQuestionnaireId(request.getQuestionnaireId());
        if (questions.isEmpty()) {
            throw new BusinessException("问卷暂无题目");
        }

        // 校验答案
        validateAnswers(request.getAnswers(), questions);

        // 把用户提交的答案序列化成 JSON 字符串存数据库
        Map<String, Object> answersSnapshot = new LinkedHashMap<>();
        answersSnapshot.put("version", 1);
        answersSnapshot.put("items", request.getAnswers());
        String answersJson = writeJson(answersSnapshot);

        Integer score = null;
        boolean needAI = questionnaire.getIsRated() != null && questionnaire.getIsRated() == 1;
        if (needAI) {
            score = computeRuleScore(questions, request.getAnswers());
        }


        // 保存评测结果
        AssessmentResult result = new AssessmentResult();
        result.setUserId(userId);
        result.setQuestionnaireId(questionnaire.getId());
        result.setAnswers(answersJson);
        result.setScore(score);
        if (needAI) {
            result.setStatus(AssessmentStatus.PROCESSING.getCode());
            result.setAiSuggestion(null);
        } else {
            result.setStatus(AssessmentStatus.SUCCESS.getCode());
            result.setAiSuggestion("感谢您的参与！");
        }
        assessmentResultMapper.insert(result);

        //完成评测赠送积分
        pointService.award(userId, ASSESSMENT_BONUS_POINTS, PointsSource.ASSESSMENT, result.getId());

        if (needAI) {
            self.generateSuggestionAsync(result.getId(), questionnaire.getId(), questions, request.getAnswers());
        }
        log.info("健康评测完成: userId={}, resultId={}, score={},needAI={}",
                userId, result.getId(), score, needAI);
        return toVO(result, questionnaire.getTitle());
    }

    /**
     * 评测历史
     */
    @Override
    public List<AssessmentVO> history() {
        Long userId = UserContext.currentUserId();
        List<AssessmentVO> result = assessmentResultMapper.selectHistoryByUserId(userId);

        for (AssessmentVO vo : result) {
            vo.setLevel(AssessmentLevel.getDescByScore(vo.getScore()));
        }

        return result;
    }

    /**
     * 查看之前评测详情
     *
     */
    @Override
    public AssessmentVO detail(Long assessmentId) {
        AssessmentResult result = assessmentResultMapper.selectById(assessmentId);
        if (result == null || !result.getUserId().equals(UserContext.currentUserId())) {
            throw new ResourceNotFoundException("评测记录不存在");
        }
        Questionnaire q = questionnaireMapper.selectById(result.getQuestionnaireId());
        return toVO(result, q != null ? q.getTitle() : null);
    }

    //异步生成AI建议，完成后更新结果状态
    @Async("assessmentSuggest")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void generateSuggestionAsync(Long resultId,Long questionnaireId,
                                        List<Question> questions,List<AssessmentAnswerItem> answers){
        log.info("AI 评测建议生成开始, resultId={}, thread={}",
                resultId, Thread.currentThread().getName());
        try{
            Questionnaire questionnaire = questionnaireMapper.selectById(questionnaireId);
            String suggestion = callAiSuggestion(questionnaire,questions,answers);

            assessmentResultMapper.updateSuggestion(
                    resultId,AssessmentStatus.SUCCESS.getCode(), suggestion,null);
            log.info("AI 评测建议生成完成, resultId={}", resultId);
        }catch (Exception e){
            log.error("AI 评测建议生成失败, resultId={}", resultId, e);
            assessmentResultMapper.updateSuggestion(
                    resultId, AssessmentStatus.FAILED.name(),
                    "AI建议暂时无法生成，请稍后重试", e.getMessage());
        }

    }

    /**
     * 查询问卷并校验状态
     */
    private Questionnaire getPublishedQuestionnaire(Long questionnaireId) {
        Questionnaire questionnaire = questionnaireMapper.selectById(questionnaireId);
        if (questionnaire == null ||
                !QuestionnaireStatus.PUBLISHED.getCode().equals(questionnaire.getStatus())) {
            throw new ResourceNotFoundException("问卷不存在或未发布");
        }
        return questionnaire;
    }


    private void validateAnswers(List<AssessmentAnswerItem> answers, List<Question> questions) {
        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));
        for (AssessmentAnswerItem item : answers) {
            Question question = questionMap.get(item.getQid());
            if (question == null) {
                throw new BusinessException("题目不存在: qid=" + item.getQid());
            }

            QuestionType qType = QuestionType.fromCode(question.getType());
            QuestionType iType = QuestionType.fromCode(item.getType());
            // 类型匹配校验
            if (qType != iType) {
                throw new BusinessException("题目类型不匹配: qid=" + item.getQid());
            }

            boolean typeValid = switch (qType) {
                case SINGLE -> item.getValue() instanceof Number;
                case MULTIPLE -> item.getValue() instanceof List<?> list && !list.isEmpty();
                case TEXT -> item.getValue() instanceof String s && !s.isBlank();
                default -> false;
            };
            if (!typeValid) {
                throw new BusinessException("答案格式不正确: qid=" + item.getQid());
            }
        }
    }

    /**
     * 简单评分逻辑来评分
     */
    private Integer computeRuleScore(List<Question> questions, List<AssessmentAnswerItem> answers) {
        Map<Long, AssessmentAnswerItem> answerMap = answers.stream()
                .collect(Collectors.toMap(AssessmentAnswerItem::getQid, a -> a));

        int totalEarned = 0; //用户得分
        int totalMax = 0; //问卷原始满分

        for (Question q : questions) {
            if (QuestionType.TEXT.getCode().equals(q.getType())) {
                continue;
            }
            List<QuestionOption> options = parseOptions(q.getOptions());
            if (options.isEmpty()) {
                continue;
            }
            // 计算本题的满分（多选：所有选项分值相加，单选：选项中最高分）
            int max = QuestionType.MULTIPLE.getCode().equals(q.getType())
                    ? options.stream().mapToInt(QuestionOption::score).sum()
                    : options.stream().mapToInt(QuestionOption::score).max().orElse(0);
            // 计算本题用户得分
            totalEarned += earnedOf(q, options, answerMap.get(q.getId()));
            totalMax += max;
        }
        // 全文本题 则返回
        if (totalMax <= 0) {
            return null;
        }
        // 百分制
        long scaled = Math.round(totalEarned * 100.0 / totalMax);
        return (int) Math.max(0, Math.min(100, scaled));
    }

    /**
     * 计算单题得分
     */
    private int earnedOf(Question q, List<QuestionOption> options, AssessmentAnswerItem answer) {
        if (answer == null) {
            return 0;
        }
        Object value = answer.getValue();
        // 判断题目类型并计分
        if (QuestionType.SINGLE.getCode().equals(q.getType()) && value instanceof Number n) {
            int idx = n.intValue();
            return idx >= 0 && idx < options.size() ? options.get(idx).score() : 0;
        }
        if (QuestionType.MULTIPLE.getCode().equals(q.getType()) && value instanceof List<?> list) {
            int sum = 0;
            for (Object o : list) {
                if (o instanceof Number n) {
                    int idx = n.intValue();
                    if (idx >= 0 && idx < options.size()) {
                        sum += options.get(idx).score();
                    }
                }
            }
            return sum;
        }
        return 0;
    }

    /**
     * 解析题目选项配置
     *
     * @param options
     * @return
     */
    private List<QuestionOption> parseOptions(String options) {
        if (options == null || options.isBlank()) {
            return List.of();
        }
        try {
            // 用 ObjectMapper把 JSON 字符串解析成一棵 JSON 树（JsonNode）
            JsonNode arr = objectMapper.readTree(options);
            // 不是数组则返回空列表
            if (!arr.isArray()) {
                return List.of();
            }
            List<QuestionOption> result = new ArrayList<>();
            for (JsonNode node : arr) {
                if (node.isObject()) {
                    result.add(
                            new QuestionOption(
                                    node.path("label").asText(),
                                    node.path("score").asInt(0)
                            ));
                }
            }
            return result;
        } catch (Exception e) {
            log.warn("解析题目选项失败: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 调用AiService生成建议
     */
    private String callAiSuggestion(Questionnaire questionnaire, List<Question> questions,
                                    List<AssessmentAnswerItem> answers) {
        String prompt = buildPrompt(questionnaire, questions, answers);
        try {
            return aiService.chat(
                    "你是一位专业的健康评测专家，请根据用户的作答给出个性化的健康建议。",
                    List.of(new AiService.ChatMessage("user", prompt)));
        } catch (Exception e) {
            log.error("AI 评测建议生成异常: questionnaireId={}", questionnaire.getId(), e);
            return "AI建议暂时无法生成，请稍后查看";
        }
    }

    /**
     * 构造 AI Prompt，把问卷标题、所有题目和用户的作答拼成一段文本，发给 AI
     */
    private String buildPrompt(Questionnaire questionnaire, List<Question> questions,
                               List<AssessmentAnswerItem> answers) {
        StringBuilder sb = new StringBuilder();
        sb.append("请根据以下健康评测答卷，为用户给出 2-4 条简短易懂、贴合作答情况的健康建议。\n");
        sb.append("问卷：《").append(questionnaire.getTitle()).append("》\n");
        sb.append("题目与作答：\n");
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            String answer = answers.stream()
                    .filter(a -> a.getQid().equals(q.getId()))
                    .findFirst()
                    .map(a -> String.valueOf(a.getValue()))
                    .orElse("未作答");
            sb.append(i + 1).append(". ").append(q.getContent())
                    .append(" [").append(q.getType()).append("] → ").append(answer).append("\n");
        }
        sb.append("请直接输出建议文本，不要输出分数。不要输出多余的符号");
        return sb.toString();
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new BusinessException("答案序列化失败");
        }
    }


    private AssessmentVO toVO(AssessmentResult result, String questionnaireTitle) {
        AssessmentVO vo = new AssessmentVO();
        vo.setId(result.getId());
        vo.setQuestionnaireId(result.getQuestionnaireId());
        vo.setQuestionnaireTitle(questionnaireTitle);
        vo.setScore(result.getScore());
        vo.setLevel(AssessmentLevel.getDescByScore(result.getScore()));
        vo.setAiSuggestion(result.getAiSuggestion());
        vo.setCreateTime(result.getCreateTime());
        vo.setStatus(result.getStatus());
        vo.setErrorMsg(result.getErrorMsg());
        return vo;
    }

    /**
     * 题目选项（label 文案 + score 分值）
     */
    private record QuestionOption(String label, int score) {
    }
}
