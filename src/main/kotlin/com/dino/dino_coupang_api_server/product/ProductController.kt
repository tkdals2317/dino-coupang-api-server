package com.dino.dino_coupang_api_server.product

import com.dino.dino_coupang_api_server.product.domain.Product
import com.dino.dino_coupang_api_server.product.dto.CreateProductRequestDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/products")
class ProductController(
    private val productService: ProductService
) {

    @GetMapping
    fun getProducts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ) : ResponseEntity<List<Product>> {
        val products = productService.getProducts(page, size)
        return ResponseEntity.ok(products)
    }

    @PostMapping
    fun createProduct(@RequestBody request: CreateProductRequestDto): ResponseEntity<Product> {
        val createdProduct = productService.createProduct(request)
        return ResponseEntity.ok(createdProduct)
    }

    @DeleteMapping("/{id}")
    fun deleteProduct(@PathVariable id: Long): ResponseEntity<Void> {
        productService.deleteProduct(id)
        return ResponseEntity.noContent().build()
    }


}