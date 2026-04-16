package com.dino.dino_coupang_api_server.product

import co.elastic.clients.elasticsearch._helpers.esql.EsqlHelper.query
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType
import com.dino.dino_coupang_api_server.product.domain.Product
import com.dino.dino_coupang_api_server.product.domain.ProductDocument
import com.dino.dino_coupang_api_server.product.dto.CreateProductRequestDto
import org.springframework.data.domain.PageRequest
import org.springframework.data.elasticsearch.client.elc.NativeQuery
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val productDocumentRepository: ProductDocumentRepository,
    private val elasticsearchOperations: ElasticsearchOperations
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

    fun getSuggestions(query: String): List<String> {

        val multiMatchQuery = MultiMatchQuery.of { m ->
            m.query(query)
                .type(TextQueryType.BoolPrefix)
                .fields(
                    "name.auto_complete",
                    "name.auto_complete._2gram",
                    "name.auto_complete._3gram"
                )
        }._toQuery()

        val nativeQuery = NativeQuery.builder()
            .withQuery(multiMatchQuery)
            .withPageable(PageRequest.of(0, 5))
            .build()


        // Query를 어디로 보낼지는 ProductDocument 명세에 다 정해져있다.
        val searchHits = elasticsearchOperations.search(
            nativeQuery,
            ProductDocument::class.java
        )

        // 데이터 검색하면 조회되는 값이 hit에 담긴다.
        return searchHits.searchHits
            .map { hit ->
                val productDocument = hit.content
                println(productDocument)
                productDocument.name ?: ""
            }
    }

}