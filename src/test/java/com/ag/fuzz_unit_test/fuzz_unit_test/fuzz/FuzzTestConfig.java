package com.ag.fuzz_unit_test.fuzz_unit_test.fuzz;

import com.ag.fuzz_unit_test.fuzz_unit_test.repository.OrderRepository;
import com.ag.fuzz_unit_test.fuzz_unit_test.repository.UserRepository;
import com.ag.fuzz_unit_test.fuzz_unit_test.service.OrderService;
import com.ag.fuzz_unit_test.fuzz_unit_test.service.UserService;
import com.ag.fuzz_unit_test.fuzz_unit_test.util.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class FuzzTestConfig {

    // Store mocks as fields to avoid Spring proxy issues
    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final OrderRepository orderRepository = Mockito.mock(OrderRepository.class);

    @Bean
    @Primary
    public UserRepository userRepository() {
        return userRepository;
    }

    @Bean
    @Primary
    public OrderRepository orderRepository() {
        return orderRepository;
    }

    @Bean
    @Primary
    public UserService userService() {
        return new UserService(userRepository);
    }

    @Bean
    @Primary
    public OrderService orderService() {
        return new OrderService(orderRepository, userRepository);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public JsonParser jsonParser() {
        return new JsonParser(objectMapper());
    }
}
