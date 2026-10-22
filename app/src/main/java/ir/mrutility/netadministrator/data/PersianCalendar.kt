package ir.mrutility.netadministrator.data

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object PersianCalendar {
    private val digits = mapOf('۰' to '0', '۱' to '1', '۲' to '2', '۳' to '3', '۴' to '4', '۵' to '5', '۶' to '6', '۷' to '7', '۸' to '8', '۹' to '9', '٠' to '0', '١' to '1', '٢' to '2', '٣' to '3', '٤' to '4', '٥' to '5', '٦' to '6', '٧' to '7', '٨' to '8', '٩' to '9')
    fun normalizeDigits(value: String): String = value.map { digits[it] ?: it }.joinToString("")

    fun toGregorian(jalaliInput: String): LocalDate {
        val raw = normalizeDigits(jalaliInput.trim()).replace('-', '/')
        val p = raw.split('/')
        require(p.size == 3) { "تاریخ را به شکل ۱۴۰۵/۰۸/۰۱ وارد کن." }
        val jy0 = p[0].toInt(); val jm = p[1].toInt(); val jd = p[2].toInt()
        require(jy0 in 1200..1600 && jm in 1..12 && jd >= 1 && jd <= if (jm <= 6) 31 else if (jm <= 11) 30 else 30) { "تاریخ شمسی معتبر نیست." }
        var jy = jy0 + 1595
        var days = -355668 + 365 * jy + (jy / 33) * 8 + ((jy % 33 + 3) / 4) + jd
        days += if (jm <= 7) (jm - 1) * 31 else 186 + (jm - 7) * 30
        var gy = 400 * (days / 146097)
        days %= 146097
        if (days > 36524) {
            gy += 100 * ((days - 1) / 36524)
            days = (days - 1) % 36524
            if (days >= 365) days += 1
        }
        gy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) { gy += (days - 1) / 365; days = (days - 1) % 365 }
        val gd = days + 1
        val leap = (gy % 4L == 0L && gy % 100L != 0L) || gy % 400L == 0L
        val months = intArrayOf(31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gm = 1; var day = gd
        while (gm <= 12 && day > months[gm - 1]) { day -= months[gm - 1]; gm++ }
        val result = LocalDate.of(gy.toInt(), gm, day.toInt())
        require(fromGregorian(result) == "%04d/%02d/%02d".format(jy0, jm, jd)) { "روز انتخاب‌شده در این ماه شمسی وجود ندارد." }
        return result
    }

    fun fromGregorian(date: LocalDate): String {
        val gy = date.year; val gm = date.monthValue; val gd = date.dayOfMonth
        val gdm = intArrayOf(0,31,59,90,120,151,181,212,243,273,304,334)
        val gy2 = if (gm > 2) gy + 1 else gy
        var days = 355666L + 365L * gy + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400 + gd + gdm[gm - 1]
        var jy = -1595 + (days / 12053).toInt() * 33
        days %= 12053
        jy += 4 * (days / 1461).toInt(); days %= 1461
        if (days > 365) { jy += ((days - 1) / 365).toInt(); days = (days - 1) % 365 }
        val jm: Int; val jd: Int
        if (days < 186) { jm = 1 + (days / 31).toInt(); jd = 1 + (days % 31).toInt() }
        else { jm = 7 + ((days - 186) / 30).toInt(); jd = 1 + ((days - 186) % 30).toInt() }
        return "%04d/%02d/%02d".format(jy, jm, jd)
    }

    fun fromEpoch(epochMillis: Long): String = fromGregorian(java.time.Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate())
    fun toEpoch(jalali: String, time: String = "09:00"): Long {
        val date = toGregorian(jalali)
        val parts = normalizeDigits(time).split(':')
        require(parts.size == 2) { "ساعت را به شکل ۰۹:۳۰ وارد کن." }
        return LocalDateTime.of(date.year, date.monthValue, date.dayOfMonth, parts[0].toInt(), parts[1].toInt()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
    fun now(): String = fromEpoch(System.currentTimeMillis())
}
