package com.malucos.tienda.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.malucos.tienda.entity.Orders;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Long> {
    List<Orders> findByClientId(Long clientId);
    List<Orders> findByProductSellerId(Long sellerId);
}