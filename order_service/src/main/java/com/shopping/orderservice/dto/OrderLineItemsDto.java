package com.shopping.orderservice.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderLineItemsDto {

    private long id;
    private String skuCode;
    private BigDecimal price;
    private Integer quantity;

}
