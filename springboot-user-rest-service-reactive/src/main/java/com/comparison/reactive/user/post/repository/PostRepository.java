package com.comparison.reactive.user.post.repository;

import com.comparison.reactive.user.post.model.Post;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PostRepository {
    Flux<Post> findPostForUser(long userId);

    Mono<Post> save(long userId, Post post);
}
