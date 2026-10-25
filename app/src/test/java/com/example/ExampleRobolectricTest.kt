package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.HardLockValidator
import com.example.data.model.SecurityHardLockException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read app_name from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("گلیسئو", appName)
  }

  @Test
  fun `verify hard lock validator rejects price changes`() {
    val forbiddenChanges = mapOf(
      "title" to "عنوان جدید",
      "regular_price" to "500000"
    )
    try {
      HardLockValidator.validateModifications(forbiddenChanges)
      fail("Should have thrown SecurityHardLockException")
    } catch (e: SecurityHardLockException) {
      assertEquals("regular_price", e.blockedField)
    }
  }

  @Test
  fun `verify hard lock validator rejects slug changes`() {
    val forbiddenChanges = mapOf(
      "slug" to "new-illegal-slug"
    )
    try {
      HardLockValidator.validateModifications(forbiddenChanges)
      fail("Should have thrown SecurityHardLockException")
    } catch (e: SecurityHardLockException) {
      assertEquals("slug", e.blockedField)
    }
  }

  @Test
  fun `verify hard lock validator allows safe SEO fields`() {
    val allowedChanges = mapOf(
      "title" to "عنوان جدید و جذاب",
      "metaTitle" to "متا تایتل سئو محور",
      "metaDescription" to "توضیحات متا مناسب برای گوگل",
      "focusKeyword" to "خرید کرم آبرسان",
      "schemaJson" to """{"@context":"https://schema.org"}""",
      "imageAlt" to "عکس باکیفیت محصول"
    )
    // Should not throw
    HardLockValidator.validateModifications(allowedChanges)
    assertTrue(true)
  }

  @Test
  fun `verify inspect raw payload catches locked fields`() {
    val jsonPayload = """{"title": "test", "sale_price": "20000", "status": "publish"}"""
    val violations = HardLockValidator.inspectRawPayload(jsonPayload)
    assertTrue(violations.contains("sale_price"))
  }

  @Test
  fun `verify elementor template builder generates valid data and json`() {
    val design = com.example.data.model.ElementorTemplateBuilder.generateDesignForPage(
      pageTitle = "درباره فروشگاه ما",
      pageType = com.example.data.model.ElementorPageType.ABOUT_US,
      brandKnowledge = com.example.data.model.BrandKnowledgeBase()
    )
    assertTrue(design.title.contains("درباره"))
    assertTrue(design.sections.isNotEmpty())
    assertTrue(design.elementorJson.contains("\"elType\":\"section\""))
    assertTrue(design.elementorJson.contains("widgetType"))
    assertTrue(design.semanticHtml.contains("elementor-custom-page"))
    assertTrue(design.recommendedRankMathTitle.contains("درباره") || design.recommendedRankMathTitle.contains("Rank Math"))
  }
}


