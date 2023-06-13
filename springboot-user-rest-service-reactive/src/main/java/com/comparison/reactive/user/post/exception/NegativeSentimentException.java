package com.comparison.reactive.user.post.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN)
public class NegativeSentimentException extends RuntimeException {
    public NegativeSentimentException(String message) {
        super(message);
    }
}
