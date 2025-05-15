package com.blog.blogapi.controller;

import com.blog.blogapi.model.Post;
import com.blog.blogapi.model.User;
import com.blog.blogapi.model.Role;
import com.blog.blogapi.service.PostService;
import com.blog.blogapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;


@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;
    private final UserService userService;

    @Autowired
    public PostController(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;
    }

    @GetMapping
    public List<Post> getAllPosts(Principal principal) {
        String username = principal.getName();
        User user = userService.getUserByUsername(username).orElseThrow();
        if (user.getRole() == Role.ADMIN) {
            return postService.getAllPosts();
        } else {
            return postService.getPostsByAuthor(user.getId());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id, Principal principal) {
        String username = principal.getName();
        User user = userService.getUserByUsername(username).orElseThrow();
        Post post = postService.getPostById(id).orElse(null);
        if (post == null) return ResponseEntity.notFound().build();
        if (user.getRole() != Role.ADMIN && !post.getAuthor().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(post);
    }

    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody Post post, Principal principal) {
        try {
            String username = principal.getName();
            Long authorId = userService.findIdByUsername(username);
            Post createdPost = postService.createPost(post, authorId);
            return ResponseEntity.ok(createdPost);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable Long id, @RequestBody Post post, Principal principal) {
        String username = principal.getName();
        User user = userService.getUserByUsername(username).orElseThrow();
        Post existingPost = postService.getPostById(id).orElse(null);
        if (existingPost == null) return ResponseEntity.notFound().build();

        if (user.getRole() != Role.ADMIN && !existingPost.getAuthor().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Post updatedPost = postService.updatePost(id, post, user.getId());
        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id, Principal principal) {
        String username = principal.getName();
        User user = userService.getUserByUsername(username).orElseThrow();
        Post existingPost = postService.getPostById(id).orElse(null);
        if (existingPost == null) return ResponseEntity.notFound().build();

        if (user.getRole() != Role.ADMIN && !existingPost.getAuthor().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        postService.deletePost(id, user.getId());
        return ResponseEntity.ok().build();
    }
}