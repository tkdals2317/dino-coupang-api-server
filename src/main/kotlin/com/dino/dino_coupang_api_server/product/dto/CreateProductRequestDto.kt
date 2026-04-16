package com.dino.dino_coupang_api_server.product.dto

data class CreateProductRequestDto(
    val name : String,
    val description : String,
    val price : Long,
    val rating : Double,
    val category : String
)
