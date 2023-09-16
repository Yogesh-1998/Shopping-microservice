package com.shopping.inventoryservice;

import com.shopping.inventoryservice.model.Inventory;
import com.shopping.inventoryservice.repository.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class InventoryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryServiceApplication.class, args);
	}

	@Bean
	public CommandLineRunner loadData(InventoryRepository inventoryRepository){
		return args -> {
			Inventory inventory = Inventory.builder()
					.skuCode("iphone_13")
					.quantity(0)
					.build();
			Inventory inventory1 = Inventory.builder()
					.skuCode("iphone_14")
					.quantity(100)
					.build();
			inventoryRepository.save(inventory1);
			inventoryRepository.save(inventory);
		};
	}
}
