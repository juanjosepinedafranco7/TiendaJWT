package com.malucos.tienda.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.malucos.tienda.dto.CategoryRequestDTO;
import com.malucos.tienda.dto.HttpGlobalResponseDTO;
import com.malucos.tienda.entity.Categories;
import com.malucos.tienda.repository.CategoriesRepository;
import com.malucos.tienda.repository.ProductsRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoriesRepository categoriesRepository;
    private final ProductsRepository productsRepository;

    public HttpGlobalResponseDTO<List<Categories>> getCategories() {
        HttpGlobalResponseDTO<List<Categories>> response = new HttpGlobalResponseDTO<>();
        response.setMessage("Categorías encontradas");
        response.setData(categoriesRepository.findAll());
        return response;
    }

    public HttpGlobalResponseDTO<Categories> createCategory(CategoryRequestDTO request) {
        HttpGlobalResponseDTO<Categories> response = new HttpGlobalResponseDTO<>();
        if (categoriesRepository.findByName(request.getName()).isPresent()) {
            response.setMessage("La categoría ya existe");
            return response; // En el controlador se mapea a 409
        }

        Categories category = new Categories();
        category.setName(request.getName());
        categoriesRepository.save(category);

        response.setMessage("Categoría creada exitosamente");
        response.setData(category);
        return response;
    }

    public HttpGlobalResponseDTO<Void> deleteCategory(Long id) {
        HttpGlobalResponseDTO<Void> response = new HttpGlobalResponseDTO<>();
        boolean hasProducts = !productsRepository.findByCategoryId(id).isEmpty();
        if (hasProducts) {
            response.setMessage("No se puede eliminar la categoría porque tiene productos asociados");
            return response; // En el controlador se mapea a 409
        }

        categoriesRepository.deleteById(id);
        response.setMessage("Categoría eliminada correctamente");
        return response;
    }
}