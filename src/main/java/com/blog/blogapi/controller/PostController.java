package com.blog.blogapi.controller;

import com.blog.blogapi.model.Post;
import com.blog.blogapi.repository.PostRepository;
import com.blog.blogapi.repository.TagRepository;
import com.blog.blogapi.model.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TagRepository tagRepository;

    @GetMapping
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    @GetMapping("/{id}")
    public Post getPostById(@PathVariable Long id) {
        return postRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Post createPost(@RequestBody Post post) {
        // Tag id'leriyle gerçek tag nesnelerini bul
        Set<Tag> realTags = new HashSet<>();
        if (post.getTags() != null) {
            for (Tag tag : post.getTags()) {
                tagRepository.findById(tag.getId()).ifPresent(realTags::add);
            }
        }
        post.setTags(realTags);
        return postRepository.save(post);
    }

    @PutMapping("/{id}")
    public Post updatePost(@PathVariable Long id, @RequestBody Post updatedPost) {
        return postRepository.findById(id)
                .map(post -> {
                    post.setTitle(updatedPost.getTitle());
                    post.setContent(updatedPost.getContent());
                    // Tag id'leriyle gerçek tag nesnelerini bul
                    Set<Tag> realTags = new HashSet<>();
                    if (updatedPost.getTags() != null) {
                        for (Tag tag : updatedPost.getTags()) {
                            tagRepository.findById(tag.getId()).ifPresent(realTags::add);
                        }
                    }
                    post.setTags(realTags);
                    return postRepository.save(post);
                })
                .orElse(null);
    }

    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable Long id) {
        postRepository.deleteById(id);
    }
}