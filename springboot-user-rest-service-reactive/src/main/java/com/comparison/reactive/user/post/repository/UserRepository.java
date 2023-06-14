package com.comparison.reactive.user.post.repository;

import com.comparison.reactive.user.post.model.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository {
    Flux<User> findAll();

    Mono<User> save(User user);

    Mono<User> findById(long id);

    Mono<Long> deleteById(long id);
}
