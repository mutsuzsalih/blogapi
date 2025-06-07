package com.blog.blogapi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.blog.blogapi.dto.UserRegistrationRequest;
import com.blog.blogapi.dto.UserResponse;
import com.blog.blogapi.exception.DuplicateResourceException;
import com.blog.blogapi.exception.ResourceNotFoundException;
import com.blog.blogapi.model.Role;
import com.blog.blogapi.model.User;
import com.blog.blogapi.repository.UserRepository;

// @SpringBootTest // Birim testleri için genellikle kaldırılır
// @ActiveProfiles("test") // Birim testleri için genellikle kaldırılır
@ExtendWith(MockitoExtension.class) // Mockito entegrasyonu için
class UserServiceTest { // public kaldırıldı

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthorizationService authorizationService; // Eklendi

    @InjectMocks
    private UserService userService;

    private User user1;
    private UserRegistrationRequest registrationRequest;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1L);
        user1.setUsername("testuser1");
        user1.setEmail("testuser1@example.com");
        user1.setRole(Role.USER);
        user1.setPassword("encodedPassword");

        registrationRequest = new UserRegistrationRequest();
        registrationRequest.setUsername("newuser");
        registrationRequest.setEmail("newuser@example.com");
        registrationRequest.setPassword("password123");
    }

    @Test
    @DisplayName("getUserById should return user when user exists and admin is authorized")
    void getUserById_whenUserExistsAndAdminAuthorized_shouldReturnUser() {
        // Given
        doNothing().when(authorizationService).checkAdmin();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        // When
        UserResponse found = userService.getUserById(1L);

        // Then
        assertNotNull(found);
        assertEquals("testuser1", found.getUsername());
        verify(authorizationService).checkAdmin();
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("getUserById should throw ResourceNotFoundException when user does not exist")
    void getUserById_whenUserDoesNotExist_shouldThrowResourceNotFoundException() {
        // Given
        doNothing().when(authorizationService).checkAdmin();
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Then
        var exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(1L);
        });
        assertEquals("User not found with id : '1'", exception.getMessage());
        verify(authorizationService).checkAdmin();
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("getAllUsers should return list of users when admin is authorized")
    void getAllUsers_whenAdminAuthorized_shouldReturnUserList() {
        // Given
        doNothing().when(authorizationService).checkAdmin();
        List<User> users = new ArrayList<>();
        users.add(user1);
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<UserResponse> foundUsers = userService.getAllUsers();

        // Then
        assertNotNull(foundUsers);
        assertEquals(1, foundUsers.size());
        assertEquals("testuser1", foundUsers.get(0).getUsername());
        verify(authorizationService).checkAdmin();
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("registerUser should successfully register new user")
    void registerUser_whenValidRequest_shouldRegisterUser() {
        // Arrange
        when(userRepository.existsByUsername(registrationRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registrationRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registrationRequest.getPassword())).thenReturn("encodedNewPassword");

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setUsername(registrationRequest.getUsername());
        savedUser.setEmail(registrationRequest.getEmail());
        savedUser.setPassword("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        UserResponse response = userService.registerUser(registrationRequest);

        // Assert
        assertNotNull(response);
        assertEquals(registrationRequest.getUsername(), response.getUsername());
        verify(userRepository).existsByUsername(registrationRequest.getUsername());
        verify(userRepository).existsByEmail(registrationRequest.getEmail());
        verify(passwordEncoder).encode(registrationRequest.getPassword());
        verify(userRepository).save(argThat(user -> user.getUsername().equals(registrationRequest.getUsername()) &&
                user.getEmail().equals(registrationRequest.getEmail()) &&
                user.getPassword().equals("encodedNewPassword")));
    }

    @Test
    @DisplayName("registerUser should throw DuplicateResourceException if username exists")
    void registerUser_whenUsernameExists_shouldThrowDuplicateResourceException() {
        // Arrange
        when(userRepository.existsByUsername(registrationRequest.getUsername())).thenReturn(true);

        // Act & Assert
        var exception = assertThrows(DuplicateResourceException.class, () -> {
            userService.registerUser(registrationRequest);
        });
        assertEquals("User with username 'newuser' already exists.", exception.getMessage());
        verify(userRepository).existsByUsername(registrationRequest.getUsername());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("registerUser should throw DuplicateResourceException if email exists")
    void registerUser_whenEmailExists_shouldThrowDuplicateResourceException() {
        // Arrange
        when(userRepository.existsByUsername(registrationRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registrationRequest.getEmail())).thenReturn(true);

        // Act & Assert
        var exception = assertThrows(DuplicateResourceException.class, () -> {
            userService.registerUser(registrationRequest);
        });
        assertEquals("User with email 'newuser@example.com' already exists.", exception.getMessage());
        verify(userRepository).existsByUsername(registrationRequest.getUsername());
        verify(userRepository).existsByEmail(registrationRequest.getEmail());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
}