package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class ElementorPageType(val persianTitle: String, val description: String) {
    HOME_BRANDING("صفحه اصلی و برندینگ", "طراحی لوکس، بنر قهرمان، مزیت‌های رقابتی و دسته‌بندی‌های برتر"),
    PRODUCT_LANDING("لندینگ معرفی و فروش محصول", "صفحه فرود با تمرکز بر تبدیل، ویژگی‌ها، ویدیو و نظرات خریداران"),
    ABOUT_US("درباره ما و اصالت برند", "معرفی داستان برند، گواهی‌های استاندارد، تیم و تضمین اصالت کالا"),
    PROMO_CAMPAIGN("جشنواره و تخفیف شگفت‌انگیز", "شمارنده معکوس، آکاردئون پرسش‌های متداول و دکمه‌های خرید مستقیم"),
    CONTACT_SUPPORT("تماس و پشتیبانی مشتریان", "فرم مشاوره، اطلاعات تماس، نقشه و ساعات پاسخگویی")
}

data class ElementorSectionDesign(
    val id: String = UUID.randomUUID().toString().take(8),
    val sectionTitle: String,
    val sectionType: String, // hero, features, products_grid, testimonials, faq, cta
    val heading: String,
    val subHeading: String,
    val bodyText: String,
    val buttonText: String = "",
    val buttonLink: String = "#",
    val items: List<String> = emptyList(),
    val backgroundColor: String = "#ffffff"
)

data class ElementorPageDesign(
    val title: String,
    val pageType: ElementorPageType,
    val sections: List<ElementorSectionDesign>,
    val elementorJson: String,
    val semanticHtml: String,
    val recommendedRankMathTitle: String,
    val recommendedMetaDesc: String
)

object ElementorTemplateBuilder {

