package com.example.ecommerce.service;

import com.example.ecommerce.dto.request.ProductRequest;
import com.example.ecommerce.dto.response.PageResponse;
import com.example.ecommerce.dto.response.ProductResponse;
import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.mapper.ProductMapper;
import com.example.ecommerce.repository.CategoryRepository;
import com.example.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock ProductMapper productMapper;

    @InjectMocks ProductService productService;

    private Product product;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        Category category = Category.builder().id(1L).name("Electronics").build();
        product = Product.builder()
                .id(1L)
                .name("MacBook Pro")
                .price(BigDecimal.valueOf(1999.99))
                .stockQuantity(10)
                .category(category)
                .build();
        productResponse = new ProductResponse(1L, "MacBook Pro", null,
                BigDecimal.valueOf(1999.99), 10, null, 1L, "Electronics", null, null);
    }

    @Test
    void findById_existingProduct_returnsResponse() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        ProductResponse result = productService.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("MacBook Pro");
    }

    @Test
    void findById_notFound_throwsResourceNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAll_returnsPageResponse() {
        var page = new PageImpl<>(List.of(product));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        PageResponse<ProductResponse> result = productService.findAll(0, 20,
                new String[]{"createdAt", "desc"}, null, null, null, null);

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void create_withValidCategory_savesProduct() {
        ProductRequest request = new ProductRequest("MacBook Pro", null,
                BigDecimal.valueOf(1999.99), 10, null, 1L);
        Category category = Category.builder().id(1L).name("Electronics").build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productMapper.toEntity(request)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        ProductResponse result = productService.create(request);

        assertThat(result.id()).isEqualTo(1L);
        verify(productRepository).save(product);
    }

    @Test
    void create_withInvalidCategory_throwsResourceNotFoundException() {
        ProductRequest request = new ProductRequest("MacBook Pro", null,
                BigDecimal.valueOf(1999.99), 10, null, 99L);

        when(productMapper.toEntity(request)).thenReturn(product);
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(productRepository, never()).save(any());
    }

    @Test
    void delete_existingProduct_deletesSuccessfully() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.delete(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    void delete_notFound_throwsResourceNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void findAll_capsPageSizeAt100() {
        var page = new PageImpl<Product>(List.of());
        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        productService.findAll(0, 9999, new String[]{"createdAt"}, null, null, null, null);

        verify(productRepository).findAll(any(Specification.class),
                argThat((Pageable p) -> p.getPageSize() == 100));
    }
}
