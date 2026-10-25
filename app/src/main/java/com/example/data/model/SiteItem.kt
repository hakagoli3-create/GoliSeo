package com.example.data.model

enum class SiteItemType(val persianLabel: String) {
    POST("نوشته و مقاله"),
    PAGE("برگه و صفحه سایت"),
    PRODUCT("محصول ووکامرس")
}

data class SiteItem(
    val id: Long,
    val title: String,
    val content: String,
    val excerpt: String = "",
    val status: String = "publish", // publish, draft, pending, future, private
    val type: SiteItemType = SiteItemType.POST,
    val dateModified: String = "",
    
    // Strict Locked Fields (Safety Protected)
    val slug: String,
    val categories: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val regularPrice: String = "",
    val salePrice: String = "",
    val stockStatus: String = "instock", // instock, outofstock
    val sku: String = "",
    
    // Open & Optimizable SEO & Rank Math Fields
    val focusKeyword: String = "",
    val secondaryKeywords: List<String> = emptyList(),
    val metaTitle: String = "",
    val metaDescription: String = "",
    val canonicalUrl: String = "",
    val metaRobots: String = "index, follow",
    val seoScore: Int = 65, // 0-100 SEO health score
    
    // Media & Featured Image Metadata
    val featuredImageUrl: String = "",
    val imageAlt: String = "",
    val imageTitle: String = "",
    val imageCaption: String = "",
    
    // Schema Markup JSON-LD
    val schemaType: String = "",
    val schemaJson: String = "",
    
    // Elementor & Page Builder Fields
    val isElementor: Boolean = false,
    val elementorDataJson: String = "",
    val elementorTemplate: String = "default" // default, elementor_header_footer, elementor_canvas
) {
    val isProduct: Boolean get() = type == SiteItemType.PRODUCT
    val isPage: Boolean get() = type == SiteItemType.PAGE
    val isPost: Boolean get() = type == SiteItemType.POST
}
