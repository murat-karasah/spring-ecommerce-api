package com.example.ecommerce.config;

import com.example.ecommerce.mapper.CartMapper;
import com.example.ecommerce.mapper.CategoryMapper;
import com.example.ecommerce.mapper.OrderMapper;
import com.example.ecommerce.mapper.ProductMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Explicitly registers MapStruct mappers as Spring beans.
 * This bypasses component scan for generated implementations and is
 * the most reliable approach across JVM versions.
 */
@Configuration
public class MapperConfig {

    @Bean
    public CategoryMapper categoryMapper() {
        return Mappers.getMapper(CategoryMapper.class);
    }

    @Bean
    public ProductMapper productMapper() {
        return Mappers.getMapper(ProductMapper.class);
    }

    @Bean
    public CartMapper cartMapper() {
        return Mappers.getMapper(CartMapper.class);
    }

    @Bean
    public OrderMapper orderMapper() {
        return Mappers.getMapper(OrderMapper.class);
    }
}
