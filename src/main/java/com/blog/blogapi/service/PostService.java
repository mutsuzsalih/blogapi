package com.blog.blogapi.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.blog.blogapi.dto.PostRequest;
import com.blog.blogapi.dto.PostResponse;
import com.blog.blogapi.exception.ResourceNotFoundException;
import com.blog.blogapi.model.Post;
import com.blog.blogapi.model.Tag;
import com.blog.blogapi.model.User;
import com.blog.blogapi.repository.PostRepository;
import com.blog.blogapi.repository.TagRepository;

@Service
@Transactional
public class PostService {
    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final AuthorizationService authorizationService;

    public PostService(PostRepository postRepository, TagRepository tagRepository,
            AuthorizationService authorizationService) {
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.authorizationService = authorizationService;
    }

    public PostResponse createPost(PostRequest request) {
        User currentUser = authorizationService.getCurrentUserOrThrow();

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAuthor(currentUser);

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
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
        return toPostResponse(post);
    }

    public List<PostResponse> getAllPosts() {
        return postRepository.findAll().stream()
                .map(this::toPostResponse)
                .toList();
    }

    public PostResponse updatePost(Long id, PostRequest request) {
        authorizationService.checkPostOwnerOrAdmin(id);
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));

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
        authorizationService.checkPostOwnerOrAdmin(id);
        // checkPostOwnerOrAdmin çağrısı, postun varlığını ve yetkiyi kontrol eder.
        // Eğer bir exception fırlatılmazsa, post vardır ve silinebilir.
        // Bu nedenle, burada tekrar findById veya existsById yapmaya gerek yok.
        // Sadece deleteById yeterlidir.
        postRepository.deleteById(id);
    }

    private PostResponse toPostResponse(Post post) {
        PostResponse dto = new PostResponse();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());

        if (post.hasAuthor()) {
            dto.setAuthorId(post.getAuthorId());
            dto.setAuthorUsername(post.getAuthorUsername());
        }
        Set<Tag> tags = post.getTags();
        if (tags != null && !tags.isEmpty()) {
            List<String> tagNames = tags.stream()
                    .map(Tag::getName)
                    .toList();
            dto.setTags(tagNames);
        } else {
            dto.setTags(List.of());
        }
        return dto;
    }
}