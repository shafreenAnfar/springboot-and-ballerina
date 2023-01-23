package com.comparision.userpost;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
public class UserController {

    private UserDaoService service;
    private UserRepository repository;
    private PostRepository postRepository;

    public UserController(UserDaoService service, UserRepository repository) {
        this.service = service;
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

    @GetMapping("/medium/users/{id}/posts")
    public List<Post> retrieveUserPosts(@PathVariable int id) {
        Optional<User> user = repository.findById(id);

        if (user.isEmpty())
            throw new UserNotFoundException("id: " + id);

        return user.get().getPosts();
    }

    @PostMapping("/medium/users")
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        User savedUser = repository.save(user);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedUser.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @DeleteMapping("/medium/users/{id}")
    public void deleteUser(@PathVariable int id) {
        repository.deleteById(id);
    }

    @PostMapping("/medium/users/{id}/post")
    public ResponseEntity<User> createUserPost(@PathVariable int id, @Valid @RequestBody Post post) {
        Optional<User> user = repository.findById(id);

        if (user.isEmpty())
            throw new UserNotFoundException("id: " + id);

        post.setUser(user.get());

        Post savedPost = postRepository.save(post);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedPost.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }
}
