package com.dino.dino_coupang_api_server

import com.dino.dino_coupang_api_server.domain.Product
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository: JpaRepository<Product, Long> {
}