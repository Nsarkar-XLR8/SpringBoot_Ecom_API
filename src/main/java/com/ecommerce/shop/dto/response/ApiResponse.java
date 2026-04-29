package com.ecommerce.shop.dto.response;

import com.ecommerce.shop.enums.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private String code;
    private String message;
    private T data;
    private String requestId;
    private LocalDateTime timestamp;

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(
                ErrorCode.SUCCESS.name(),
                message,
                data,
                MDC.get("requestId"),
                LocalDateTime.now()
        );
    }

    public static <T> ApiResponse<T> error(String message) {
        return error(ErrorCode.INTERNAL_ERROR, message);
    }

    public static <T> ApiResponse<T> error(ErrorCode code, String message) {
        return new ApiResponse<>(
                code.name(),
                message,
                null,
                MDC.get("requestId"),
                LocalDateTime.now()
        );
    }
}
