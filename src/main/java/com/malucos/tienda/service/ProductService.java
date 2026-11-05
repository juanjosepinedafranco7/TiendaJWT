package com.malucos.tienda.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.malucos.tienda.dto.HttpGlobalResponseDTO;
import com.malucos.tienda.dto.ProductRequestDTO;
import com.malucos.tienda.entity.Categories;
import com.malucos.tienda.entity.Products;
import com.malucos.tienda.entity.Users;
import com.malucos.tienda.repository.CategoriesRepository;
import com.malucos.tienda.repository.ProductsRepository;
import com.malucos.tienda.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductsRepository productsRepository;
    private final CategoriesRepository categoriesRepository;
    private final UserRepository userRepository;

    public HttpGlobalResponseDTO<List<Products>> getProducts(Long categoryId) {
        HttpGlobalResponseDTO<List<Products>> response = new HttpGlobalResponseDTO<>();
        List<Products> products = categoryId != null ? 
            productsRepository.findByCategoryId(categoryId) : 
            productsRepository.findAll();
        response.setMessage("Productos encontrados");
        response.setData(products);
        return response;
    }

    public HttpGlobalResponseDTO<Products> getProductById(Long id) {
        HttpGlobalResponseDTO<Products> response = new HttpGlobalResponseDTO<>();
        Products product = productsRepository.findById(id).orElse(null);
        if (product == null) {
            response.setMessage("Producto no encontrado");
        } else {
            response.setMessage("Producto encontrado");
            response.setData(product);
        }
        return response;
    }

    public HttpGlobalResponseDTO<List<Products>> getProductsBySeller(Long sellerId) {
        HttpGlobalResponseDTO<List<Products>> response = new HttpGlobalResponseDTO<>();
        response.setMessage("Productos del vendedor encontrados");
        response.setData(productsRepository.findBySellerId(sellerId));
        return response;
    }

    public HttpGlobalResponseDTO<Products> createProduct(ProductRequestDTO request, Long sellerId) {
        HttpGlobalResponseDTO<Products> response = new HttpGlobalResponseDTO<>();
        Users seller = userRepository.findById(sellerId).orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));
        Categories category = categoriesRepository.findById(request.getCategoryId()).orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        Products product = new Products();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(category);
        product.setSeller(seller);

        productsRepository.save(product);
        response.setMessage("Producto creado correctamente");
        response.setData(product);
        return response;
    }

    public HttpGlobalResponseDTO<Products> updateProduct(Long id, ProductRequestDTO request, Long userId, Long rolId) {
        HttpGlobalResponseDTO<Products> response = new HttpGlobalResponseDTO<>();
        Products product = productsRepository.findById(id).orElse(null);
        if (product == null) {
            response.setMessage("Producto no encontrado");
            return response;
        }

        // Si es VENDEDOR (rolId = 3), valida que sea dueño del producto
        if (rolId == 3L && !product.getSeller().getId().equals(userId)) {
            response.setMessage("No tienes permiso para editar este producto ajeno");
            return response; // En el controlador se mapea a 403
        }

        Categories category = categoriesRepository.findById(request.getCategoryId()).orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(category);

        productsRepository.save(product);
        response.setMessage("Producto actualizado correctamente");
        response.setData(product);
        return response;
    }

    public HttpGlobalResponseDTO<Void> deleteProduct(Long id, Long userId, Long rolId) {
        HttpGlobalResponseDTO<Void> response = new HttpGlobalResponseDTO<>();
        Products product = productsRepository.findById(id).orElse(null);
        if (product == null) {
            response.setMessage("Producto no encontrado");
            return response;
        }

        if (rolId == 3L && !product.getSeller().getId().equals(userId)) {
            response.setMessage("No tienes permiso para eliminar este producto ajeno");
            return response;
        }

        productsRepository.delete(product);
        response.setMessage("Producto eliminado correctamente");
        return response;
    }
}