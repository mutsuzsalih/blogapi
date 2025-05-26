package com.blog.blogapi.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.blog.blogapi.dto.PostRequest;
import com.blog.blogapi.dto.PostResponse;
import com.blog.blogapi.model.Post;
import com.blog.blogapi.model.Tag;
import com.blog.blogapi.model.User;
import com.blog.blogapi.repository.PostRepository;
import com.blog.blogapi.repository.TagRepository;
import com.blog.blogapi.repository.UserRepository;

@Service
@Transactional
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository, TagRepository tagRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
    }

    public PostResponse createPost(PostRequest request) {
        Long authorId = 1L; // TODO: Get authorId from JWT in real project
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Author not found"));

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAuthor(author);

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(request.getTagIds());
            post.setTags(new HashSet<>(tags));
        } else {
            post.setTags(new HashSet<>());
        }

        Post saved = postRepository.save(post);
        return toPostResponse(saved);
    }

    public PostResponse getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return toPostResponse(post);
    }

    public List<PostResponse> getAllPosts() {
        return postRepository.findAll().stream()
                .map(this::toPostResponse)
                .collect(Collectors.toList());
    }

    public PostResponse updatePost(Long id, PostRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(request.getTagIds());
            post.setTags(new HashSet<>(tags));
        } else {
            post.setTags(new HashSet<>());
        }
        Post updated = postRepository.save(post);
        return toPostResponse(updated);
    }

    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        postRepository.delete(post);
    }

    private PostResponse toPostResponse(Post post) {
        PostResponse dto = new PostResponse();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setAuthorId(post.getAuthor().getId());
        dto.setAuthorUsername(post.getAuthor().getUsername());
        Set<Tag> tags = post.getTags();
        if (tags != null && !tags.isEmpty()) {
            List<String> tagNames = tags.stream()
                    .map(Tag::getName)
                    .collect(Collectors.toList());
            dto.setTags(tagNames);
        } else {
            dto.setTags(List.of());
        }
        return dto;
    }
}