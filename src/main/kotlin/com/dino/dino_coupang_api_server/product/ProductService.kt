package com.dino.dino_coupang_api_server.product

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery
import co.elastic.clients.elasticsearch._types.query_dsl.NumberRangeQuery
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType
import com.dino.dino_coupang_api_server.product.domain.Product
import com.dino.dino_coupang_api_server.product.domain.ProductDocument
import com.dino.dino_coupang_api_server.product.dto.CreateProductRequestDto
import org.springframework.data.domain.PageRequest
import org.springframework.data.elasticsearch.client.elc.NativeQuery
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.query.HighlightQuery
import org.springframework.data.elasticsearch.core.query.highlight.Highlight
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField
import org.springframework.data.elasticsearch.core.query.highlight.HighlightParameters
import org.springframework.data.elasticsearch.core.search
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
                productDocument.name
            }
    }

    fun searchProducts(
        query: String,
        category: String?,
        minPrice: Double,
        maxPrice: Double,
        page: Int,
        size: Int
    ): List<ProductDocument> {
        // multi_match 쿼리
        val multiMatchQuery = MultiMatchQuery.of { m ->
            m.query(query)
                .fields("name^3", "description^1", "category^2") // 가중치 설정(name : 3, description : 1, category : 2)
                .fuzziness("AUTO") // 오타 허용
        }._toQuery()

        val filters = mutableListOf<Query>()
        // term filter 쿼리 : 카테고리가 정확히 일치하는 것만 필터링
        if (!category.isNullOrBlank()) {
            val categoryFilter = TermQuery.of { t ->
                t.field("category.raw")
                    .value(category)
            }._toQuery()
            filters.add(categoryFilter)
        }

        // range filter 쿼리 : 가격이 minPrice와 maxPrice 사이인 것만 필터링
        val priceFilter = NumberRangeQuery.of { r ->
            r.field("price")
                .gte(minPrice)
                .lte(maxPrice)
        }._toRangeQuery()._toQuery()
        filters.add(priceFilter)

        // should 쿼리 : rating이 4.0 이상인 경우 점수를 높여주는 역할
        val ratingShould = NumberRangeQuery.of { r ->
            r.field("rating")
                .gte(4.0)
        }._toRangeQuery()._toQuery()
        filters.add(ratingShould)

        // bool 쿼리 : multi_match 쿼리는 must로, 필터들은 filter로, rating should는 should로 묶어서 최종 쿼리 생성
        val boolQuery = BoolQuery.of { b ->
            b.must(multiMatchQuery)
                .filter(filters)
                .should(ratingShould)
        }._toQuery()

        // highlight 설정 : 검색어가 포함된 부분을 <b> 태그로 감싸서 강조 표시
        val highlightParameters = HighlightParameters.builder()
            .withPreTags("<b>")
            .withPostTags("</b>")
            .build()

        val highlight = Highlight(
            highlightParameters,
            listOf(HighlightField("name"))
        )

        val highlightQuery = HighlightQuery(highlight, ProductDocument::class.java)

        val nativeQuery = NativeQuery.builder()
            .withQuery(boolQuery)
            .withHighlightQuery(highlightQuery)
            .withPageable(PageRequest.of(page - 1, size))
            .build()

        val searchHits = elasticsearchOperations.search<ProductDocument>(
            nativeQuery
        )

        return searchHits.searchHits.map { hit ->
            val productDocument = hit.content
            // highlight 결과가 있으면 name 필드를 highlight된 값으로 대체
            val highlightedName = hit.highlightFields["name"]?.firstOrNull()
            if (highlightedName != null) {
                productDocument.name = highlightedName
            }
            productDocument
        }
    }

}