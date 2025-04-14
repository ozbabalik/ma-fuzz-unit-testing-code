package com.ag.fuzz_unit_test.fuzz_unit_test.dto;

import com.ag.fuzz_unit_test.fuzz_unit_test.entity.Order;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class OrderDto {

    private Long id;

    private String orderNumber;

    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be greater than 0")
    private BigDecimal totalAmount;

    private Order.OrderStatus status;

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    @NotNull(message = "User ID is required")
    private Long userId;

    // Constructors
    public OrderDto() {
    }

    public OrderDto(BigDecimal totalAmount, String shippingAddress, Long userId) {
        this.totalAmount = totalAmount;
        this.shippingAddress = shippingAddress;
        this.userId = userId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Order.OrderStatus getStatus() {
        return status;
    }

    public void setStatus(Order.OrderStatus status) {
        this.status = status;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