    fun generateDesignForPage(
        pageTitle: String,
        pageType: ElementorPageType,
        brandKnowledge: BrandKnowledgeBase
    ): ElementorPageDesign {
        val brandName = brandKnowledge.storeName.ifBlank { "گلی کالا" }
        val advantage = "تضمین صد درصدی اصالت کالا و ارسال سریع به سراسر کشور"

        val sections = when (pageType) {
            ElementorPageType.HOME_BRANDING -> listOf(
                ElementorSectionDesign(
                    sectionTitle = "بخش قهرمان اصلی (Hero Section)",
                    sectionType = "hero",
                    heading = "زیبایی و سلامت پوست با برترین محصولات اصل جهان",
                    subHeading = brandName,
                    bodyText = "در $brandName با خیالی آسوده از اصالت، کیفیت و تاریخ انقضای معتبر خرید کنید. $advantage",
                    buttonText = "مشاهده جدیدترین محصولات",
                    buttonLink = "/shop",
                    backgroundColor = "#F8FAFC"
                ),
                ElementorSectionDesign(
                    sectionTitle = "مزایای رقابتی و اعتمادسازی",
                    sectionType = "features",
                    heading = "چرا مشتریان $brandName را انتخاب می‌کنند؟",
                    subHeading = "استانداردهای طلایی ما",
                    bodyText = "تعهد بی‌قید و شرط به سلامت مصرف‌کننده، اصالت کالا و رضایت پایدار مشتریان.",
                    items = listOf(
                        "ضمانت صد در صدی بازگشت وجه در صورت عدم اصالت",
                        "مشاوره تخصصی و رایگان انتخاب محصول مناسب با پوست",
                        "ارسال فوری و بسته‌بندی کاملاً بهداشتی و ضربه‌گیر",
                        "تنوع بی‌نظیر بهترین برندهای تخصصی پوست و مو"
                    ),
                    backgroundColor = "#FFFFFF"
                ),
                ElementorSectionDesign(
                    sectionTitle = "پرسش‌های متداول مشتریان (FAQ)",
                    sectionType = "faq",
                    heading = "پاسخ به سوالات متداول شما",
                    subHeading = "شفافیت و راهنمایی",
                    bodyText = "پاسخ به پرتکرارترین پرسش‌های کاربران درباره سفارش، اصالت و ارسال.",
                    items = listOf(
                        "چگونه از اصالت محصولات مطمئن شویم؟ | تمام محصولات دارای بارکد اصالت و رهگیری بین‌المللی هستند.",
                        "زمان ارسال به شهرستان‌ها چند روز است؟ | سفارش‌ها ظرف ۲۴ تا ۴۸ ساعت کاری با پست پیشتاز تحویل می‌شوند.",
                        "آیا امکان دریافت مشاوره قبل از خرید وجود دارد؟ | بله، کارشناسان ما از طریق چت آنلاین و تماس آماده پاسخگویی هستند."
                    ),
                    backgroundColor = "#F1F5F9"
                ),
                ElementorSectionDesign(
                    sectionTitle = "فراخوان به اقدام پایانی (Final CTA)",
                    sectionType = "cta",
                    heading = "به جمع بیش از ده هزار مشتری راضی $brandName بپیوندید",
                    subHeading = "تخفیف ویژه سفارش اول",
                    bodyText = "همین حالا با کد تخفیف FIRST10 از ۱۰ درصد تخفیف ویژه اولین خرید بهره‌مند شوید.",
                    buttonText = "شروع خرید هوشمندانه",
                    buttonLink = "/shop",
                    backgroundColor = "#1E293B"
                )
            )

            ElementorPageType.PRODUCT_LANDING -> listOf(
                ElementorSectionDesign(
                    sectionTitle = "معرفی محصول شاخص",
                    sectionType = "hero",
                    heading = pageTitle,
                    subHeading = "فرمولاسیون پیشرفته و اثربخشی اثبات‌شده",
                    bodyText = "راهکاری تخصصی برای رفع نیازهای مراقبتی پوست با بالاترین استانداردهای جهانی.",
                    buttonText = "افزودن به سبد خرید با گارانتی اصالت",
                    buttonLink = "#order",
                    backgroundColor = "#F0FDF4"
                ),
                ElementorSectionDesign(
                    sectionTitle = "مشخصات و ترکیبات فعال",
                    sectionType = "features",
                    heading = "ترکیبات منحصربه‌فرد و فرمول علمی",
                    subHeading = "اثربخشی بالینی",
                    bodyText = "بدون پارابن، الکل و مواد حساسیت‌زا با تاییدیه متخصصان درماتولوژی.",
                    items = listOf(
                        "هیالورونیک اسید چندوزنی برای نفوذ به عمیق‌ترین لایه‌های اپیدرم",
                        "نیاسینامید خالص جهت یکنواخت کردن بافت پوست و رفع قرمزی",
                        "ویتامین B5 برای ترمیم سد دفاعی و التیام‌بخشی سریع"
                    ),
                    backgroundColor = "#FFFFFF"
                ),
                ElementorSectionDesign(
                    sectionTitle = "نظرات و تجربیات مصرف‌کنندگان",
                    sectionType = "testimonials",
                    heading = "نظر خریداران واقعی این محصول",
                    subHeading = "رضایت ۹۶ درصدی مشتریان",
                    bodyText = "بازخورد کاربران پس از ۴ هفته مصرف منظم این محصول.",
                    items = listOf(
                        "سارا م. (تهران): پس از دو هفته استفاده منظم، خشکی پوستم کاملاً برطرف شد و حس بسیار شادابی دارم.",
                        "مهندس ناصری (اصفهان): اصالت کالا و بسته‌بندی عالی بود. حتماً خرید بعدی را از همین فروشگاه انجام می‌دهم.",
                        "دکتر کیانی (شیراز): یکی از بهترین فرمولاسیون‌های موجود در بازار با جذب بسیار سریع و سبک."
                    ),
                    backgroundColor = "#F8FAFC"
                ),
                ElementorSectionDesign(
                    sectionTitle = "دعوت به خرید فوری",
                    sectionType = "cta",
                    heading = "فرصت خرید با قیمت استثنایی و ارسال رایگان",
                    subHeading = "تعداد محدود در انبار",
                    bodyText = "همراه با ضمانت اصالت فیزیکی و ۷ روز مهلت بازگشت کالا.",
                    buttonText = "ثبت سفارش آنی",
                    buttonLink = "#order",
                    backgroundColor = "#0F172A"
                )
            )

            ElementorPageType.ABOUT_US -> listOf(
                ElementorSectionDesign(
                    sectionTitle = "داستان پیدایش و هویت برند",
                    sectionType = "hero",
                    heading = "درباره فروشگاه تخصصی $brandName",
                    subHeading = "اصالت، کیفیت و سلامتی",
                    bodyText = "ما با یک باور ساده آغاز کردیم: هر فردی سزاوار دسترسی به اصیل‌ترین و موثرترین محصولات مراقبتی پوست است، بدون نگرانی از کالاهای فیک یا تاریخ گذشته.",
                    buttonText = "تماس با کارشناسان ما",
                    buttonLink = "/contact-us",
                    backgroundColor = "#F8FAFC"
                ),
                ElementorSectionDesign(
                    sectionTitle = "ارزش‌های کلیدی و منشور اخلاقی",
                    sectionType = "features",
                    heading = "اصولی که به آن وفاداریم",
                    subHeading = "تعهد اخلاقی و حرفه‌ای",
                    bodyText = "در $brandName هرگز کیفیت را فدای سود کوتاه‌مدت نکرده‌ایم.",
                    items = listOf(
                        "تامین انحصاری از مبادی رسمی و واردکنندگان دارای مجوز بهداشت",
                        "نگهداری استاندارد در دمای کنترل‌شده انبار مرکزی برای حفظ کارایی مواد",
                        "صداقت در مشاوره و پرهیز از ادعاهای مبالغه‌آمیز در تبلیغات",
                        "ارتقای آگاهی جامعه نسبت به روتین‌های مراقبتی صحیح"
                    ),
                    backgroundColor = "#FFFFFF"
                ),
                ElementorSectionDesign(
                    sectionTitle = "پیام مدیریت",
                    sectionType = "testimonials",
                    heading = "تعهد ما به سلامتی و رضایت شماست",
                    subHeading = "مدیریت فروشگاه $brandName",
                    bodyText = "«اعتماد شما سرمایه‌ای است که با سال‌ها پاسخگویی دقیق و کیفیت بی‌نقص به دست آمده است. همواره در تلاشیم تا تجربه‌ای شیرین از خرید اینترنتی برایتان رقم بزنیم.»",
                    backgroundColor = "#EEF2FF"
                )
            )

            ElementorPageType.PROMO_CAMPAIGN -> listOf(
                ElementorSectionDesign(
                    sectionTitle = "بنر شگفت‌انگیز جشنواره",
                    sectionType = "hero",
                    heading = "جشنواره ویژه شگفتی‌های $brandName",
                    subHeading = "تخفیف تا ۴۰ درصد بر روی منتخب برندها",
                    bodyText = "فرصت طلایی نوسازی روتین مراقبتی با بهترین قیمت‌های سال. فقط تا پایان این هفته!",
                    buttonText = "مشاهده کالاهای شگفت‌انگیز",
                    buttonLink = "/sale",
                    backgroundColor = "#FFF1F2"
                ),
                ElementorSectionDesign(
                    sectionTitle = "هدایای ویژه سفارش‌ها",
                    sectionType = "features",
                    heading = "هدایای رایگان با هر سفارش جشنواره",
                    subHeading = "پاداش وفاداری",
                    bodyText = "به پاس همراهی گرم شما، برای خریدهای بالای یک میلیون تومان هدایای نفیس ارسال می‌شود.",
                    items = listOf(
                        "یک عدد ماسک ورقه‌ای آبرسان کره‌ای رایگان",
                        "ارسال کاملاً رایگان با پست پیشتاز فوری",
                        "کارت تخفیف ۵۰ هزار تومانی برای خرید بعدی"
                    ),
                    backgroundColor = "#FFFFFF"
                ),
                ElementorSectionDesign(
                    sectionTitle = "شمارش معکوس و پایان جشنواره",
                    sectionType = "cta",
                    heading = "همین حالا سبد خرید خود را تکمیل کنید",
                    subHeading = "موجودی محدود در انبار مرکزی",
                    bodyText = "با توجه به استقبال گسترده، موجودی برخی اقلام رو به پایان است.",
                    buttonText = "ورود به سبد خرید",
                    buttonLink = "/cart",
                    backgroundColor = "#991B1B"
                )
            )

            ElementorPageType.CONTACT_SUPPORT -> listOf(
                ElementorSectionDesign(
                    sectionTitle = "بخش خوش‌آمدگویی و پشتیبانی",
                    sectionType = "hero",
                    heading = "همواره مشتاق شنیدن صدای گرم شما هستیم",
                    subHeading = "پشتیبانی و مشاوره تخصصی $brandName",
                    bodyText = "کارشناسان ما در تمامی روزهای کاری از ساعت ۹ الی ۲۱ آماده ارائه مشاوره رایگان انتخاب محصول و پیگیری سفارش‌های شما هستند.",
                    buttonText = "ارسال پیام در واتساپ",
                    buttonLink = "https://wa.me/989120000000",
                    backgroundColor = "#F8FAFC"
                ),
                ElementorSectionDesign(
                    sectionTitle = "راه‌های ارتباطی مستقیم",
                    sectionType = "features",
                    heading = "اطلاعات تماس مستقیم",
                    subHeading = "کانال‌های رسمی",
                    bodyText = "سریع‌ترین روش‌های دسترسی به تیم پشتیبانی $brandName:",
                    items = listOf(
                        "تلفن تماس مستقیم پشتیبانی: ۰۲۱-۸۸۸۸۰۰۰۰",
                        "پست الکترونیکی واحد فروش: info@${brandKnowledge.storeName.replace(" ", "").lowercase()}.ir",
                        "نشانی دفتر مرکزی و انبار پردازش: تهران، خیابان ولیعصر، برج تجارت، طبقه ۵",
                        "پاسخگویی سریع در تلگرام و اینستاگرام: @$brandName"
                    ),
                    backgroundColor = "#FFFFFF"
                )
            )
        }

        val elementorJson = buildElementorDataJson(sections)
        val semanticHtml = buildSemanticHtml(pageTitle, sections)
        val rankMathTitle = "$pageTitle | $brandName".take(60)
        val metaDesc = "صفحه رسمی $pageTitle در $brandName. $advantage".take(150)

        return ElementorPageDesign(
            title = pageTitle,
            pageType = pageType,
            sections = sections,
            elementorJson = elementorJson,
            semanticHtml = semanticHtml,
            recommendedRankMathTitle = rankMathTitle,
            recommendedMetaDesc = metaDesc
        )
    }

