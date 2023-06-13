package com.comparison.reactive.user.post.controller;

import com.comparison.reactive.user.post.exception.NegativeSentimentException;
import com.comparison.reactive.user.post.exception.UserNotFoundException;
import com.comparison.reactive.user.post.model.Post;
import com.comparison.reactive.user.post.model.User;
import com.comparison.reactive.user.post.repository.PostRepository;
import com.comparison.reactive.user.post.repository.UserRepository;
import com.comparison.reactive.user.post.sentiment.SentimentAnalysisClient;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/social-media/users")
public class SocialMediaController {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final SentimentAnalysisClient sentimentAnalysisClient;

    @Value("${sentiment.moderate}")
    private boolean moderate;

    @Autowired
    public SocialMediaController(UserRepository userRepository, PostRepository postRepository,
                                 SentimentAnalysisClient sentimentAnalysisClient) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.sentimentAnalysisClient = sentimentAnalysisClient;
    }

    @GetMapping
    public Flux<User> retrieveAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public Mono<User> retrieveUser(@PathVariable long id) {
        return userRepository
                .findById(id)
                .switchIfEmpty(
                        Mono.error(new UserNotFoundException(String.format("User not found for id: %d", id)))
                );
    }

    @PostMapping
    public Mono<ResponseEntity<Void>> createUser(@Valid @RequestBody User user) {
        return userRepository
                .save(user)
                .map(savedUser -> ResponseEntity.created(null).build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable long id) {
        return userRepository
                .deleteById(id)
                .map(v -> ResponseEntity.noContent().build());
    }

    @GetMapping("/{id}/posts")
    public Flux<Post> retrieveUserPosts(@PathVariable long id) {
        return userRepository
                .findById(id)
                .switchIfEmpty(
                        Mono.error(new UserNotFoundException(String.format("User not found for id: %d", id)))
                ).flatMapMany(user -> postRepository.findPostForUser(id));
    }

    @PostMapping("/{id}/posts")
    public Mono<ResponseEntity<Void>> createUserPost(@PathVariable int id, @Valid @RequestBody Post post) {
        return userRepository
                .findById(id)
                .switchIfEmpty(
                        Mono.error(new UserNotFoundException(String.format("User not found for id: %d", id)))
                ).flatMap(user -> moderateContent(post))
                .flatMap(moderatedPost -> postRepository.save(id, post))
                .map(savedPost -> ResponseEntity.created(null).build());
    }

    private Mono<Post> moderateContent(Post post) {
        if (!moderate) {
            return Mono.just(post);
        }
        return sentimentAnalysisClient
                .retrieveSentiment(post.getDescription())
                .map(sentiment -> !sentiment.getLabel().equalsIgnoreCase("neg"))
                .switchIfEmpty(Mono.error(new NegativeSentimentException("Negative sentiment detected")))
                .map(sentiment -> post);
    }
}
