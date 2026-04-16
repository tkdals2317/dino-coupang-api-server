package com.dino.dino_coupang_api_server.product

import com.dino.dino_coupang_api_server.product.domain.Product
import com.dino.dino_coupang_api_server.product.dto.CreateProductRequestDto
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductRepository
) {

    fun getProducts(page: Int, size: Int): List<Product> {
        val pageable = PageRequest.of(page, size)
        return productRepository.findAll(pageable).content
    }

    fun createProduct(request: CreateProductRequestDto): Product {
        val product = Product(
            name = request.name,
            description = request.description,
            price = request.price,
            rating = request.rating,
            category = request.category
        )
        return productRepository.save(product)
    }

    fun deleteProduct(id: Long) {
        productRepository.deleteById(id)
    }


}