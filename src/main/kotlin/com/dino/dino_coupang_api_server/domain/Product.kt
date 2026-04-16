package com.dino.dino_coupang_api_server.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "products")
class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    var name: String,

    @Column(columnDefinition = "TEXT")
    var description: String, // 에디터가 HTML로 상품 설명 저장

    var price: Long,

    var rating: Double,

    var category: String,
) {

}