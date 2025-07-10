package com.blog.blogapi.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.blog.blogapi.dto.PostRequest;
import com.blog.blogapi.dto.PostResponse;
import com.blog.blogapi.exception.ResourceNotFoundException;
import com.blog.blogapi.model.Post;
import com.blog.blogapi.model.Role;
import com.blog.blogapi.model.Tag;
import com.blog.blogapi.model.User;
import com.blog.blogapi.repository.PostRepository;
import com.blog.blogapi.repository.TagRepository;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private PostService postService;

    private User user;
    private Post post;
    private Tag tag;

    @BeforeEach
    void initializeTestData() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRole(Role.USER);

        tag = new Tag();
        tag.setId(1L);
        tag.setName("java");

        post = new Post();
        post.setId(1L);
        post.setTitle("Test Title");
        post.setContent("Test Content");
        post.setAuthor(user);
        post.setTags(Set.of(tag));
    }

    @Test
    @DisplayName("getPostsByUser should return user posts")
    void getPostsByUser_shouldReturnUserPosts() {
        when(postRepository.findByAuthor_Id(1L)).thenReturn(List.of(post));

        List<PostResponse> responses = postService.getPostsByUser(1L);

        assertEquals(1, responses.size());
        assertEquals("Test Title", responses.get(0).getTitle());
        assertEquals("testuser", responses.get(0).getAuthorUsername());
        verify(postRepository).findByAuthor_Id(1L);
    }

    @Test
    @DisplayName("getPostById should return post when found")
    void getPostById_whenFound_shouldReturnPost() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        PostResponse response = postService.getPostById(1L);
        assertNotNull(response);
        assertEquals("Test Title", response.getTitle());
        verify(postRepository).findById(1L);
    }

    @Test
    @DisplayName("getPostById should throw exception when not found")
    void getPostById_whenNotFound_shouldThrowException() {
        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> postService.getPostById(1L));
        assertEquals("Post not found with id : '1'", exception.getMessage());
        verify(postRepository).findById(1L);
    }

    @Test
    @DisplayName("createPost should create and return post")
    void createPost_shouldCreateAndReturnPost() {
        PostRequest request = new PostRequest();
        request.setTitle("New Post");
        request.setContent("New Content");
        request.setTagIds(List.of(1L));

        when(authorizationService.getCurrentUserOrThrow()).thenReturn(user);
        when(tagRepository.findAllById(List.of(1L))).thenReturn(List.of(tag));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PostResponse response = postService.createPost(request);

        assertNotNull(response);
        assertEquals("New Post", response.getTitle());
        assertEquals("testuser", response.getAuthorUsername());
        assertEquals(1, response.getTags().size());
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("updatePost should update post when authorized")
    void updatePost_whenAuthorized_shouldUpdatePost() {
        // Given
        Long postId = 1L;
        PostRequest request = new PostRequest();
        request.setTitle("Updated Title");
        request.setContent("Updated Content");

        Post existingPost = new Post();
        existingPost.setId(postId);

        when(postRepository.findById(postId)).thenReturn(Optional.of(existingPost));
        when(postRepository.save(any(Post.class))).thenReturn(existingPost);
        doNothing().when(authorizationService).checkPostOwnerOrAdmin(postId);

        // When
        PostResponse response = postService.updatePost(postId, request);

        // Then
        assertNotNull(response);
        assertEquals("Updated Title", response.getTitle());
        verify(authorizationService).checkPostOwnerOrAdmin(postId);
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("updatePost should throw exception when post not found")
    void updatePost_whenPostNotFound_shouldThrowException() {
        // Given
        Long postId = 1L;
        PostRequest request = new PostRequest();
        doThrow(new ResourceNotFoundException("Post", "id", postId))
                .when(authorizationService).checkPostOwnerOrAdmin(postId);

        // Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> postService.updatePost(postId, request));
        assertNotNull(exception);
        verify(authorizationService).checkPostOwnerOrAdmin(postId);
        verify(postRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("deletePost should delete post when authorized")
    void deletePost_whenAuthorized_shouldDeletePost() {
        // Given
        Long postId = 1L;
        doNothing().when(authorizationService).checkPostOwnerOrAdmin(postId);
        doNothing().when(postRepository).deleteById(postId);

        // When
        postService.deletePost(postId);

        // Then
        verify(authorizationService).checkPostOwnerOrAdmin(postId);
        verify(postRepository).deleteById(postId);
    }

    @Test
    @DisplayName("deletePost should throw ResourceNotFoundException when post is not found")
    void deletePost_whenPostNotFound_shouldThrowException() {
        // Given
        Long postId = 1L;
        doThrow(new ResourceNotFoundException("Post", "id", postId))
                .when(authorizationService).checkPostOwnerOrAdmin(postId);

        // Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> postService.deletePost(postId));
        assertNotNull(exception);
        verify(authorizationService).checkPostOwnerOrAdmin(postId);
        verify(postRepository, never()).deleteById(anyLong());
    }
}