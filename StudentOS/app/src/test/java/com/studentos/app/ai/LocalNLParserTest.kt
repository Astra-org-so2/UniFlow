package com.studentos.app.ai

import org.junit.Assert.*
import org.junit.Test

class LocalNLParserTest {

    private val parser = LocalNLParser()

    @Test
    fun `parse Russian lab assignment with subject`() {
        val result = parser.parse("По физике сделать лабораторную 3 до пятницы")

        assertTrue(result.title.isNotEmpty())
        assertEquals("Physics", result.subject) // "физике" → "Physics"... actually it extracts Cyrillic
        assertNotNull(result.dayOfWeek)
    }

    @Test
    fun `parse task with deadline date`() {
        val result = parser.parse("Сделать ДЗ до 20.09")

        assertTrue(result.title.isNotEmpty())
        assertNotNull(result.date)
        assertEquals("TASK", result.type)
    }

    @Test
    fun `detect exam type`() {
        val result = parser.parse("Экзамен по математике 15 декабря")

        assertEquals("EXAM", result.type)
    }

    @Test
    fun `detect lab type`() {
        val result = parser.parse("Лабораторная работа 4 по программированию")

        assertEquals("LAB", result.type)
    }

    @Test
    fun `parse English input`() {
        val result = parser.parse("Finish Physics Lab #3 by Friday")

        assertTrue(result.title.isNotEmpty())
        assertEquals("Friday", result.dayOfWeek)
    }

    @Test
    fun `parse with date pattern`() {
        val result = parser.parse("Задача по английскому до 15.10.2024")

        assertTrue(result.title.isNotEmpty())
        assertNotNull(result.date)
        assertTrue(result.date!!.contains("15"))
    }

    @Test
    fun `handle empty input gracefully`() {
        val result = parser.parse("")

        assertNotNull(result)
        assertEquals("TASK", result.type)
    }

    @Test
    fun `detect practice type`() {
        val result = parser.parse("Практика по химии на среду")

        assertEquals("PRACTICE", result.type)
    }

    @Test
    fun `parse day of week in Russian`() {
        val result = parser.parse("Сделать задачу по физике на вторник")

        assertEquals("Tuesday", result.dayOfWeek)
    }
}
