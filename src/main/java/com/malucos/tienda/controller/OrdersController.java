package com.malucos.tienda.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.malucos.tienda.dto.HttpGlobalResponseDTO;
import com.malucos.tienda.dto.OrderRequestDTO;
import com.malucos.tienda.entity.Orders;
import com.malucos.tienda.enums.RoleEnum;
import com.malucos.tienda.security.RequiresRole;
import com.malucos.tienda.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrdersController {

    private final OrderService orderService;

    @PostMapping
    @RequiresRole({RoleEnum.CLIENTE})
    public ResponseEntity<HttpGlobalResponseDTO<Orders>> createOrder(@RequestBody OrderRequestDTO request, HttpServletRequest servletRequest) {
        Long clientId = (Long) servletRequest.getAttribute("userId");
        HttpGlobalResponseDTO<Orders> response = orderService.createOrder(request, clientId);
        if (response.getMessage().contains("Stock insuficiente")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // 400 si no hay stock
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/mine")
    @RequiresRole({RoleEnum.CLIENTE})
    public ResponseEntity<HttpGlobalResponseDTO<List<Orders>>> getMyOrders(HttpServletRequest request) {
        Long clientId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(orderService.getOrdersByClient(clientId));
    }

    @GetMapping("/sales")
    @RequiresRole({RoleEnum.VENDEDOR})
    public ResponseEntity<HttpGlobalResponseDTO<List<Orders>>> getSales(HttpServletRequest request) {
        Long sellerId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(orderService.getOrdersBySeller(sellerId));
    }

    @GetMapping
    @RequiresRole({RoleEnum.ADMIN})
    public ResponseEntity<HttpGlobalResponseDTO<List<Orders>>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PatchMapping("/{id}/status")
    @RequiresRole({RoleEnum.VENDEDOR, RoleEnum.ADMIN})
    public ResponseEntity<HttpGlobalResponseDTO<Orders>> updateStatus(@PathVariable Long id, @RequestParam String status, HttpServletRequest servletRequest) {
        Long userId = (Long) servletRequest.getAttribute("userId");
        Long rolId = (Long) servletRequest.getAttribute("rolId");
        HttpGlobalResponseDTO<Orders> response = orderService.updateOrderStatus(id, status, userId, rolId);
        if (response.getMessage().contains("No tienes permiso")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel")
    @RequiresRole({RoleEnum.CLIENTE})
    public ResponseEntity<HttpGlobalResponseDTO<Orders>> cancelOrder(@PathVariable Long id, HttpServletRequest servletRequest) {
        Long clientId = (Long) servletRequest.getAttribute("userId");
        HttpGlobalResponseDTO<Orders> response = orderService.cancelOrder(id, clientId);
        if (response.getMessage().contains("Solo se pueden cancelar")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // 400 si no está pendiente
        }
        if (response.getMessage().contains("no te pertenece")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response); // 403 si no es suyo
        }
        return ResponseEntity.ok(response);
    }
}