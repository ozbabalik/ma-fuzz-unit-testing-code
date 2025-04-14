package com.ag.fuzz_unit_test.fuzz_unit_test.unit;

import com.ag.fuzz_unit_test.fuzz_unit_test.dto.UserDto;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.User;
import com.ag.fuzz_unit_test.fuzz_unit_test.exception.DuplicateResourceException;
import com.ag.fuzz_unit_test.fuzz_unit_test.exception.ResourceNotFoundException;
import com.ag.fuzz_unit_test.fuzz_unit_test.repository.UserRepository;
import com.ag.fuzz_unit_test.fuzz_unit_test.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user1;
    private User user2;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user1 = new User("user1", "user1@example.com", "password123");
        user1.setId(1L);

        user2 = new User("user2", "user2@example.com", "password456");
        user2.setId(2L);

        userDto = new UserDto("newUser", "newuser@example.com", "newpassword123");
    }

    @Test
    void getAllUsers_ShouldReturnUserList() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<User> users = userService.getAllUsers();

        assertEquals(2, users.size());
        assertEquals(user1, users.get(0));
        assertEquals(user2, users.get(1));
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        User result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(user1.getId(), result.getId());
        assertEquals(user1.getUsername(), result.getUsername());
        assertEquals(user1.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldThrowException() {
        when(userRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(3L));
        verify(userRepository, times(1)).findById(3L);
    }

    @Test
    void createUser_WhenUsernameAndEmailAreUnique_ShouldCreateUser() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(3L);
            return savedUser;
        });

        User result = userService.createUser(userDto);

        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals(userDto.getUsername(), result.getUsername());
        assertEquals(userDto.getEmail(), result.getEmail());
        verify(userRepository, times(1)).existsByUsername(userDto.getUsername());
        verify(userRepository, times(1)).existsByEmail(userDto.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_WhenUsernameExists_ShouldThrowException() {
        when(userRepository.existsByUsername(userDto.getUsername())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.createUser(userDto));
        verify(userRepository, times(1)).existsByUsername(userDto.getUsername());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_WhenEmailExists_ShouldThrowException() {
        when(userRepository.existsByUsername(userDto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.createUser(userDto));
        verify(userRepository, times(1)).existsByUsername(userDto.getUsername());
        verify(userRepository, times(1)).existsByEmail(userDto.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void validateEmail_WhenEmailIsValid_ShouldNotThrowException() {
        assertDoesNotThrow(() -> userService.validateEmail("user@example.com"));
    }

    @Test
    void validateEmail_WhenEmailIsMissingAt_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                     () -> userService.validateEmail("userexample.com"));
    }

    @Test
    void validateEmail_WhenEmailHasEmptyLocalPart_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                     () -> userService.validateEmail("@example.com"));
    }
}
