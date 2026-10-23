import request from '../utils/request'

export function listQuestionnaires() {
  return request.get('/member/assessment/questionnaires')
}

export function getQuestionnaireDetail(questionnaireId) {
  return request.get(`/member/assessment/questionnaire/${questionnaireId}`)
}

export function submitAssessment(data) {
  return request.post('/member/assessment/submit', data)
}

export function getAssessmentHistory() {
  return request.get('/member/assessment/history')
}

export function getAssessmentDetail(assessmentId) {
  return request.get(`/member/assessment/history/${assessmentId}`)
}

