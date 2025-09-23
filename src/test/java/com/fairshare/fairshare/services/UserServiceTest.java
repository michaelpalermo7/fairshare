package com.fairshare.fairshare.services;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fairshare.fairshare.dto.UserDTO;
import com.fairshare.fairshare.entity.User;
import com.fairshare.fairshare.repository.UserRepository;
import com.fairshare.fairshare.service.UserService;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService; 

    /* test: user creation when email does not exist */
    @Test
    void createUser_createsWhenEmailNotExists() {

        String name = "Alice";
        String email = "alice@example.com";

        when(userRepository.findByUserEmail(email)).thenReturn(Optional.empty());

        User saved = new User();
        saved.setUserId(1L);
        saved.setUserName(name);
        saved.setUserEmail(email);
        
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserDTO result = userService.createUser(email, name);

        assertEquals(1L, result.id());
        assertEquals("Alice", result.name());
        assertEquals("alice@example.com", result.email());
        verify(userRepository).save(any(User.class));
    }

    /* test: user creation when email aleady exists */
    @Test
    void createUser_throwsWhenEmailAlreadyExists() {
        String name = "Alice";
        String email = "alice@example.com";

        User existing = new User();
        existing.setUserId(99L);
        existing.setUserName(name);
        existing.setUserEmail(email);

        when(userRepository.findByUserEmail(email)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () ->
        userService.createUser(email, name)
    );

        verify(userRepository, never()).save(any(User.class));
    }
}
