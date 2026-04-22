package com.example.ecommerce.mapper;

import com.example.ecommerce.dto.response.CartItemResponse;
import com.example.ecommerce.dto.response.CartResponse;
import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface CartMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(target = "total", expression = "java(calculateTotal(cart.getItems()))")
    CartResponse toResponse(Cart cart);

    @Mapping(source = "product.id",    target = "productId")
    @Mapping(source = "product.name",  target = "productName")
    @Mapping(source = "product.price", target = "productPrice")
    @Mapping(target = "subtotal",
             expression = "java(item.getProduct().getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))")
    CartItemResponse toItemResponse(CartItem item);

    default BigDecimal calculateTotal(List<CartItem> items) {
        if (items == null || items.isEmpty()) return BigDecimal.ZERO;
        return items.stream()
                .map(i -> i.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
