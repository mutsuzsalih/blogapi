

package com.blog.blogapi.service;

import com.blog.blogapi.model.User;
import com.blog.blogapi.repository.UserRepository;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Test
    void testFindUserById() {
        UserRepository userRepository = mock(UserRepository.class);
        UserService userService = new UserService(userRepository);

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User found = userService.findUserById(1L);

        assertNotNull(found);
        assertEquals("testuser", found.getUsername());
    }
}