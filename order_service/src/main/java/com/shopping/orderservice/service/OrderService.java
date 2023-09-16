package com.shopping.orderservice.service;

import com.shopping.orderservice.dto.InventoryResponse;
import com.shopping.orderservice.dto.OrderDto;
import com.shopping.orderservice.dto.OrderLineItemsDto;
import com.shopping.orderservice.dto.OrderRequest;
import com.shopping.orderservice.event.OrderPlacedEvent;
import com.shopping.orderservice.model.Order;
import com.shopping.orderservice.model.OrderLineItems;
import com.shopping.orderservice.repository.OrderRepository;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

        private final OrderRepository orderRepository;
        private final WebClient.Builder webClientBuilder;
        private final Tracer tracer;
        private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

        public String placeOrder(OrderRequest orderRequest){
                Order order = new Order();
                order.setOrderNumber(UUID.randomUUID().toString());

                List<OrderLineItems> orderLineItemsList = orderRequest.getOrderLineItemsDtoList().stream()
                        .map(this::mapToOrderLineItems)
                        .toList();

                order.setOrderLineItemsList(orderLineItemsList);

                List<OrderDto> orderDtos = orderLineItemsList.stream().map(orderLineItems ->
                        OrderDto.builder()
                                .quantity(orderLineItems.getQuantity())
                                .skuCode(orderLineItems.getSkuCode())
                                .build())
                        .toList();
                List<String> skuCodeList =orderLineItemsList.stream().map(OrderLineItems::getSkuCode).toList();

                //Call inventory service to check if order in stock
//                InventoryResponse[] inventoryResponseArray = webClientBuilder.build().get()
//                        .uri("http://localhost:8082/api/inventory",
//                                uriBuilder -> uriBuilder.queryParam("skuCode", skuCodeList).build())
////                        .uri("http://localhost:8082/api/inventory")
//                        .retrieve()
//                        .bodyToMono(InventoryResponse[].class)
//                        .block();
                Span inventoryServiceLookup = tracer.nextSpan().name("inventoryServiceLookup");
                try(Tracer.SpanInScope spanInScope = tracer.withSpan(inventoryServiceLookup.start())){
                        InventoryResponse[] inventoryResponseArray = webClientBuilder.build().post()
                                .uri("http://inventory-service/api/inventory")
                                .body(Mono.just(orderDtos),OrderDto.class)
                                .retrieve()
                                .bodyToMono(InventoryResponse[].class)
                                .block();

                        boolean res = Arrays.stream(inventoryResponseArray).allMatch(InventoryResponse::isInStock);
                        if (res){
                                orderRepository.save(order);
                                kafkaTemplate.send("notificationTopic", new OrderPlacedEvent(order.getOrderNumber()));
                                return "Order Placed Successfully";
                        }else {
                                throw new IllegalArgumentException("Order Out Of stock");
                        }
                }finally {
                        inventoryServiceLookup.end();
                }


        }

        private OrderLineItems mapToOrderLineItems(OrderLineItemsDto orderLineItemsDto) {
                return OrderLineItems.builder()
                        .skuCode(orderLineItemsDto.getSkuCode())
                        .id(orderLineItemsDto.getId())
                        .price(orderLineItemsDto.getPrice())
                        .quantity(orderLineItemsDto.getQuantity())
                        .build();
        }

}
