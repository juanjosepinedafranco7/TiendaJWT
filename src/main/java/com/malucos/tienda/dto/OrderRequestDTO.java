package com.malucos.tienda.dto;

import lombok.Data;

@Data
public class OrderRequestDTO {
    private Long productId;
    private Integer quantity;
}