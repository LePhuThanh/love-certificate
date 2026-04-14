package com.phelim.system.love_certificate.exception;

import com.phelim.system.love_certificate.constant.BaseConstants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Business Exception
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request
    ) {

        log.warn("[GlobalExceptionHandler][BusinessException][DEBUG][MDC] requestId from MDC={}", MDC.get("logId"));
        String requestId = extractRequestId(request, null);

        log.warn("[GlobalExceptionHandler][BusinessException] code={}, message={}, requestId={}",
                ex.getErrorCode().getCode(),
                ex.getMessage(),
                requestId
        );

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .requestId(requestId)
                .errorCode(ex.getErrorCode().getCode())
                .message(ex.getErrorCode().getMessage())
                .details(ex.getDetails())
                .path(request.getRequestURI())
                .validationErrors(null)
                .build();

        return ResponseEntity
                .status(getHttpStatus(ex.getErrorCode()))
                .body(response);
    }

    /**
     * Validation Exception @PhoneNumber + @Identification => its error handle here
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        log.warn("[GlobalExceptionHandler][Validation][DEBUG][MDC] requestId from MDC={}", MDC.get("logId"));
        String requestId = extractRequestId(request, ex);

        log.warn("[GlobalExceptionHandler][Validation] requestId={}, error={}",
                requestId, ex.getMessage());

        List<ErrorResponse.ValidationError> validationErrors =
                ex.getBindingResult()
                        .getAllErrors()
                        .stream()
                        .map(error -> {
                            if (error instanceof FieldError fieldError) {
                                return ErrorResponse.ValidationError.builder()
                                        .field(fieldError.getField())
                                        .message(fieldError.getDefaultMessage())
                                        .build();
                            }
                            return null;
                        })
                        .filter(Objects::nonNull)
                        .toList();

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .requestId(requestId)
                .errorCode(ErrorCode.VALIDATION_ERROR.getCode())
                .message(ErrorCode.VALIDATION_ERROR.getMessage())
                .details("Validation failed")
                .path(request.getRequestURI())
                .validationErrors(validationErrors)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Generic Exception
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {

        log.warn("[GlobalExceptionHandler][Exception][DEBUG][MDC] requestId from MDC={}", MDC.get("logId"));
        String requestId = extractRequestId(request, null);

        log.error("[GlobalExceptionHandler][Exception] requestId={}, error={}",
                requestId, ex.getMessage(), ex);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .requestId(requestId)
                .errorCode(ErrorCode.INTERNAL_ERROR.getCode())
                .message(ErrorCode.INTERNAL_ERROR.getMessage())
                .details(ex.getMessage())
                .path(request.getRequestURI())
                .validationErrors(null)
                .build();

        return ResponseEntity.internalServerError().body(response);
    }

    /**
     * Map ErrorCode → HTTP Status
     */
    private HttpStatus getHttpStatus(ErrorCode errorCode) {
        return switch (errorCode) {

            // 400
            case VALIDATION_ERROR,
                 INVALID_REQUEST -> HttpStatus.BAD_REQUEST;

            // 404
            case SESSION_NOT_FOUND -> HttpStatus.NOT_FOUND;

            // 409
            case IDEMPOTENCY_KEY_REUSED -> HttpStatus.CONFLICT;

            // 500
            case INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;

            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    //Request
    //→ Filter (set MDC)
    //→ Controller
    //→ Service
    //→ Exception xảy ra
    //→ GlobalExceptionHandler
    //→ MDC.get("logId")
    private String extractRequestId(HttpServletRequest request, MethodArgumentNotValidException ex) {

        // 🔥 1. LẤY TỪ MDC (QUAN TRỌNG NHẤT)
        String mdcId = MDC.get("logId");
        if (mdcId != null && !mdcId.isBlank()) {
            return mdcId;
        }

        // 2. fallback header
        String header = request.getHeader("X-Request-Id");
        if (header != null && !header.isBlank()) {
            return header;
        }

        // 3. query param
        String param = request.getParameter(BaseConstants.REQUEST_ID);
        if (param != null && !param.isBlank()) {
            return param;
        }

        // 4. body (validation case)
        if (ex != null && ex.getBindingResult() != null) {
            Object target = ex.getBindingResult().getTarget();
            if (target != null) {
                try {
                    Method method = target.getClass().getMethod(BaseConstants.GET_REQUEST_ID);
                    Object value = method.invoke(target);
                    if (value instanceof String id && !id.isBlank()) {
                        return id;
                    }
                } catch (Exception ignore) {}
            }
        }

        return null;
    }


    /**
     * Extract requestId (banking-style)
     */
//    private String extractRequestId(HttpServletRequest request,
//                                    MethodArgumentNotValidException ex) {
//
//        // 1. từ filter
//        Object attr = request.getAttribute(BaseConstants.REQUEST_ID);
//        if (attr instanceof String id && !id.isBlank()) {
//            return id;
//        }
//
//        // 2. từ query param
//        String param = request.getParameter(BaseConstants.REQUEST_ID);
//        if (param != null && !param.isBlank()) {
//            return param;
//        }
//
//        // 3. từ request body (reflection)
//        if (ex != null && ex.getBindingResult() != null) {
//            Object target = ex.getBindingResult().getTarget();
//            if (target != null) {
//                try {
//                    Method method = target.getClass().getMethod(BaseConstants.GET_REQUEST_ID);
//                    Object value = method.invoke(target);
//                    if (value instanceof String id && !id.isBlank()) {
//                        return id;
//                    }
//                } catch (Exception ignore) {
//                    // ignore safely
//                }
//            }
//        }
//
//        return null;
//    }
}
