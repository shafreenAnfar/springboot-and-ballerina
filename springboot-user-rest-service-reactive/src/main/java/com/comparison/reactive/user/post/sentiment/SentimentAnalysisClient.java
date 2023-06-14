package com.comparison.reactive.user.post.sentiment;

import reactor.core.publisher.Mono;

public interface SentimentAnalysisClient {

    Mono<Sentiment> retrieveSentiment(String content);
}
