package com.example

import com.example.data.model.BrandKnowledgeBase
import com.example.data.model.ContentModuleProcessor
import com.example.data.model.ContentValidator
import com.example.data.model.JalaliCalendarHelper
import com.example.data.model.PersianNormalizer
import com.example.data.model.ProductArchetype
import com.example.data.model.SiteItem
import com.example.data.model.SiteItemType
import com.example.data.model.ZeroContradictionGuard
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

  @Test
  fun testPorcozDetergentCategoryIsolationAndSpecs() {
    val porcozItem = SiteItem(
      id = 109,
      title = "رسوب‌زدا و جرم‌گیر شیرآلات و سطوح پورچز (Porçöz) اصل ترکیه",
      content = "محلول رسوب‌زدا و جرم‌گیر پرقدرت پورچز ساخت ترکیه برای تمیز کردن شیرآلات و رسوب کتری.",
      slug = "porcoz-limescale-remover",
      categories = listOf("شوینده و نظافت", "جرم گیر"),
      imageAlt = "بطری قرمز رسوب‌زدا و جرم‌گیر پورچز اصل ساخت ترکیه",
      type = SiteItemType.PRODUCT
    )

    // 1. Smart Initial Analysis
    val analysis = ContentModuleProcessor.analyzeProductBeforeWriting(porcozItem)
    assertEquals("پورچز (Porçöz)", analysis.detectedBrand)
    assertTrue("Origin country must be Turkey", analysis.originCountry.contains("ترکیه"))
    assertTrue("Must flag as detergent/cleaner", analysis.isDetergentOrCleaner)
    assertFalse("Must NOT be official agency brand", analysis.isOfficialAgencyBrand)

    // 2. Package generation & 100% Category Alignment (Zero cross-contamination)
    val pkg = ContentModuleProcessor.generateProductPackage(porcozItem, BrandKnowledgeBase())

    // CRITICAL: Absolutely NO hair/shampoo/scalp/facial terms
    val fullTextToCheck = pkg.shortDescription + " " + pkg.fullDescription + " " +
        pkg.features.joinToString(" ") + " " + pkg.safetyWarning + " " +
        pkg.structuredHtmlContent

    assertFalse("Must NOT contain shampoo", fullTextToCheck.contains("شامپو"))
    assertFalse("Must NOT contain hair", fullTextToCheck.contains("موهای") || fullTextToCheck.contains("ساقه مو"))
    assertFalse("Must NOT contain scalp", fullTextToCheck.contains("پوست سر"))
    assertFalse("Must NOT contain keratin", fullTextToCheck.contains("کراتین"))
    assertFalse("Must NOT contain skincare routine", fullTextToCheck.contains("روتین پوستی"))

    // Must contain cleaning and safety terms
    assertTrue("Must mention limescale/descaling", fullTextToCheck.contains("رسوب") || fullTextToCheck.contains("جرم"))
    assertTrue("Must mention gloves safety precaution", fullTextToCheck.contains("دستکش"))
    assertTrue("Must warn against mixing with bleach (وایتکس)", fullTextToCheck.contains("وایتکس") || fullTextToCheck.contains("سفیدکننده"))

    // 3. Technical Extraction Table: Exact required keys
    val specsMap = pkg.specificationsTable.toMap()
    assertTrue("Specs must contain 'نام و استاندارد کالا'", specsMap.containsKey("نام و استاندارد کالا"))
    assertTrue("Specs must contain 'برند اصلی سازنده'", specsMap.containsKey("برند اصلی سازنده"))
    assertEquals("پورچز (Porçöz)", specsMap["برند اصلی سازنده"])
    assertTrue("Specs must contain 'کشور مبدا و تولید'", specsMap.containsKey("کشور مبدا و تولید"))
    assertTrue("Country must be Turkey", specsMap["کشور مبدا و تولید"]?.contains("ترکیه") == true)
    assertTrue("Specs must contain 'عرضه کننده و مرجع خرید'", specsMap.containsKey("عرضه کننده و مرجع خرید"))
    assertEquals("گلی کالا (واردکننده رسمی و مرجع اصالت کالا)", specsMap["عرضه کننده و مرجع خرید"])

    // 4. Category-appropriate internal links
    assertTrue("Internal links must exist", pkg.internalLinks.isNotEmpty())
    assertTrue(
      "Must link to cleaning category or porcoz brand, NOT hair care",
      pkg.internalLinks.any { it.targetUrl.contains("home-cleaning") || it.targetUrl.contains("porcoz") }
    )
    assertFalse(
      "Must NOT link to hair care for detergent",
      pkg.internalLinks.any { it.targetUrl.contains("hair-care") }
    )
  }

  @Test
  fun testInternalLinksHelperMatchesCategory() {
    val cleaningLinksHtml = ContentModuleProcessor.buildRealGolikalaInternalLinksHtml("جرم‌گیر و رسوب‌زدای پورچز ترکیه")
    assertTrue(cleaningLinksHtml.contains("home-cleaning") || cleaningLinksHtml.contains("porcoz"))
    assertFalse(cleaningLinksHtml.contains("hair-care"))

    val hairLinksHtml = ContentModuleProcessor.buildRealGolikalaInternalLinksHtml("ماسک مو کراتینه آمالفی اسپانیا")
    assertTrue(hairLinksHtml.contains("hair-care") || hairLinksHtml.contains("amalfi"))
  }

  @Test
  fun testTemplateMatchingBanArchetypeDifferentiation() {
    val shampooItem = SiteItem(
      id = 201,
      title = "شامپو بدون سولفات کراتینه آگرادو (Agrado) اصل اسپانیا",
      content = "شامپو موهای کراتینه‌شده و آسیب‌دیده با فرمول ملایم اروپایی",
      slug = "agrado-keratin-shampoo",
      categories = listOf("مراقبت مو", "شامپو"),
      type = SiteItemType.PRODUCT
    )

    val hairMaskItem = SiteItem(
      id = 202,
      title = "ماسک مو ترمیم‌کننده عمقی آمالفی (Amalfi) اصل اسپانیا",
      content = "ماسک مو احیاکننده و نرم‌کننده ساقه موهای رنگ‌شده و دکلره",
      slug = "amalfi-restoring-hair-mask",
      categories = listOf("مراقبت مو", "ماسک مو"),
      type = SiteItemType.PRODUCT
    )

    val sunscreenItem = SiteItem(
      id = 203,
      title = "کرم ضد آفتاب و ضد لک فاقد چربی لافارر SPF 50",
      content = "کرم ضد آفتاب با محافظت حداکثری از پوست در برابر اشعه‌های خورشید",
      slug = "lafarrerr-sunscreen-spf50",
      categories = listOf("مراقبت پوست", "ضد آفتاب"),
      type = SiteItemType.PRODUCT
    )

    val intimateGelItem = SiteItem(
      id = 204,
      title = "ژل بهداشتی بانوان هیدرودرم با تنظیم pH فیزیولوژیک",
      content = "شوینده بسیار ملایم بهداشتی با خاصیت ضد التهاب و رفع بوی نامطبوع",
      slug = "hydroderm-intimate-gel",
      categories = listOf("بهداشت فردی", "ژل بهداشتی"),
      type = SiteItemType.PRODUCT
    )

    val shampooPkg = ContentModuleProcessor.generateProductPackage(shampooItem, BrandKnowledgeBase())
    val maskPkg = ContentModuleProcessor.generateProductPackage(hairMaskItem, BrandKnowledgeBase())
    val sunscreenPkg = ContentModuleProcessor.generateProductPackage(sunscreenItem, BrandKnowledgeBase())
    val intimatePkg = ContentModuleProcessor.generateProductPackage(intimateGelItem, BrandKnowledgeBase())

    // 1. Shampoo Verification (Strictly Scalp & Hair Washing)
    assertTrue("Shampoo must mention hair washing or scalp", shampooPkg.shortDescription.contains("مو") || shampooPkg.shortDescription.contains("سر"))
    assertFalse("Shampoo short desc must NOT copy hair mask text", shampooPkg.shortDescription.contains("ماسک مو"))
    assertFalse("Shampoo must not suggest leave-in bath mask", shampooPkg.fullDescription.contains("ماسک را منحصراً") || shampooPkg.fullDescription.contains("ماسک موی تخصصی"))
    assertTrue("Shampoo callout box must be hair wash specific", shampooPkg.structuredHtmlContent.contains("شستشوی مو"))

    // 2. Hair Mask Verification (Strictly Mid-lengths to Ends, NOT Scalp Washing)
    assertTrue("Mask must mention lengths or repair", maskPkg.shortDescription.contains("ساقه") || maskPkg.shortDescription.contains("نوک مو") || maskPkg.shortDescription.contains("کوتیکول"))
    assertTrue("Mask must specify leave-in time 3 to 5 minutes", maskPkg.fullDescription.contains("۵ دقیقه") || maskPkg.fullDescription.contains("۳ تا ۵"))
    assertTrue("Mask callout must warn against applying on scalp", maskPkg.structuredHtmlContent.contains("ریشه و پوست سر") || maskPkg.structuredHtmlContent.contains("احیای مو"))

    // 3. Sunscreen Verification (Strictly UV Protection, SPF, 2-hour reapplication)
    assertTrue("Sunscreen must mention UVA/UVB or sun", sunscreenPkg.shortDescription.contains("آفتاب") || sunscreenPkg.shortDescription.contains("UVA"))
    assertTrue("Sunscreen full desc must mention reapplication", sunscreenPkg.fullDescription.contains("تمدید") || sunscreenPkg.fullDescription.contains("ساعت"))
    assertFalse("Sunscreen must not have detergent or hair keywords", sunscreenPkg.shortDescription.contains("رسوب") || sunscreenPkg.shortDescription.contains("شامپو"))

    // 4. Intimate Hygiene Verification (Strictly physiological pH & external use)
    assertTrue("Intimate gel must mention pH", intimatePkg.shortDescription.contains("pH") || intimatePkg.fullDescription.contains("pH"))
    assertTrue("Intimate gel must specify external use", intimatePkg.safetyWarning.contains("خارجی") || intimatePkg.fullDescription.contains("خارجی"))
    assertFalse("Intimate gel must not mention hair care or descaler", intimatePkg.shortDescription.contains("رسوب") || intimatePkg.shortDescription.contains("ساقه مو"))

    // 5. Fixed Identity Policy for "Golikala"
    // Never brand manufacturer
    assertFalse("Golikala cannot be brand of shampoo", shampooPkg.detectedBrand.startsWith("گلی کالا"))
    assertFalse("Golikala cannot be brand of sunscreen", sunscreenPkg.detectedBrand.startsWith("گلی کالا"))
    // Official Agency for Amalfi & Agrado
    assertTrue("Agrado must be official agency", shampooPkg.isOfficialAgencyBrand)
    assertTrue("Amalfi must be official agency", maskPkg.isOfficialAgencyBrand)
    assertTrue("Agency statement must be explicit", shampooPkg.agencyStatement.contains("نماینده رسمی و انحصاری"))
    // Official importer for Lafarrerr
    assertFalse("Lafarrerr is not agency brand", sunscreenPkg.isOfficialAgencyBrand)
    assertTrue("Must be official importer", sunscreenPkg.agencyStatement.contains("واردکننده رسمی و مرجع اصالت کالا"))

    // 6. Complete Non-Contradictory Technical Table (All 9 keys present)
    for (pkg in listOf(shampooPkg, maskPkg, sunscreenPkg, intimatePkg)) {
      val specs = pkg.specificationsTable.toMap()
      assertTrue(specs.containsKey("نام و استاندارد کالا"))
      assertTrue(specs.containsKey("برند اصلی سازنده"))
      assertTrue(specs.containsKey("کشور مبدا و تولید"))
      assertTrue(specs.containsKey("عرضه کننده و مرجع خرید"))
      assertTrue(specs.containsKey("نوع محصول و فرمولاسیون"))
      assertTrue(specs.containsKey("بافت هدف و سطوح مصرف"))
      assertTrue(specs.containsKey("روش و الگوی مصرف"))
      assertTrue(specs.containsKey("نکات ایمنی و بهداشتی"))
      assertTrue(specs.containsKey("اصالت و گارانتی خرید"))
    }
  }

  @Test
  fun testVisionAndOcrAnalysisPipeline() {
    val lorealItem = SiteItem(
      id = 110,
      title = "شامپو آبرسان لورال Loreal Elseve بنفش هیالورونیک اسید اصل فرانسه",
      content = "شامپو هیالورونیک اسید السیو بنفش لورال فرانسه",
      slug = "loreal-elseve-hyaluron-purple-shampoo",
      imageAlt = "بطری بنفش شامپو آبرسان لورال السیو حاوی هیالورونیک اسید اصل فرانسه",
      type = SiteItemType.PRODUCT
    )

    val vision = ContentModuleProcessor.analyzeProductVisionAndText(lorealItem)
    assertTrue("Form factor must detect purple shampoo bottle", vision.packagingFormFactor.contains("بنفش") && vision.packagingFormFactor.contains("شامپو"))
    assertTrue("OCR labels must include Loreal and Elseve", vision.ocrDetectedLabels.any { it.contains("L'Oréal") || it.contains("Elseve") })
    assertTrue("Dominant colors must detect purple", vision.visualDominantColors.contains("بنفش"))
    assertTrue("Category cues must confirm scalp/hair wash", vision.visualCategoryCues.contains("شامپو") || vision.visualCategoryCues.contains("کف سر"))
    assertEquals(99, vision.confidenceScore)

    val porcozItem = SiteItem(
      id = 109,
      title = "رسوب‌زدا و جرم‌گیر شیرآلات و سطوح پورچز (Porçöz) اصل ترکیه",
      content = "محلول رسوب‌زدا و جرم‌گیر پرقدرت پورچز ساخت کمپانی لونت کیمیا ترکیه",
      slug = "porcoz-descaler-turkey",
      imageAlt = "بطری قرمز رسوب‌زدا و جرم‌گیر پورچز اصل ساخت ترکیه",
      type = SiteItemType.PRODUCT
    )

    val porcozVision = ContentModuleProcessor.analyzeProductVisionAndText(porcozItem)
    assertTrue("Porcoz form factor must detect red bottle and safety cap", porcozVision.packagingFormFactor.contains("قرمز") && porcozVision.packagingFormFactor.contains("ایمنی"))
    assertTrue("Porcoz OCR must include Levent Kimya / Porcoz", porcozVision.ocrDetectedLabels.any { it.contains("Porçöz") || it.contains("Levent Kimya") })
  }

  @Test
  fun testLorealElsevePurpleShampooZeroContradiction() {
    val lorealItem = SiteItem(
      id = 110,
      title = "شامپو آبرسان لورال Loreal Elseve بنفش هیالورونیک اسید اصل فرانسه",
      content = "شامپو آبرسان ۷۲ ساعته لورال پاریس فرانسه",
      slug = "loreal-elseve-hyaluron-purple-shampoo",
      imageAlt = "بطری بنفش شامپو آبرسان لورال السیو حاوی هیالورونیک اسید اصل فرانسه",
      type = SiteItemType.PRODUCT
    )

    val preAnalysis = ContentModuleProcessor.analyzeProductBeforeWriting(lorealItem)
    assertEquals(ProductArchetype.HAIR_SHAMPOO, preAnalysis.archetype)
    assertTrue(preAnalysis.detectedBrand.contains("لورآل") || preAnalysis.detectedBrand.contains("L'Oréal"))
    assertTrue(preAnalysis.originCountry.contains("فرانسه"))
    assertFalse("Golikala cannot be the brand", preAnalysis.detectedBrand.contains("گلی کالا"))
    assertTrue("Golikala is official importer", preAnalysis.agencyStatement.contains("واردکننده رسمی و مرجع اصالت کالا"))

    val pkg = ContentModuleProcessor.generateProductPackage(lorealItem)
    // Short desc must be exclusively about hair wash and scalp
    assertTrue(pkg.shortDescription.contains("شامپو") || pkg.shortDescription.contains("تارهای مو"))
    assertFalse("Must NOT contain mask keywords in shampoo short desc", pkg.shortDescription.contains("ماسک مو"))
    // Image metadata must sync
    assertTrue(pkg.imageMetadata?.persianAltText?.contains("شامپو") == true)
    assertTrue(pkg.imageMetadata?.persianAltText?.contains("لورآل") == true || pkg.imageMetadata?.persianAltText?.contains("لورال") == true)
    // Zero Contradiction Guard
    assertTrue("Must pass zero contradiction verification", pkg.contradictionAudit?.isZeroContradictionVerified == true)
    assertFalse("No self-correction needed for valid shampoo", pkg.contradictionAudit?.selfCorrectionApplied == true)
  }

  @Test
  fun testZeroContradictionGuardCatchesAndSelfCorrects() {
    val lorealItem = SiteItem(
      id = 110,
      title = "شامپو آبرسان لورال Loreal Elseve بنفش هیالورونیک اسید اصل فرانسه",
      content = "شامپو آبرسان",
      slug = "loreal-elseve-hyaluron-purple-shampoo",
      type = SiteItemType.PRODUCT
    )

    val preAnalysis = ContentModuleProcessor.analyzeProductBeforeWriting(lorealItem)

    // Intentionally inject cross-category contradiction (calling shampoo a hair mask and instructing 3-5 min bath wait)
    val contaminatedPackage = ContentModuleProcessor.generateProductPackage(lorealItem).copy(
      shortDescription = "ماسک موی تخصصی و کراتینه لورال جهت احیای ساقه و نوک مو در حمام بدون برخورد با کف سر.",
      fullDescription = "این ماسک را منحصراً روی ساقه مو بمالید و ۳ الی ۵ دقیقه مکث کنید."
    )

    val audit = ZeroContradictionGuard.audit(preAnalysis, contaminatedPackage)
    assertTrue("Audit must flag contradictions", audit.identifiedContradictions.isNotEmpty())
    assertTrue("Must flag mask keyword in shampoo", audit.identifiedContradictions.any { it.contains("ماسک") })

    // Self-correct via auditAndSanitize
    val sanitized = ZeroContradictionGuard.auditAndSanitize(preAnalysis, contaminatedPackage, lorealItem)
    assertTrue(sanitized.contradictionAudit?.isZeroContradictionVerified == true)
    assertTrue(sanitized.contradictionAudit?.selfCorrectionApplied == true)
    assertFalse("Sanitized package must not contain mask words in shampoo short desc", sanitized.shortDescription.contains("ماسک مو"))
    assertTrue("Sanitized package must restore hair shampoo nature", sanitized.shortDescription.contains("شامپو"))
  }
}



