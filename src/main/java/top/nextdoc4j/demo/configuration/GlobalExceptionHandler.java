package top.nextdoc4j.demo.configuration;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import top.nextdoc4j.demo.enums.ResultCode;
import top.nextdoc4j.demo.model.base.R;

import java.util.stream.Collectors;


/**
 * 全局异常处理器
 *
 * @author Charles7c
 * @since 2024/8/7 20:21
 */
@Slf4j
@Order(99)
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public R<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("[{}] {} - Business Exception: {}", request.getMethod(), request.getRequestURI(), e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理方法参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public R<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e,
                                                             HttpServletRequest request,
                                                             HttpServletResponse response) {
        log.error("[{}] {}", request.getMethod(), request.getRequestURI(), e);
        response.setStatus(HttpStatus.BAD_REQUEST.value());

        String requiredType = e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "Unknown";
        String errorMessage = String.format("参数 '%s' 类型不匹配，期望类型: %s，实际值: %s",
                e.getName(), requiredType, e.getValue());

        return R.fail(ResultCode.BAD_REQUEST.code(), errorMessage);
    }

    /**
     * 处理请求路径不存在异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public R<Void> handleNoHandlerFoundException(NoHandlerFoundException e,
                                                 HttpServletRequest request,
                                                 HttpServletResponse response) {
        log.error("[{}] {}", request.getMethod(), request.getRequestURI(), e);
        response.setStatus(HttpStatus.NOT_FOUND.value());

        String errorMessage = String.format("请求路径不存在: [%s] %s",
                e.getHttpMethod(), e.getRequestURL());

        return R.fail(ResultCode.NOT_FOUND.code(), errorMessage);
    }

    /**
     * 处理不支持的HTTP请求方法异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e,
                                                                HttpServletRequest request,
                                                                HttpServletResponse response) {
        log.error("[{}] {}", request.getMethod(), request.getRequestURI(), e);
        response.setStatus(HttpStatus.METHOD_NOT_ALLOWED.value());

        String supportedMethods = e.getSupportedMethods() != null ?
                String.join(", ", e.getSupportedMethods()) : "Unknown";
        String errorMessage = String.format("不支持的请求方法: [%s]，支持的方法: [%s]",
                e.getMethod(), supportedMethods);

        return R.fail(ResultCode.METHOD_NOT_ALLOWED.code(), errorMessage);
    }

    /**
     * 处理缺少必需参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public R<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e,
                                                                 HttpServletRequest request,
                                                                 HttpServletResponse response) {
        log.error("[{}] {}", request.getMethod(), request.getRequestURI(), e);
        response.setStatus(HttpStatus.BAD_REQUEST.value());

        String errorMessage = String.format("缺少必需参数: '%s'，类型: %s",
                e.getParameterName(), e.getParameterType());

        return R.fail(ResultCode.BAD_REQUEST.code(), errorMessage);
    }

    /**
     * 处理HTTP消息不可读异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e,
                                                         HttpServletRequest request,
                                                         HttpServletResponse response) {
        log.error("[{}] {}", request.getMethod(), request.getRequestURI(), e);
        response.setStatus(HttpStatus.BAD_REQUEST.value());

        String errorMessage = "请求体格式错误";

        // 处理JSON格式错误的详细信息
        if (e.getCause() instanceof InvalidFormatException invalidFormatException) {
            String fieldPath = invalidFormatException.getPath().stream()
                    .map(ref -> ref.getFieldName())
                    .filter(name -> name != null)
                    .collect(Collectors.joining("."));

            errorMessage = String.format("字段 '%s' 格式错误，期望类型: %s，实际值: %s",
                    fieldPath.isEmpty() ? "unknown" : fieldPath,
                    invalidFormatException.getTargetType().getSimpleName(),
                    invalidFormatException.getValue());
        }

        return R.fail(ResultCode.BAD_REQUEST.code(), errorMessage);
    }

    /**
     * 处理文件上传异常
     */
    @ExceptionHandler(MultipartException.class)
    public R<Void> handleMultipartException(MultipartException e,
                                            HttpServletRequest request,
                                            HttpServletResponse response) {
        log.error("[{}] {}", request.getMethod(), request.getRequestURI(), e);
        response.setStatus(HttpStatus.BAD_REQUEST.value());

        String msg = e.getMessage();
        R<Void> defaultFail = R.fail(ResultCode.PAYLOAD_TOO_LARGE.code(), "文件上传失败");

        if (CharSequenceUtil.isBlank(msg)) {
            return defaultFail;
        }

        String sizeLimit;
        Throwable cause = e.getCause();
        if (null != cause) {
            msg = msg.concat(cause.getMessage().toLowerCase());
        }

        if (msg.contains("larger than")) {
            sizeLimit = CharSequenceUtil.subAfter(msg, "larger than ", true);
        } else if (msg.contains("size") && msg.contains("exceed")) {
            sizeLimit = CharSequenceUtil.subBetween(msg, "the maximum size ", " for");
        } else {
            return R.fail(ResultCode.FILE_TOO_LARGE.code(), "上传文件过大");
        }

        try {
            String readableSize = FileUtil.readableFileSize(Long.parseLong(sizeLimit));
            return R.fail(ResultCode.FILE_TOO_LARGE.code(), "文件大小超出限制，最大允许: " + readableSize);
        } catch (NumberFormatException ex) {
            return defaultFail;
        }
    }

    /**
     * 处理不支持的Content-Type异常
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public R<Void> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e,
                                                            HttpServletRequest request,
                                                            HttpServletResponse response) {
        log.error("[{}] {}", request.getMethod(), request.getRequestURI(), e);
        response.setStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());

        String supportedTypes = e.getSupportedMediaTypes().stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));

        String errorMessage = String.format("不支持的Content-Type: %s，支持的类型: [%s]",
                e.getContentType(), supportedTypes);

        return R.fail(ResultCode.UNSUPPORTED_MEDIA_TYPE.code(), errorMessage);
    }

    /**
     * 处理方法参数校验异常（@Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e,
                                                         HttpServletRequest request,
                                                         HttpServletResponse response) {
        log.error("[{}] {}", request.getMethod(), request.getRequestURI(), e);
        response.setStatus(HttpStatus.BAD_REQUEST.value());

        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining("; "));

        if (errorMessage.isEmpty()) {
            errorMessage = "参数校验失败";
        }

        return R.fail(ResultCode.UNPROCESSABLE_ENTITY.code(), errorMessage);
    }

    /**
     * 处理约束违反异常（@Validated）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public R<Void> handleConstraintViolationException(ConstraintViolationException e,
                                                      HttpServletRequest request,
                                                      HttpServletResponse response) {
        log.error("[{}] {}", request.getMethod(), request.getRequestURI(), e);
        response.setStatus(HttpStatus.BAD_REQUEST.value());

        String errorMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));

        if (errorMessage.isEmpty()) {
            errorMessage = "参数约束校验失败";
        }

        return R.fail(ResultCode.UNPROCESSABLE_ENTITY.code(), errorMessage);
    }

    /**
     * 处理HTTP消息转换异常
     */
    @ExceptionHandler(HttpMessageConversionException.class)
    public R<Void> handleHttpMessageConversionException(HttpMessageConversionException e,
                                                        HttpServletRequest request,
                                                        HttpServletResponse response) {
        log.error("[{}] {}", request.getMethod(), request.getRequestURI(), e);
        response.setStatus(HttpStatus.BAD_REQUEST.value());

        return R.fail(ResultCode.BAD_REQUEST.code(), "请求体参数类型转换失败");
    }

    /**
     * 处理安全相关异常
     */
    @ExceptionHandler(SecurityException.class)
    public R<Void> handleSecurityException(SecurityException e,
                                           HttpServletRequest request,
                                           HttpServletResponse response) {
        log.error("[{}] {} - Security Exception: {}", request.getMethod(), request.getRequestURI(), e.getMessage());
        response.setStatus(HttpStatus.FORBIDDEN.value());

        return R.fail(ResultCode.FORBIDDEN.code(), "安全校验失败: " + e.getMessage());
    }

    /**
     * 处理通用运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public R<Void> handleRuntimeException(RuntimeException e,
                                          HttpServletRequest request,
                                          HttpServletResponse response) {
        log.error("[{}] {} - Runtime Exception", request.getMethod(), request.getRequestURI(), e);
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());

        return R.fail(ResultCode.INTERNAL_ERROR.code(), "系统运行时异常，请联系管理员");
    }

    /**
     * 处理所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {
        log.error("[{}] {} - Unexpected Exception", request.getMethod(), request.getRequestURI(), e);
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());

        return R.fail(ResultCode.INTERNAL_ERROR.code(), "系统异常，请联系管理员");
    }

    /**
     * 自定义业务异常类
     */
    public static class BusinessException extends RuntimeException {
        private final String code;
        private final String message;

        public BusinessException(String code, String message) {
            super(message);
            this.code = code;
            this.message = message;
        }

        public BusinessException(ResultCode resultCode) {
            super(resultCode.msg());
            this.code = resultCode.code();
            this.message = resultCode.msg();
        }

        public String getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }

}