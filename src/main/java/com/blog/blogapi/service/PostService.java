package com.blog.blogapi.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));
    }

    private boolean isAdmin(User user) {
        return user.getRole().name().equals("ADMIN"); // Role enum ADMIN stringi ile karşılaştır
    }

    public PostResponse createPost(PostRequest request) {
        User currentUser = getCurrentUser();
        // Author zaten SecurityContext'ten gelen kullanıcı olacak
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAuthor(currentUser); // Postun yazarını set et

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
        User currentUser = getCurrentUser();
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        // Yetki kontrolü: Ya ADMIN olacak ya da postun sahibi olacak
        if (!isAdmin(currentUser) && !post.getAuthor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to update this post.");
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(request.getTagIds());
            post.setTags(new HashSet<>(tags));
        } else {
            // Eğer tagId'ler boş geliyorsa ve postta tag varsa, mevcut tagları temizle.
            // Eğer null geliyorsa ve tagları değiştirmek istemiyorsa, mevcut tagları koru.
            // Bu kısım isteğe bağlı, şimdilik basitçe boş set atayalım.
            post.setTags(new HashSet<>()); 
        }
        Post updated = postRepository.save(post);
        return toPostResponse(updated);
    }

    public void deletePost(Long id) {
        User currentUser = getCurrentUser();
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        // Yetki kontrolü: Ya ADMIN olacak ya da postun sahibi olacak
        if (!isAdmin(currentUser) && !post.getAuthor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to delete this post.");
        }

        postRepository.delete(post);
    }

    private PostResponse toPostResponse(Post post) {
        PostResponse dto = new PostResponse();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        // Yazar null değilse ID ve kullanıcı adını al
        if (post.getAuthor() != null) {
            dto.setAuthorId(post.getAuthor().getId());
            dto.setAuthorUsername(post.getAuthor().getUsername());
        }
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