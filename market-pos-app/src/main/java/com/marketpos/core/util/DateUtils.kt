package com.marketpos.core.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateUtils {
    private val zone: ZoneId = ZoneId.systemDefault()
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

    fun now(): Long = System.currentTimeMillis()

    fun formatDateTime(epochMillis: Long): String {
        return Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDateTime().format(dateTimeFormatter)
    }

    fun formatDate(epochMillis: Long): String {
        return Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate().format(dateFormatter)
    }

    fun startOfDay(epochMillis: Long): Long {
        val localDate = Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
        return localDate.atStartOfDay(zone).toInstant().toEpochMilli()
    }

    fun endOfDay(epochMillis: Long): Long {
        val localDate = Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
        return localDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
    }

    fun dayRange(epochMillis: Long = now()): LongRange {
        val localDate = Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
        val start = localDate.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = localDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
        return start..end
    }

    fun monthRange(epochMillis: Long = now()): LongRange {
        val localDate = Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
        val first = LocalDate.of(localDate.year, localDate.month, 1)
        val next = first.plusMonths(1)
        val start = first.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = next.atStartOfDay(zone).toInstant().toEpochMilli() - 1
        return start..end
    }

    fun weekRange(epochMillis: Long = now()): LongRange {
        val localDate = Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
        val startDate = localDate.minusDays((localDate.dayOfWeek.value - 1).toLong())
        val endDate = startDate.plusDays(7)
        val start = startDate.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = endDate.atStartOfDay(zone).toInstant().toEpochMilli() - 1
        return start..end
    }
}
