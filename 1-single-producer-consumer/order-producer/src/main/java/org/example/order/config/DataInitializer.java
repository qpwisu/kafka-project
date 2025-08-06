package org.example.order.config;

import lombok.RequiredArgsConstructor;
import org.example.order.entity.Product;
import org.example.order.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final ProductRepository productRepository;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (productRepository.count() == 0) {
                productRepository.save(new Product("맥북", 2000000));
                productRepository.save(new Product("아이폰", 1500000));
                productRepository.save(new Product("에어팟", 300000));
                productRepository.save(new Product("갤럭시탭", 800000));
                productRepository.save(new Product("모니터", 400000));
            }
        };
    }
}