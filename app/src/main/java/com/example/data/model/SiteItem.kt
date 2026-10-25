package com.example.data.model

enum class SiteItemType(val persianLabel: String) {
    POST("نوشته وردپرس"),
    PAGE("برگه وردپرس"),
    PRODUCT("محصول ووکامرس")
}

data class SiteItem(
    val id: Long,
    val title: String,
    val content: String,
    val excerpt: String = "",
    val status: String = "publish", // publish, draft, pending
    val type: SiteItemType = SiteItemType.POST,
    val dateModified: String = "",
    
    // Strict Locked Fields (Read-Only)
    val slug: String,
    val categories: List<String> = emptyList(),
    val regularPrice: String = "",
    val salePrice: String = "",
    
    // Open & Optimizable SEO Fields
    val focusKeyword: String = "",
    val metaTitle: String = "",
    val metaDescription: String = "",
    val schemaJson: String = "",
    val imageAlt: String = "",
    val seoScore: Int = 65 // 0-100 SEO health score
) {
    val isProduct: Boolean get() = type == SiteItemType.PRODUCT
}
