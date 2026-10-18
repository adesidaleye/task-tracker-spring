package com.adesidaleye.tasks.domain.dto;

public record ApiErrorResponse(
        int status,
        String message,
        String details
) {
}
