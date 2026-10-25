package com.example.data.model

/**
 * Pure Kotlin Persian Typography & Text Normalizer
 * Handles:
 * - Arabic to Persian Yeh & Kaf (ي -> ی, ك -> ک)
 * - Half-spaces (نیم‌فاصله \u200c) for prefixes & suffixes
 * - English/Arabic digits to Persian digits (0-9 -> ۰-۹)
 * - Punctuation normalization (ویرگول «،»، گیومه «...»، علامت سوال «؟»)
 * - Multi-space & redundant punctuation reduction
 */
object PersianNormalizer {
    private const val ZWNJ = "\u200c" // Zero Width Non-Joiner (نیم‌فاصله)

    private val PERSIAN_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    private val ARABIC_DIGITS = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')

    /**
     * Complete Persian normalization pipeline
     */
    fun normalize(text: String): String {
        if (text.isBlank()) return text

        var result = text

        // 1. Convert Arabic characters to Persian
        result = result.replace('ي', 'ی')
            .replace('ك', 'ک')
            .replace('ى', 'ی')
            .replace('ؤ', 'و')
            .replace('إ', 'ا')
            .replace('أ', 'ا')
            .replace('ة', 'ه')

        // 2. Normalize punctuation
        result = result.replace(",", "،")
            .replace("?", "؟")
            .replace(";", "؛")
            .replace("%", "٪")
            .replace("\"", "»")

        // 3. Fix Persian digits
        val chars = result.toCharArray()
        for (i in chars.indices) {
            val c = chars[i]
            if (c in '0'..'9') {
                chars[i] = PERSIAN_DIGITS[c - '0']
            } else if (c in '٠'..'٩') {
                chars[i] = PERSIAN_DIGITS[c - '٠']
            }
        }
        result = String(chars)

        // 4. Half-space prefixes: «می » -> «می‌», «نمی » -> «نمی‌»
        result = result.replace(Regex("\\bمی\\s+"), "می$ZWNJ")
            .replace(Regex("\\bنمی\\s+"), "نمی$ZWNJ")

        // 5. Half-space suffixes: « ها», « تر», « ترین»
        result = result.replace(Regex("\\s+ها\\b"), "${ZWNJ}ها")
            .replace(Regex("\\s+های\\b"), "${ZWNJ}های")
            .replace(Regex("\\s+هایی\\b"), "${ZWNJ}هایی")
            .replace(Regex("\\s+تر\\b"), "${ZWNJ}تر")
            .replace(Regex("\\s+ترین\\b"), "${ZWNJ}ترین")
            .replace(Regex("\\s+ام\\b"), "${ZWNJ}ام")
            .replace(Regex("\\s+ات\\b"), "${ZWNJ}ات")
            .replace(Regex("\\s+اش\\b"), "${ZWNJ}اش")
            .replace(Regex("\\s+مان\\b"), "${ZWNJ}مان")
            .replace(Regex("\\s+تان\\b"), "${ZWNJ}تان")
            .replace(Regex("\\s+شان\\b"), "${ZWNJ}شان")

        // 6. Common compound cosmetic/hygiene words with half-space
        val compoundTerms = listOf(
            "ضد آفتاب" to "ضد${ZWNJ}آفتاب",
            "ضد لک" to "ضد${ZWNJ}لک",
            "ضد چروک" to "ضد${ZWNJ}چروک",
            "ضد جوش" to "ضد${ZWNJ}جوش",
            "دست ساز" to "دست${ZWNJ}ساز",
            "نرم کننده" to "نرم${ZWNJ}کننده",
            "مرطوب کننده" to "مرطوب${ZWNJ}کننده",
            "تقویت کننده" to "تقویت${ZWNJ}کننده",
            "پاک کننده" to "پاک${ZWNJ}کننده",
            "روشن کننده" to "روشن${ZWNJ}کننده",
            "لایه بردار" to "لایه${ZWNJ}بردار",
            "آبرسان" to "آب${ZWNJ}رسان",
            "فاقد چربی" to "فاقد چربی",
            "ضد عفونی کننده" to "ضد${ZWNJ}عفونی${ZWNJ}کننده"
        )
        for ((plain, normalized) in compoundTerms) {
            result = result.replace(plain, normalized)
        }

        // 7. Cleanup extra spaces and redundant exclamation/question marks
        result = result.replace(Regex("\\s+"), " ")
            .replace(Regex("([!؟]){2,}"), "$1")
            .trim()

        return result
    }
}
