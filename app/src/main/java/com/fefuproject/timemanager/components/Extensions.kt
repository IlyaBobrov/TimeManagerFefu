package com.fefuproject.timemanager.components

import android.content.Context
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.fefuproject.timemanager.R
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.*

internal fun getParsedDate(date: String, format: String, locale: Locale = Locale.US): String {
    for (mask in Constants.listOfParserMask) {
        try {
            val parser = SimpleDateFormat(mask, locale)
            val formatter = SimpleDateFormat(format, locale)
            return formatter.format(parser.parse(date)!!)
        } catch (e: Exception) {
            e.printStackTrace()
            continue
        }
    }
    throw IllegalArgumentException("Неккоректная маска для парсинга дат")
}

internal fun getLocalDateRangeFormat(date: String): LocalDate =
    LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)

internal fun selectHeaderCardDate(date: String, tvHeader: TextView) {
    val today = LocalDate.now()
    val dateLocale = getLocalDateRangeFormat(date)
    tvHeader.text = when (dateLocale) {
        today -> {
            tvHeader.resources.getString(R.string.today)
        }
        today.minusDays(1) -> {
            tvHeader.resources.getString(R.string.Yesterday)
        }
        today.plusDays(1) -> {
            tvHeader.resources.getString(R.string.Tomorrow)
        }
        else -> {
            getParsedDate(date, "dd.MM.yyyy")
        }
    }
}

internal fun Context.getColorCompat(@ColorRes color: Int) = ContextCompat.getColor(this, color)
