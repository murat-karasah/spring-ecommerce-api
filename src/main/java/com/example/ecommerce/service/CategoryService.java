package com.example.ecommerce.service;

import com.example.ecommerce.dto.request.CategoryRequest;
import com.example.ecommerce.dto.response.CategoryResponse;
import com.example.ecommerce.entity.Category;
import com.example.ecommerce.exception.BadRequestException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.mapper.CategoryMapper;
import com.example.ecommerce.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    public CategoryResponse findById(Long id) {
        return categoryMapper.toResponse(getOrThrow(id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new BadRequestException("Category '" + request.name() + "' already exists");
        }
        Category saved = categoryRepository.save(categoryMapper.toEntity(request));
        log.info("Category created: id={}, name={}", saved.getId(), saved.getName());
        return categoryMapper.toResponse(saved);
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = getOrThrow(id);
        if (!category.getName().equals(request.name())
                && categoryRepository.existsByName(request.name())) {
            throw new BadRequestException("Category '" + request.name() + "' already exists");
        }
        category.setName(request.name());
        category.setDescription(request.description());
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        getOrThrow(id);
        categoryRepository.deleteById(id);
        log.info("Category deleted: id={}", id);
    }

    private Category getOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }
}
