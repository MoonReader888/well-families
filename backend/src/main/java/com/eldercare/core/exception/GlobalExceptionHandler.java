package com.eldercare.core.exception;

import com.eldercare.core.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常：400 */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusiness(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return build(e.getCode(), e.getMessage());
    }

    /** 参数校验异常：400，返回具体字段错误 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValid(MethodArgumentNotValidException e) {
        String msg = firstFieldError(e.getBindingResult().getFieldError());
        return build(ErrorCode.BAD_REQUEST.getCode(), msg);
    }

    /** 表单绑定校验异常：400 */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>> handleBind(BindException e) {
        String msg = firstFieldError(e.getBindingResult().getFieldError());
        return build(ErrorCode.BAD_REQUEST.getCode(), msg);
    }

    /** 参数缺失：400 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<Void>> handleMissingParam(MissingServletRequestParameterException e) {
        return build(ErrorCode.BAD_REQUEST.getCode(), "缺少参数: " + e.getParameterName());
    }

    /** 参数类型不匹配：400 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return build(ErrorCode.BAD_REQUEST.getCode(), "参数类型错误: " + e.getName());
    }

    /** 请求体不可读：400 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleNotReadable(HttpMessageNotReadableException e) {
        return build(ErrorCode.BAD_REQUEST.getCode(), "请求体格式错误");
    }

    /** 请求方法不支持：405 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return build(ErrorCode.BAD_REQUEST.getCode(), "请求方法不支持: " + e.getMethod());
    }

    /** 静态资源/接口不存在：404 */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Void>> handleNoResource(NoResourceFoundException e) {
        return build(ErrorCode.NOT_FOUND.getCode(), "接口不存在");
    }

    /** 唯一索引冲突（并发重复注册、重复报名等） */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Result<Void>> handleDuplicateKey(DuplicateKeyException e) {
        log.warn("唯一索引冲突: {}", e.getMessage());
        String msg = resolveDuplicateMessage(e.getMessage());
        return build(ErrorCode.BAD_REQUEST.getCode(), msg);
    }

    /** 兜底异常：500 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        log.error("系统异常", e);
        return build(ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getMessage());
    }

    private String firstFieldError(FieldError fieldError) {
        return fieldError == null
                ? ErrorCode.BAD_REQUEST.getMessage()
                : fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }

    private ResponseEntity<Result<Void>> build(int code, String message) {
        HttpStatus status = HttpStatus.resolve(code);
        return ResponseEntity.status(status == null ? HttpStatus.INTERNAL_SERVER_ERROR : status)
                .body(Result.error(code, message));
    }

    /** 从 DuplicateKeyException 消息中推断是哪个唯一索引冲突，给出对应提示 */
    private String resolveDuplicateMessage(String msg) {
        if (msg == null) return "数据冲突，请稍后重试或更换后再提交";
        String lower = msg.toLowerCase();
        // 按"唯一索引名"精确匹配（异常消息 for key 'xxx' 必定带索引名）
        // 不做字段名模糊匹配，避免不同表同名字段被误判
        if (lower.contains("uk_phone")) {
            // 安全：与注册接口主动检查话术保持一致（模糊，防手机号枚举）
            return "手机号或密码错误";
        }
        if (lower.contains("uk_user_activity")) {
            return "您已报名该活动，请勿重复提交";
        }
        if (lower.contains("uk_config_key")) {
            return "同名配置项已存在，请修改配置键名称后再提交";
        }
        // 原代码实际执行逻辑
        if (lower.contains("uk_user_package_date") || (lower.contains("user_id") && lower.contains("package_id"))) {
            return "该日期已预约此套餐，请勿重复预约";
        }
        return "数据冲突，请稍后重试或更换后再提交";
    }
}
