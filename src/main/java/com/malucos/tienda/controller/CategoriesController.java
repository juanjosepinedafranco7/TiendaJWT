package com.malucos.tienda.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.malucos.tienda.dto.CategoryRequestDTO;
import com.malucos.tienda.dto.HttpGlobalResponseDTO;
import com.malucos.tienda.entity.Categories;
import com.malucos.tienda.enums.RoleEnum;
import com.malucos.tienda.security.RequiresRole;
import com.malucos.tienda.service.CategoryService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoriesController {

    private final CategoryService categoryService;

    @GetMapping
    @RequiresRole({RoleEnum.ADMIN, RoleEnum.CLIENTE, RoleEnum.VENDEDOR})
    public ResponseEntity<HttpGlobalResponseDTO<List<Categories>>> getCategories() {
        return ResponseEntity.ok(categoryService.getCategories());
    }

    @PostMapping
    @RequiresRole({RoleEnum.ADMIN})
    public ResponseEntity<HttpGlobalResponseDTO<Categories>> createCategory(@RequestBody CategoryRequestDTO request) {
        HttpGlobalResponseDTO<Categories> response = categoryService.createCategory(request);
        if (response.getData() == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response); // 409 si nombre repetido
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    @RequiresRole({RoleEnum.ADMIN})
    public ResponseEntity<HttpGlobalResponseDTO<Void>> deleteCategory(@PathVariable Long id) {
        HttpGlobalResponseDTO<Void> response = categoryService.deleteCategory(id);
        if (response.getMessage().contains("tiene productos")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response); // 409 si tiene productos asociados
        }
        return ResponseEntity.ok(response);
    }
}