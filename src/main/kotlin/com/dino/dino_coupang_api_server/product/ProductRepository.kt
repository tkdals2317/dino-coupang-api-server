package com.dino.dino_coupang_api_server.product

import com.dino.dino_coupang_api_server.product.domain.Product
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository: JpaRepository<Product, Long> {
}