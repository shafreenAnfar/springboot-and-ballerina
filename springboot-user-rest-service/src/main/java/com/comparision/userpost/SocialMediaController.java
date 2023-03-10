package com.comparision.userpost;

import io.github.resilience4j.retry.annotation.Retry;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class SocialMediaController {

    private UserRepository userRepository;
    private PostRepository postRepository;

    @Autowired
    private Configuration configuration;

    @Autowired
    private SentimentProxy sentimentProxy;

    public SocialMediaController(UserRepository userRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    @GetMapping("/social-media/users")
    public List<User> retrieveAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/social-media/users/{id}")
    public User retrieveUser(@PathVariable int id) {
        Optional<User> user = userRepository.findById(id);

        if (user.isEmpty())
            throw new UserNotFoundException("id: " + id);

        return user.get();
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create user",
                    content = { @Content(mediaType = "application/json",
                        schema = @Schema(implementation = User.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = { @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorDetails.class))})
    })
    @PostMapping("/social-media/users")
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        userRepository.save(user);
        return ResponseEntity.created(null).build();
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Delete user")
    })
    @DeleteMapping("/social-media/users/{id}")
    public void deleteUser(@PathVariable int id) {
        userRepository.deleteById(id);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Create post",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Post.class))}),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class))})})
    @GetMapping("/social-media/users/{id}/posts")
    public List<Post> retrieveUserPosts(@PathVariable int id) {
        Optional<User> user = userRepository.findById(id);

        if (user.isEmpty())
            throw new UserNotFoundException("id: " + id);

        return user.get().getPosts();
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create post",
                    content = { @Content(mediaType = "application/json",
                        schema = @Schema(implementation = User.class))}),
            @ApiResponse(responseCode = "403", description = "Negative sentiment detected",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class))}),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = { @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorDetails.class))})
    })
    @PostMapping("/social-media/users/{id}/post")
    @Retry(name="sentiment-api")
    public ResponseEntity<User> createUserPost(@PathVariable int id, @Valid @RequestBody Post post) {
        Optional<User> user = userRepository.findById(id);

        if (user.isEmpty())
            throw new UserNotFoundException("id: " + id);

        Sentiment sentiment = sentimentProxy.retrieveSentiment(new SentimentRequest(post.getDescription()));
        if (sentiment.getLabel().equalsIgnoreCase("neg") && configuration.isModerate()) {
            throw new NegativeSentimentException("Negative sentiment detected");
        }

        post.setUser(user.get());
        postRepository.save(post);

        return ResponseEntity.created(null).build();
    }
}
