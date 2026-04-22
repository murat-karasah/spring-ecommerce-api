package com.example.ecommerce.mapper;

import com.example.ecommerce.dto.response.OrderItemResponse;
import com.example.ecommerce.dto.response.OrderResponse;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface OrderMapper {

    @Mapping(source = "user.id", target = "userId")
    OrderResponse toResponse(Order order);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(target = "subtotal",
            expression = "java(item.getUnitPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))")
    OrderItemResponse toItemResponse(OrderItem item);
}
