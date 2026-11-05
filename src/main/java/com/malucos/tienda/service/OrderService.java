package com.malucos.tienda.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import com.malucos.tienda.dto.HttpGlobalResponseDTO;
import com.malucos.tienda.dto.OrderRequestDTO;
import com.malucos.tienda.entity.Orders;
import com.malucos.tienda.entity.Products;
import com.malucos.tienda.entity.Users;
import com.malucos.tienda.repository.OrdersRepository;
import com.malucos.tienda.repository.ProductsRepository;
import com.malucos.tienda.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrdersRepository ordersRepository;
    private final ProductsRepository productsRepository;
    private final UserRepository userRepository;

    public HttpGlobalResponseDTO<Orders> createOrder(OrderRequestDTO request, Long clientId) {
        HttpGlobalResponseDTO<Orders> response = new HttpGlobalResponseDTO<>();
        Users client = userRepository.findById(clientId).orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        Products product = productsRepository.findById(request.getProductId()).orElse(null);

        if (product == null) {
            response.setMessage("Producto no encontrado");
            return response;
        }

        if (product.getStock() < request.getQuantity()) {
            response.setMessage("Stock insuficiente para realizar el pedido");
            return response; 
        }

        product.setStock(product.getStock() - request.getQuantity());
        productsRepository.save(product);

        BigDecimal total = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        Orders order = new Orders();
        order.setClient(client);
        order.setProduct(product);
        order.setQuantity(request.getQuantity());
        order.setTotal(total);
        order.setStatus("PENDIENTE");

        ordersRepository.save(order);
        response.setMessage("Pedido creado exitosamente");
        response.setData(order);
        return response;
    }

    public HttpGlobalResponseDTO<List<Orders>> getOrdersByClient(Long clientId) {
        HttpGlobalResponseDTO<List<Orders>> response = new HttpGlobalResponseDTO<>();
        response.setMessage("Pedidos del cliente encontrados");
        response.setData(ordersRepository.findByClientId(clientId));
        return response;
    }

    public HttpGlobalResponseDTO<List<Orders>> getOrdersBySeller(Long sellerId) {
        HttpGlobalResponseDTO<List<Orders>> response = new HttpGlobalResponseDTO<>();
        response.setMessage("Pedidos de los productos del vendedor encontrados");
        response.setData(ordersRepository.findByProductSellerId(sellerId));
        return response;
    }

    public HttpGlobalResponseDTO<List<Orders>> getAllOrders() {
        HttpGlobalResponseDTO<List<Orders>> response = new HttpGlobalResponseDTO<>();
        response.setMessage("Todos los pedidos encontrados");
        response.setData(ordersRepository.findAll());
        return response;
    }

    public HttpGlobalResponseDTO<Orders> updateOrderStatus(Long orderId, String newStatus, Long userId, Long rolId) {
        HttpGlobalResponseDTO<Orders> response = new HttpGlobalResponseDTO<>();
        Orders order = ordersRepository.findById(orderId).orElse(null);
        if (order == null) {
            response.setMessage("Pedido no encontrado");
            return response;
        }

        if (rolId == 3L && !order.getProduct().getSeller().getId().equals(userId)) {
            response.setMessage("No tienes permiso para modificar este pedido");
            return response;
        }

        order.setStatus(newStatus);
        ordersRepository.save(order);
        response.setMessage("Estado del pedido actualizado correctamente");
        response.setData(order);
        return response;
    }

    public HttpGlobalResponseDTO<Orders> cancelOrder(Long orderId, Long clientId) {
        HttpGlobalResponseDTO<Orders> response = new HttpGlobalResponseDTO<>();
        Orders order = ordersRepository.findById(orderId).orElse(null);
        if (order == null) {
            response.setMessage("Pedido no encontrado");
            return response;
        }

        if (!order.getClient().getId().equals(clientId)) {
            response.setMessage("Este pedido no te pertenece");
            return response; // Se mapea a 403
        }

        if (!"PENDIENTE".equalsIgnoreCase(order.getStatus())) {
            response.setMessage("Solo se pueden cancelar pedidos en estado PENDIENTE");
            return response; 
        }

        Products product = order.getProduct();
        product.setStock(product.getStock() + order.getQuantity());
        productsRepository.save(product);

        order.setStatus("CANCELADO");
        ordersRepository.save(order);

        response.setMessage("Pedido cancelado y stock devuelto exitosamente");
        response.setData(order);
        return response;
    }
}