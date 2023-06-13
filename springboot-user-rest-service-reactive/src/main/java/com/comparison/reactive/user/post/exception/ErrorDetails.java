package com.comparison.reactive.user.post.exception;

import java.time.LocalDate;
import java.util.Map;

public class ErrorDetails {
    private final LocalDate timeStamp;
    private final String message;
    private final Map<String, Object> details;

    public ErrorDetails(String message, Map<String, Object> details) {
        this.timeStamp = LocalDate.now();
        this.message = message;
        this.details = details;
    }

    public LocalDate getTimeStamp() {
        return timeStamp;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
