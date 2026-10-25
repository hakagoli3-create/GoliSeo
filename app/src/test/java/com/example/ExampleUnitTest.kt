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
  }
}


