package com.example.ecommerce.service;

import com.example.ecommerce.dto.request.ProductRequest;
import com.example.ecommerce.dto.response.PageResponse;
import com.example.ecommerce.dto.response.ProductResponse;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.mapper.ProductMapper;
import com.example.ecommerce.repository.CategoryRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public PageResponse<ProductResponse> findAll(int page, int size, String[] sort,
                                                  Long categoryId, BigDecimal minPrice,
                                                  BigDecimal maxPrice, String search) {
        size = Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size, buildSort(sort));

        Specification<Product> spec = ProductSpecification.withFilters(
                categoryId, minPrice, maxPrice, search);

        return PageResponse.of(
                productRepository.findAll(spec, pageable).map(productMapper::toResponse)
        );
    }

    public ProductResponse findById(Long id) {
        return productMapper.toResponse(getOrThrow(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = productMapper.toEntity(request);
        if (request.categoryId() != null) {
            product.setCategory(getCategoryOrThrow(request.categoryId()));
        }
        Product saved = productRepository.save(product);
        log.info("Product created: id={}, name={}", saved.getId(), saved.getName());
        return productMapper.toResponse(saved);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getOrThrow(id);
        productMapper.updateEntity(request, product);
        product.setCategory(
                request.categoryId() != null ? getCategoryOrThrow(request.categoryId()) : null
        );
        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        getOrThrow(id);
        productRepository.deleteById(id);
        log.info("Product deleted: id={}", id);
    }

    // ---- helpers ----

    private Product getOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    private com.example.ecommerce.entity.Category getCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
    }

    private Sort buildSort(String[] sort) {
        if (sort == null || sort.length == 0) {
            return Sort.by("createdAt").descending();
        }
        Sort.Direction direction = (sort.length > 1)
                ? Sort.Direction.fromOptionalString(sort[1]).orElse(Sort.Direction.ASC)
                : Sort.Direction.ASC;
        return Sort.by(direction, sort[0]);
    }
}
