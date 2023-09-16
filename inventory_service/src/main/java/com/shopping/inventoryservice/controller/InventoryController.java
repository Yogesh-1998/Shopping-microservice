package com.shopping.inventoryservice.controller;

import com.shopping.inventoryservice.dto.InventoryResponse;
import com.shopping.inventoryservice.dto.OrderDto;
import com.shopping.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> isAvailable(@RequestParam List<String> skuCode) throws InterruptedException {
        return inventoryService.isAvailable(skuCode);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> isInStock(@RequestBody List<OrderDto> orderDtoList){
        orderDtoList.stream().forEach(orderDto -> System.out.println(orderDto.toString()));
        return inventoryService.isInStock(orderDtoList);
    }


}
