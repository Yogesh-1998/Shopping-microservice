package com.shopping.inventoryservice.service;

import com.shopping.inventoryservice.dto.InventoryResponse;
import com.shopping.inventoryservice.dto.OrderDto;
import com.shopping.inventoryservice.model.Inventory;
import com.shopping.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    @SneakyThrows
    public List<InventoryResponse> isAvailable(List<String> skuCode) {
        log.info("Wait started");
        Thread.sleep(1000);  //Simulate slow behavior
        return inventoryRepository.findBySkuCodeIn(skuCode).stream()
                .map(inventory ->
                    InventoryResponse.builder()
                            .skuCode(inventory.getSkuCode())
                            .isInStock(inventory.getQuantity() > 0)
                            .build()
                ).toList();
    }

    @Transactional(readOnly = true)
    @SneakyThrows
    public List<InventoryResponse> isInStock(List<OrderDto> orderDtoList){
        log.info("Wait started");
//        Thread.sleep(10000);  //Simulate slow behavior
        return orderDtoList.stream().map(orderDto ->
                InventoryResponse.builder()
                        .skuCode(orderDto.getSkuCode())
                        .isInStock(orderDto.getQuantity() <= inventoryRepository.getQuantityBySkuCode(orderDto.getSkuCode()))
                        .build()
        ).toList();
    }
}
