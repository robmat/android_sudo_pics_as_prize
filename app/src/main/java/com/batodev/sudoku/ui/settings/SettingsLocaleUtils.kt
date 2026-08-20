package com.batodev.sudoku.ui.settings

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.batodev.sudoku.R
import org.xmlpull.v1.XmlPullParser
import java.util.Locale

internal fun getCurrentLocaleString(context: Context): String {
    val langs = getLangs(context)
    langs.forEach {
        Log.d("lang", "${it.key} ${it.value}")
    }
    val locales = AppCompatDelegate.getApplicationLocales()
    if (locales == LocaleListCompat.getEmptyLocaleList()) {
        return context.getString(R.string.label_default)
    }
    return getDisplayName(locales.toLanguageTags())
}

internal fun getCurrentLocaleTag(): String {
    val locales = AppCompatDelegate.getApplicationLocales()
    if (locales == LocaleListCompat.getEmptyLocaleList()) {
        return ""
    }
    return locales.toLanguageTags()
}

private fun XmlPullParser.localeNameAttributeOrNull(): String? {
    for (i in 0 until attributeCount) {
        if (getAttributeName(i) == "name") {
            return getAttributeValue(i)
        }
    }
    return null
}

private fun localeTagAndDisplayNameOrNull(parser: XmlPullParser): Pair<String, String>? {
    val isLocaleTag = parser.eventType == XmlPullParser.START_TAG && parser.name == "locale"
    val langTag = if (isLocaleTag) parser.localeNameAttributeOrNull() else null
    val displayName = langTag?.let { getDisplayName(it) }
    return if (langTag != null && !displayName.isNullOrEmpty()) Pair(langTag, displayName) else null
}

internal fun getLangs(context: Context): Map<String, String> {
    val langs = mutableListOf<Pair<String, String>>()
    val parser = context.resources.getXml(R.xml.locales_config)
    while (parser.eventType != XmlPullParser.END_DOCUMENT) {
        localeTagAndDisplayNameOrNull(parser)?.let { langs.add(it) }
        parser.next()
    }

    langs.sortBy { it.second }
    langs.add(0, Pair("", context.getString(R.string.label_default)))

    return langs.toMap()
}

private fun getDisplayName(lang: String?): String {
    if (lang == null) {
        return ""
    }

    val locale = when (lang) {
        "" -> LocaleListCompat.getAdjustedDefault()[0]
        else -> Locale.forLanguageTag(lang)
    }
    return locale!!.getDisplayName(locale).replaceFirstChar { it.uppercase(locale) }
}
