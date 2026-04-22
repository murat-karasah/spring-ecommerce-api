package com.example.ecommerce.mapper;

import com.example.ecommerce.dto.request.ProductRequest;
import com.example.ecommerce.dto.response.ProductResponse;
import com.example.ecommerce.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper
public interface ProductMapper {

    @Mapping(source = "category.id",   target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    ProductResponse toResponse(Product product);

    @Mapping(target = "id",         ignore = true)
    @Mapping(target = "category",   ignore = true)
    @Mapping(target = "createdAt",  ignore = true)
    @Mapping(target = "updatedAt",  ignore = true)
    Product toEntity(ProductRequest request);

    @Mapping(target = "id",         ignore = true)
    @Mapping(target = "category",   ignore = true)
    @Mapping(target = "createdAt",  ignore = true)
    @Mapping(target = "updatedAt",  ignore = true)
    void updateEntity(ProductRequest request, @MappingTarget Product product);
}
