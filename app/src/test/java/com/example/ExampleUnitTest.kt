package com.example

import com.example.data.model.BrandKnowledgeBase
import com.example.data.model.ContentModuleProcessor
import com.example.data.model.ContentValidator
import com.example.data.model.JalaliCalendarHelper
import com.example.data.model.PersianNormalizer
import com.example.data.model.SiteItem
import com.example.data.model.SiteItemType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testPersianNormalizer() {
    val raw = "كرم ضد آفتاب مي شود 123"
    val normalized = PersianNormalizer.normalize(raw)
    // Checks kaf replacement, digits to Persian, and half-space
    assertTrue(normalized.contains("کرم"))
    assertTrue(normalized.contains("۱۲۳"))
    assertTrue(normalized.contains("می\u200cشود"))
  }

  @Test
  fun testFreeformCommandBlocksPriceAttempt() {
    val plan = ContentModuleProcessor.parseFreeformCommand("قیمت تمام محصولات را ۵۰ هزار تومان کم کن")
    assertFalse(plan.isSafeFromHardLock)
    assertTrue(plan.blockedReason?.contains("قیمت") == true)
  }

  @Test
  fun testFreeformCommandAllowsSafeSeo() {
    val plan = ContentModuleProcessor.parseFreeformCommand("توضیحات متا و عنوان سئو مقالات را استاندارد کن")
    assertTrue(plan.isSafeFromHardLock)
    assertEquals(3, plan.steps.size)
  }

  @Test
  fun testContentValidatorBansMedicalHype() {
    val result = ContentValidator.validate(
      title = "کرم ضد لک قوی با بهترین کیفیت و گارانتی",
      content = "این کرم درمان قطعی لک‌های صورت در ۳ روز است و معجزه پزشکی به شمار می‌رود.",
      metaDescription = "خرید کرم ضد لک عالی با ضمانت اصالت و بازگشت وجه در فروشگاه گلی کالا.",
      focusKeyword = "کرم ضد لک",
      isHygieneOrDetergent = false
    )
    assertFalse("Medical claims must fail validation", result.isValid)
    assertTrue(result.reviewReasons.any { it.contains("پزشکی") })
  }

  @Test
  fun testContentValidatorDetectsCannibalization() {
    val existing = listOf("کرم آبرسان صورت", "روتین پوستی")
    val result = ContentValidator.validate(
      title = "راهنمای خرید کرم آبرسان صورت برای انواع پوست",
      content = "کرم آبرسان صورت یکی از ملزومات روزانه است. برای رطوبت پوست عالی است و دور از دسترس کودکان نگهداری شود.",
      metaDescription = "بررسی مشخصات و راهنمای انتخاب کرم آبرسان صورت با اصالت کالا.",
      focusKeyword = "کرم آبرسان صورت",
      isHygieneOrDetergent = true,
      existingKeywordsOnSite = existing
    )
    assertTrue(result.reviewReasons.any { it.contains("هم‌نوع‌خواری") })
  }

  @Test
  fun testJalaliFutureScheduleFormatting() {
    val future = JalaliCalendarHelper.createWpFutureDate(5)
    assertTrue(future.first.contains("T")) // ISO GMT date
    assertTrue(future.second.contains("۱۴۰")) // Jalali year 140x
  }

  @Test
  fun testProductPackageGeneration() {
    val sampleItem = SiteItem(
      id = 1,
      title = "سرم آبرسان پوست",
      content = "حاوی هیالورونیک اسید برای نرمی پوست",
      slug = "hyaluronic-serum",
      type = SiteItemType.PRODUCT
    )
    val pkg = ContentModuleProcessor.generateProductPackage(sampleItem, BrandKnowledgeBase())
    assertTrue(pkg.seoTitle.contains("گلی کالا"))
    assertTrue(pkg.faqs.isNotEmpty())
    assertTrue(pkg.safetyWarning.isNotBlank())
    assertTrue(pkg.specificationsTable.isNotEmpty())
    assertTrue(pkg.internalLinks.isNotEmpty())
  }

  @Test
  fun testCompetitorAnalyzerReturnsStructuredIntelligence() {
    val sampleProduct = SiteItem(
      id = 101,
      title = "شامپو کراتینه و تقویت‌کننده آگرادو اصل اسپانیا",
      content = "شامپو ترمیم کننده موهای آسیب دیده",
      slug = "agrado-keratin-shampoo",
      focusKeyword = "شامپو آگرادو اصل اسپانیا",
      type = SiteItemType.PRODUCT
    )
    val report = com.example.data.model.GoogleCompetitorAnalyzer.analyze(sampleProduct, BrandKnowledgeBase())
    assertEquals("شامپو آگرادو اصل اسپانیا", report.targetKeyword)
    assertEquals(3, report.topCompetitors.size)
    assertTrue("Should include Digikala competitor", report.topCompetitors.any { competitor -> competitor.domain.contains("digikala") })
    assertTrue("Content gaps must be identified", report.contentGaps.isNotEmpty())
    assertTrue("LSI keywords must be provided", report.suggestedLsiKeywords.isNotEmpty())
    assertTrue("Winning strategy must be generated", report.winningStrategy.isNotBlank())
  }

  @Test
  fun testAdvancedContentRewriteEliminatesWallOfText() {
    val sampleProduct = SiteItem(
      id = 102,
      title = "ماسک مو ترمیم‌کننده آمالفی فاقد سولفات",
      content = "این یک متن طولانی است که به صورت دیوار متنی نوشته شده و هیچ ساختار یا سرفصلی ندارد تا تست شود.",
      slug = "amalfi-hair-mask",
      focusKeyword = "ماسک مو آمالفی",
      type = SiteItemType.PRODUCT
    )
    val pkg = ContentModuleProcessor.generateProductPackage(sampleProduct, BrandKnowledgeBase())
    val structuredHtml = pkg.structuredHtmlContent

    // 1. Pre-Writing Analysis & Official Agency
    assertTrue("Must detect official agency brand", pkg.isOfficialAgencyBrand)
    assertEquals("آمالفی (Amalfi)", pkg.detectedBrand)
    assertEquals("اسپانیا (اتحادیه اروپا)", pkg.originCountry)
    assertTrue("Must assert official agency", pkg.agencyStatement.contains("نماینده رسمی و انحصاری محصولات برندهای آمالفی، آگرادو و سایرو"))

    // 2. Structure & Anti-wall of text
    assertTrue("Must have H2 headings", structuredHtml.contains("<h2"))
    assertTrue("Must have specifications table", structuredHtml.contains("<table"))
    assertTrue("Must have bullet points", structuredHtml.contains("✔") || structuredHtml.contains("<li"))

    // 3. Real Internal Linking with golikala.com
    assertTrue("Must have internal links section", structuredHtml.contains("gliseo-internal-links-section"))
    assertTrue("Must link to real golikala.com category or brand", structuredHtml.contains("https://golikala.com/"))
    assertTrue("Must have brand reference", structuredHtml.contains("آمالفی"))
  }
}



