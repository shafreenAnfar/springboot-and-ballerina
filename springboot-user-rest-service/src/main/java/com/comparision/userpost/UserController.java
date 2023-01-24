package com.comparision.userpost;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class UserController {

    private UserRepository repository;
    private PostRepository postRepository;

    public UserController(UserRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/medium/users")
    public List<User> retrieveAllUsers() {
        return repository.findAll();
    }

    @GetMapping("/medium/users/{id}")
    public User retrieveUser(@PathVariable int id) {
        Optional<User> user = repository.findById(id);

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
    @PostMapping("/medium/users")
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        repository.save(user);
        return ResponseEntity.created(null).build();
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Delete user")
    })
    @DeleteMapping("/medium/users/{id}")
    public void deleteUser(@PathVariable int id) {
        repository.deleteById(id);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Create post",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Post.class))}),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDetails.class))})})
    @GetMapping("/medium/users/{id}/posts")
    public List<Post> retrieveUserPosts(@PathVariable int id) {
        Optional<User> user = repository.findById(id);

        if (user.isEmpty())
            throw new UserNotFoundException("id: " + id);

        return user.get().getPosts();
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create post",
                    content = { @Content(mediaType = "application/json",
                        schema = @Schema(implementation = User.class))}),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = { @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorDetails.class))})
    })
    @PostMapping("/medium/users/{id}/post")
    public ResponseEntity<User> createUserPost(@PathVariable int id, @Valid @RequestBody Post post) {
        Optional<User> user = repository.findById(id);

        if (user.isEmpty())
            throw new UserNotFoundException("id: " + id);

        post.setUser(user.get());
        postRepository.save(post);

        return ResponseEntity.created(null).build();
    }
}
