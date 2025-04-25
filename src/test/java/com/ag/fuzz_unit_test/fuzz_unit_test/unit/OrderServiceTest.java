package com.ag.fuzz_unit_test.fuzz_unit_test.unit;

import com.ag.fuzz_unit_test.fuzz_unit_test.dto.OrderDto;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.Order;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.User;
import com.ag.fuzz_unit_test.fuzz_unit_test.exception.ResourceNotFoundException;
import com.ag.fuzz_unit_test.fuzz_unit_test.repository.OrderRepository;
import com.ag.fuzz_unit_test.fuzz_unit_test.repository.UserRepository;
import com.ag.fuzz_unit_test.fuzz_unit_test.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Order order;
    private OrderDto orderDto;

    @BeforeEach
    void setUp() {
        // Setup user
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        
        // Setup order
        order = new Order();
        order.setId(1L);
        order.setOrderNumber("ORD-12345678");
        order.setTotalAmount(BigDecimal.valueOf(100.0));
        order.setShippingAddress("123 Test St");
        order.setStatus(Order.OrderStatus.PENDING);
        order.setUser(user);
        
        // Setup order DTO
        orderDto = new OrderDto();
        orderDto.setUserId(1L);
        orderDto.setTotalAmount(BigDecimal.valueOf(100.0));
        orderDto.setShippingAddress("123 Test St");
    }

    @Test
    void getAllOrders_ShouldReturnAllOrders() {
        // Arrange
        List<Order> orders = Arrays.asList(order, new Order());
        when(orderRepository.findAll()).thenReturn(orders);
        
        // Act
        List<Order> result = orderService.getAllOrders();
        
        // Assert
        assertEquals(2, result.size());
        verify(orderRepository).findAll();
    }

    @Test
    void getOrderById_WithExistingId_ShouldReturnOrder() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        
        // Act
        Order result = orderService.getOrderById(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("ORD-12345678", result.getOrderNumber());
        verify(orderRepository).findById(1L);
    }
    
    @Test
    void getOrderById_WithNonExistingId_ShouldThrowException() {
        // Arrange
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(99L));
        verify(orderRepository).findById(99L);
    }

    @Test
    void getOrdersByUser_WithExistingUserId_ShouldReturnUserOrders() {
        // Arrange
        List<Order> userOrders = Arrays.asList(order);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findByUser(user)).thenReturn(userOrders);
        
        // Act
        List<Order> result = orderService.getOrdersByUser(1L);
        
        // Assert
        assertEquals(1, result.size());
        assertEquals("ORD-12345678", result.get(0).getOrderNumber());
        verify(userRepository).findById(1L);
        verify(orderRepository).findByUser(user);
    }
    
    @Test
    void getOrdersByUser_WithNonExistingUserId_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrdersByUser(99L));
        verify(userRepository).findById(99L);
        verify(orderRepository, never()).findByUser(any(User.class));
    }

    @Test
    void createOrder_WithValidData_ShouldCreateOrder() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(1L);
            return savedOrder;
        });
        
        // Act
        Order result = orderService.createOrder(orderDto);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertTrue(result.getOrderNumber().startsWith("ORD-"));
        assertEquals(BigDecimal.valueOf(100.0), result.getTotalAmount());
        assertEquals("123 Test St", result.getShippingAddress());
        assertEquals(user, result.getUser());
        verify(userRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
    }
    
    @Test
    void createOrder_WithNonExistingUserId_ShouldThrowException() {
        // Arrange
        orderDto.setUserId(99L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(orderDto));
        verify(userRepository).findById(99L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_WithExistingId_ShouldUpdateStatus() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Order result = orderService.updateOrderStatus(1L, Order.OrderStatus.SHIPPED);
        
        // Assert
        assertEquals(Order.OrderStatus.SHIPPED, result.getStatus());
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(order);
    }
    
    @Test
    void updateOrderStatus_WithNonExistingId_ShouldThrowException() {
        // Arrange
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
                () -> orderService.updateOrderStatus(99L, Order.OrderStatus.SHIPPED));
        verify(orderRepository).findById(99L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void deleteOrder_WithExistingId_ShouldDeleteOrder() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        doNothing().when(orderRepository).delete(order);
        
        // Act
        orderService.deleteOrder(1L);
        
        // Assert
        verify(orderRepository).findById(1L);
        verify(orderRepository).delete(order);
    }
    
    @Test
    void deleteOrder_WithNonExistingId_ShouldThrowException() {
        // Arrange
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderService.deleteOrder(99L));
        verify(orderRepository).findById(99L);
        verify(orderRepository, never()).delete(any(Order.class));
    }
} 
