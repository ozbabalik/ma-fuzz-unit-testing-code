package com.ag.fuzz_unit_test.fuzz_unit_test.integration;

import com.ag.fuzz_unit_test.fuzz_unit_test.dto.UserDto;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.User;
import com.ag.fuzz_unit_test.fuzz_unit_test.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(com.ag.fuzz_unit_test.fuzz_unit_test.controller.UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
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
    void getAllUsers_ShouldReturnUserList() throws Exception {
        List<User> users = Arrays.asList(user1, user2);
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc
                .perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].username", is("user1")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].username", is("user2")));
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() throws Exception {
        when(userService.getUserById(1L)).thenReturn(user1);

        mockMvc.perform(get("/api/users/1")).andExpect(status().isOk()).andExpect(
                jsonPath("$.id", is(1))).andExpect(jsonPath("$.username", is("user1"))).andExpect(
                jsonPath("$.email", is("user1@example.com")));
    }

    @Test
    void createUser_WhenValid_ShouldReturnCreated() throws Exception {
        User createdUser = new User(userDto.getUsername(), userDto.getEmail(),
                                    userDto.getPassword());
        createdUser.setId(3L);
        when(userService.createUser(any(UserDto.class))).thenReturn(createdUser);

        mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userDto))).andExpect(
                status().isCreated()).andExpect(jsonPath("$.id", is(3))).andExpect(
                jsonPath("$.username", is("newUser"))).andExpect(
                jsonPath("$.email", is("newuser@example.com")));
    }

    @Test
    void updateUser_WhenValid_ShouldReturnUpdatedUser() throws Exception {
        User updatedUser = new User(userDto.getUsername(), userDto.getEmail(),
                                    userDto.getPassword());
        updatedUser.setId(1L);
        when(userService.updateUser(eq(1L), any(UserDto.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userDto))).andExpect(
                status().isOk()).andExpect(jsonPath("$.id", is(1))).andExpect(
                jsonPath("$.username", is("newUser"))).andExpect(
                jsonPath("$.email", is("newuser@example.com")));
    }
}