    private fun buildElementorDataJson(sections: List<ElementorSectionDesign>): String {
        val rootArray = JSONArray()

        for (sec in sections) {
            val sectionObj = JSONObject().apply {
                put("id", sec.id)
                put("elType", "section")
                put("isInner", false)
                put("settings", JSONObject().apply {
                    put("background_background", "classic")
                    put("background_color", sec.backgroundColor)
                    put("padding", JSONObject().apply {
                        put("unit", "px")
                        put("top", "60")
                        put("bottom", "60")
                        put("left", "20")
                        put("right", "20")
                        put("isLinked", false)
                    })
                })

                val colArray = JSONArray()
                val colObj = JSONObject().apply {
                    put("id", UUID.randomUUID().toString().take(8))
                    put("elType", "column")
                    put("isInner", false)
                    put("settings", JSONObject().apply {
                        put("_column_size", 100)
                    })

                    val widgetsArray = JSONArray()

                    // Subheading Widget
                    if (sec.subHeading.isNotBlank()) {
                        widgetsArray.put(JSONObject().apply {
                            put("id", UUID.randomUUID().toString().take(8))
                            put("elType", "widget")
                            put("widgetType", "heading")
                            put("settings", JSONObject().apply {
                                put("title", sec.subHeading)
                                put("header_size", "h4")
                                put("align", "center")
                                put("title_color", "#2563EB")
                            })
                        })
                    }

                    // Main Heading Widget
                    widgetsArray.put(JSONObject().apply {
                        put("id", UUID.randomUUID().toString().take(8))
                        put("elType", "widget")
                        put("widgetType", "heading")
                        put("settings", JSONObject().apply {
                            put("title", sec.heading)
                            put("header_size", "h2")
                            put("align", "center")
                            put("title_color", if (sec.backgroundColor == "#1E293B" || sec.backgroundColor == "#0F172A" || sec.backgroundColor == "#991B1B") "#FFFFFF" else "#0F172A")
                        })
                    })

                    // Body Text Widget
                    if (sec.bodyText.isNotBlank()) {
                        widgetsArray.put(JSONObject().apply {
                            put("id", UUID.randomUUID().toString().take(8))
                            put("elType", "widget")
                            put("widgetType", "text-editor")
                            put("settings", JSONObject().apply {
                                put("editor", "<p style='text-align: center; font-size: 16px; line-height: 1.8; color: ${if (sec.backgroundColor.contains("1") || sec.backgroundColor.contains("9")) "#E2E8F0" else "#475569"};'>${sec.bodyText}</p>")
                            })
                        })
                    }

                    // Items / Features / Accordion Widget
                    if (sec.items.isNotEmpty()) {
                        if (sec.sectionType == "faq") {
                            val accordionItems = JSONArray()
                            for (item in sec.items) {
                                val parts = item.split("|")
                                val q = parts.getOrNull(0)?.trim() ?: item
                                val a = parts.getOrNull(1)?.trim() ?: "توضیحات تکمیلی..."
                                accordionItems.put(JSONObject().apply {
                                    put("tab_title", q)
                                    put("tab_content", a)
                                })
                            }
                            widgetsArray.put(JSONObject().apply {
                                put("id", UUID.randomUUID().toString().take(8))
                                put("elType", "widget")
                                put("widgetType", "accordion")
                                put("settings", JSONObject().apply {
                                    put("tabs", accordionItems)
                                    put("icon_align", "right")
                                })
                            })
                        } else {
                            val listHtml = StringBuilder("<ul style='max-width: 600px; margin: 20px auto; list-style-type: none; padding: 0;'>")
                            for (it in sec.items) {
                                listHtml.append("<li style='padding: 10px 16px; margin-bottom: 8px; background: rgba(0,0,0,0.03); border-radius: 8px; font-size: 14px;'>✓ $it</li>")
                            }
                            listHtml.append("</ul>")
                            widgetsArray.put(JSONObject().apply {
                                put("id", UUID.randomUUID().toString().take(8))
                                put("elType", "widget")
                                put("widgetType", "text-editor")
                                put("settings", JSONObject().apply {
                                    put("editor", listHtml.toString())
                                })
                            })
                        }
                    }

                    // Button CTA Widget
                    if (sec.buttonText.isNotBlank()) {
                        widgetsArray.put(JSONObject().apply {
                            put("id", UUID.randomUUID().toString().take(8))
                            put("elType", "widget")
                            put("widgetType", "button")
                            put("settings", JSONObject().apply {
                                put("text", sec.buttonText)
                                put("link", JSONObject().apply { put("url", sec.buttonLink) })
                                put("align", "center")
                                put("size", "md")
                                put("button_text_color", "#FFFFFF")
                                put("background_color", "#2563EB")
                                put("border_radius", JSONObject().apply {
                                    put("unit", "px")
                                    put("top", "8")
                                    put("bottom", "8")
                                    put("left", "8")
                                    put("right", "8")
                                })
                            })
                        })
                    }

                    put("elements", widgetsArray)
                }

                colArray.put(colObj)
                put("elements", colArray)
            }

            rootArray.put(sectionObj)
        }

        return rootArray.toString()
    }

    private fun buildSemanticHtml(title: String, sections: List<ElementorSectionDesign>): String {
        val sb = StringBuilder()
        sb.append("<!-- طراحی شده اختصاصی با افزونه المنتور گلیسئو -->\n")
        sb.append("<div class=\"elementor elementor-custom-page\" dir=\"rtl\">\n")
        for (sec in sections) {
            sb.append("  <section class=\"elementor-section elementor-section-boxed\" style=\"background-color: ${sec.backgroundColor}; padding: 40px 16px;\">\n")
            sb.append("    <div class=\"elementor-container\" style=\"max-width: 900px; margin: 0 auto; text-align: center;\">\n")
            if (sec.subHeading.isNotBlank()) {
                sb.append("      <h4 style=\"color: #2563EB; margin-bottom: 8px;\">${sec.subHeading}</h4>\n")
            }
            sb.append("      <h2 style=\"margin-bottom: 16px; font-weight: bold;\">${sec.heading}</h2>\n")
            if (sec.bodyText.isNotBlank()) {
                sb.append("      <p style=\"font-size: 16px; line-height: 1.8; margin-bottom: 24px;\">${sec.bodyText}</p>\n")
            }
            if (sec.items.isNotEmpty()) {
                sb.append("      <div style=\"text-align: right; max-width: 600px; margin: 20px auto;\">\n")
                for (it in sec.items) {
                    sb.append("        <div style=\"padding: 10px; margin-bottom: 8px; background: rgba(0,0,0,0.03); border-radius: 6px;\">✦ $it</div>\n")
                }
                sb.append("      </div>\n")
            }
            if (sec.buttonText.isNotBlank()) {
                sb.append("      <a href=\"${sec.buttonLink}\" style=\"display: inline-block; padding: 12px 28px; background-color: #2563EB; color: #fff; text-decoration: none; border-radius: 8px; font-weight: bold; margin-top: 16px;\">${sec.buttonText}</a>\n")
            }
            sb.append("    </div>\n")
            sb.append("  </section>\n")
        }
        sb.append("</div>\n")
        return sb.toString()
    }
}
