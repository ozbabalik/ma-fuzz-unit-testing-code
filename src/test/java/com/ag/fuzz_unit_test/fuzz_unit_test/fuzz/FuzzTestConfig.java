package com.ag.fuzz_unit_test.fuzz_unit_test.fuzz;

import com.ag.fuzz_unit_test.fuzz_unit_test.repository.OrderRepository;
import com.ag.fuzz_unit_test.fuzz_unit_test.repository.UserRepository;
import com.ag.fuzz_unit_test.fuzz_unit_test.service.OrderService;
import com.ag.fuzz_unit_test.fuzz_unit_test.service.UserService;
import com.ag.fuzz_unit_test.fuzz_unit_test.util.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FuzzTestConfig {

    @Bean
    public UserRepository userRepository() {
        return Mockito.mock(UserRepository.class);
    }

    @Bean
    public OrderRepository orderRepository() {
        return Mockito.mock(OrderRepository.class);
    }

    @Bean
    public UserService userService() {
        return new UserService(userRepository());
    }

    @Bean
    public OrderService orderService() {
        return new OrderService(orderRepository(), userRepository());
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
