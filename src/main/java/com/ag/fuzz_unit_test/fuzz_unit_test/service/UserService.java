package com.ag.fuzz_unit_test.fuzz_unit_test.service;

import com.ag.fuzz_unit_test.fuzz_unit_test.dto.UserDto;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.User;
import com.ag.fuzz_unit_test.fuzz_unit_test.exception.DuplicateResourceException;
import com.ag.fuzz_unit_test.fuzz_unit_test.exception.ResourceNotFoundException;
import com.ag.fuzz_unit_test.fuzz_unit_test.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", id));
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User", "email", email));
    }

    @Transactional
    public User createUser(UserDto userDto) {
        // Check if username already exists
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new DuplicateResourceException("User", "username", userDto.getUsername());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new DuplicateResourceException("User", "email", userDto.getEmail());
        }

        // Validate email format (this is a potential target for fuzz testing)
        validateEmail(userDto.getEmail());

        // Create new user
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword()); // In real app, you would hash the password

        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, UserDto userDto) {
        User user = getUserById(id);

        // Check if new username already exists for another user
        if (!user.getUsername().equals(userDto.getUsername()) && userRepository.existsByUsername(
                userDto.getUsername())) {
            throw new DuplicateResourceException("User", "username", userDto.getUsername());
        }

        // Check if new email already exists for another user
        if (!user.getEmail().equals(userDto.getEmail()) && userRepository.existsByEmail(
                userDto.getEmail())) {
            throw new DuplicateResourceException("User", "email", userDto.getEmail());
        }

        // Validate email format
        validateEmail(userDto.getEmail());

        // Update user
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword()); // In real app, you would hash the password

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }

    /**
     * Validates email format.
     * This method contains deliberate vulnerabilities for fuzz testing purposes.
     *
     * @param email the email to validate
     * @throws IllegalArgumentException if the email format is invalid
     */
    public void validateEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }

        if (!email.contains("@")) {
            throw new IllegalArgumentException("Email must contain @");
        }

        String[] parts = email.split("@");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Email must contain exactly one @");
        }

        String localPart = parts[0];
        String domainPart = parts[1];

        if (localPart.isEmpty()) {
            throw new IllegalArgumentException("Local part of email cannot be empty");
        }

        if (domainPart.isEmpty()) {
            throw new IllegalArgumentException("Domain part of email cannot be empty");
        }

        if (!domainPart.contains(".")) {
            throw new IllegalArgumentException("Domain part must contain a dot");
        }

        // More complex validation that could have edge cases
        if (email.length() > 254) {
            throw new IllegalArgumentException("Email is too long");
        }

        // Check for illegal characters (simplified)
        if (email.contains(" ")) {
            throw new IllegalArgumentException("Email cannot contain spaces");
        }

        // Additional validation that could introduce subtle bugs
        if (domainPart.startsWith(".") || domainPart.endsWith(".")) {
            throw new IllegalArgumentException("Domain part cannot start or end with a dot");
        }

        if (localPart.startsWith(".") || localPart.endsWith(".")) {
            throw new IllegalArgumentException("Local part cannot start or end with a dot");
        }

        // This is a deliberate bug for fuzz testing to find
        // It will incorrectly reject emails with certain TLDs
        if (domainPart.endsWith(".test") || domainPart.endsWith(".example")) {
            throw new IllegalArgumentException("Invalid TLD");
        }
    }
}
