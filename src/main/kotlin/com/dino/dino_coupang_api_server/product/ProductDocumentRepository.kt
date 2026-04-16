package com.dino.dino_coupang_api_server.product

import com.dino.dino_coupang_api_server.product.domain.ProductDocument
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface ProductDocumentRepository  : ElasticsearchRepository<ProductDocument, String> {
}