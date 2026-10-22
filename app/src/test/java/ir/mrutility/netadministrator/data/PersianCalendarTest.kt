package ir.mrutility.netadministrator.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class PersianCalendarTest {
    @Test
    fun roundTripKnownDate() {
        val j = "1403/01/01"
        val g = PersianCalendar.toGregorian(j)
        assertEquals(LocalDate.of(2024, 3, 20), g)
        assertEquals(j, PersianCalendar.fromGregorian(g))
    }

    @Test
    fun acceptsPersianDigits() {
        val g = PersianCalendar.toGregorian("۱۴۰۳/۰۱/۰۱")
        assertEquals(LocalDate.of(2024, 3, 20), g)
    }
}
