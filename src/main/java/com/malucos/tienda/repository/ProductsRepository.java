package com.malucos.tienda.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.malucos.tienda.entity.Products;

@Repository
public interface ProductsRepository extends JpaRepository<Products, Long> {
    List<Products> findByCategoryId(Long categoryId);
    List<Products> findBySellerId(Long sellerId);
}