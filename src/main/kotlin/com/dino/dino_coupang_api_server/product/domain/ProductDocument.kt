package com.dino.dino_coupang_api_server.product.domain

import jakarta.persistence.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import org.springframework.data.elasticsearch.annotations.InnerField
import org.springframework.data.elasticsearch.annotations.MultiField
import org.springframework.data.elasticsearch.annotations.Setting

@Document(indexName = "products")
@Setting(settingPath = "/elasticsearch/product-settings.json")

class ProductDocument(
    @Id
    val id: String,

    @MultiField(
        mainField = Field(type = FieldType.Text, analyzer = "products_name_analyzer"),
        otherFields = [
            InnerField(
                suffix = "auto_complete",
                type = FieldType.Search_As_You_Type,
                analyzer = "nori"
            )
        ]
    )
    var name: String,

    @Field(type = FieldType.Text, analyzer = "products_description_analyzer")
    var description: String,

    @Field(type = FieldType.Integer)
    var price: Long,

    @Field(type = FieldType.Double)
    var rating: Double,

    @MultiField(
        mainField = Field(type = FieldType.Text, analyzer = "products_category_analyzer"),
        otherFields = [
            InnerField(suffix = "raw", type = FieldType.Keyword)
        ]
    )
    var category: String
) {
}