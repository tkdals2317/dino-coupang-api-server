package com.dino.dino_coupang_api_server.product

import com.dino.dino_coupang_api_server.product.domain.Product
import com.dino.dino_coupang_api_server.product.domain.ProductDocument
import com.dino.dino_coupang_api_server.product.dto.CreateProductRequestDto
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val productDocumentRepository: ProductDocumentRepository
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
        val savedProduct = productRepository.save(product)

        // Elasticsearch에 저장
        // TODO Kafka 사용해서 Elasticsearch에 반영하는 방식으로 변경
        val productDocument = ProductDocument(
            id = savedProduct.id.toString(),
            name = savedProduct.name,
            description = savedProduct.description,
            price = savedProduct.price,
            rating = savedProduct.rating,
            category = savedProduct.category
        )
        productDocumentRepository.save(productDocument)
        return savedProduct
    }

    fun deleteProduct(id: Long) {
        productRepository.deleteById(id)
        productDocumentRepository.deleteById(id.toString())
    }

}