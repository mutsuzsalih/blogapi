package com.blog.blogapi.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.blog.blogapi.exception.ResourceNotFoundException;
import com.blog.blogapi.model.Post;
import com.blog.blogapi.model.Role;
import com.blog.blogapi.model.User;
import com.blog.blogapi.repository.PostRepository;
import com.blog.blogapi.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthorizationService authorizationService;

    private User adminUser;
    private User regularUser;
    private User anotherUser;
    private Post post;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setRole(Role.ADMIN);

        regularUser = new User();
        regularUser.setId(2L);
        regularUser.setUsername("user");
        regularUser.setRole(Role.USER);

        anotherUser = new User();
        anotherUser.setId(3L);
        anotherUser.setUsername("another");
        anotherUser.setRole(Role.USER);

        post = new Post();
        post.setId(10L);
        post.setAuthor(regularUser);
    }

    private void mockSecurityContext(User user) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(user.getUsername());
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("checkPostOwnerOrAdmin should allow admin user")
    void checkPostOwnerOrAdmin_whenUserIsAdmin_shouldNotThrow() {
        mockSecurityContext(adminUser);
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        assertDoesNotThrow(() -> authorizationService.checkPostOwnerOrAdmin(10L));
    }

    @Test
    @DisplayName("checkPostOwnerOrAdmin should allow post owner")
    void checkPostOwnerOrAdmin_whenUserIsOwner_shouldNotThrow() {
        mockSecurityContext(regularUser);
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        assertDoesNotThrow(() -> authorizationService.checkPostOwnerOrAdmin(10L));
    }

    @Test
    @DisplayName("checkPostOwnerOrAdmin should throw AccessDeniedException for non-owner")
    void checkPostOwnerOrAdmin_whenUserIsNotOwnerOrAdmin_shouldThrow() {
        mockSecurityContext(anotherUser);
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        assertThrows(AccessDeniedException.class, () -> authorizationService.checkPostOwnerOrAdmin(10L));
    }

    @Test
    @DisplayName("checkPostOwnerOrAdmin should throw ResourceNotFoundException if post does not exist")
    void checkPostOwnerOrAdmin_whenPostNotFound_shouldThrowResourceNotFoundException() {
        mockSecurityContext(regularUser);
        when(postRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> authorizationService.checkPostOwnerOrAdmin(10L));
    }

    @Test
    @DisplayName("checkAdmin should allow admin user")
    void checkAdmin_whenUserIsAdmin_shouldNotThrow() {
        mockSecurityContext(adminUser);
        assertDoesNotThrow(() -> authorizationService.checkAdmin());
    }

    @Test
    @DisplayName("checkAdmin should throw AccessDeniedException for non-admin")
    void checkAdmin_whenUserIsNotAdmin_shouldThrow() {
        mockSecurityContext(regularUser);
        assertThrows(AccessDeniedException.class, () -> authorizationService.checkAdmin());
    }
}