package com.offline.maptimestampcamera.utils

object FlagUtils {
    /**
     * Converts a 2-letter country code (e.g., "US", "IN", "GB") into a regional indicator flag emoji (🇺🇸, 🇮🇳, 🇬🇧).
     */
    fun countryCodeToEmojiFlag(countryCode: String?): String {
        if (countryCode.isNullOrEmpty() || countryCode.length != 2) {
            return "🌐"
        }
        val codeUpper = countryCode.uppercase()
        val firstChar = Character.codePointAt(codeUpper, 0) - 0x41 + 0x1F1E6
        val secondChar = Character.codePointAt(codeUpper, 1) - 0x41 + 0x1F1E6

        return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
    }
}
