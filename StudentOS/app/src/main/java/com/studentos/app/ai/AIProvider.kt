package com.studentos.app.ai

/**
 * Abstraction for AI capabilities.
 * Implementations:
 * - [NoOpAIProvider] — no AI available, app fully functional
 * - [LocalAIProvider] — on-device ML model
 * - [ApiAIProvider] — external API (user provides key)
 */
interface AIGateway {
    suspend fun parseNaturalLanguage(input: String): ParsedInput?
    suspend fun generateResponse(prompt: String, context: String): String?
    suspend fun analyzeImage(imageUri: String): String?
    suspend fun isAvailable(): Boolean
    fun getProviderType(): AIProviderType
}

enum class AIProviderType {
    NONE, LOCAL, API
}

data class ParsedInput(
    val title: String = "",
    val subject: String? = null,
    val deadline: String? = null, // natural language date
    val type: ParsedInputType = ParsedInputType.TASK,
    val priority: String? = null,
    val description: String? = null,
    val confidence: Float = 0f
)

enum class ParsedInputType {
    TASK, CLASS, GRADE, ATTENDANCE, EXAM, NOTE
}

/**
 * No-op provider — app works fully without AI
 */
class NoOpAIProvider : AIGateway {
    override suspend fun parseNaturalLanguage(input: String): ParsedInput? = null
    override suspend fun generateResponse(prompt: String, context: String): String? = null
    override suspend fun analyzeImage(imageUri: String): String? = null
    override suspend fun isAvailable(): Boolean = false
    override fun getProviderType(): AIProviderType = AIProviderType.NONE
}

/**
 * Basic local NLP parser — works without any AI model.
 * Uses rule-based parsing for common patterns.
 */
class LocalNLParser {

    data class ParseResult(
        val title: String,
        val subject: String? = null,
        val dayOfWeek: String? = null,
        val date: String? = null,
        val type: String = "TASK"
    )

    fun parse(input: String): ParseResult {
        val lower = input.lowercase().trim()

        // Detect type
        val type = when {
            lower.contains("лаб") || lower.contains("lab") -> "LAB"
            lower.contains("экзамен") || lower.contains("exam") -> "EXAM"
            lower.contains("зачёт") || lower.contains("зачет") || lower.contains("credit") -> "CREDIT"
            lower.contains("контрольн") || lower.contains("test") -> "TEST"
            lower.contains("курсов") || lower.contains("course") -> "COURSEWORK"
            else -> "TASK"
        }

        // Detect subject patterns (after "по", "по предмету", "for")
        val subjectPatterns = listOf(
            Regex("(?:по|по предмету|для|for)\\s+([а-яА-Яa-zA-ZёЁ]+(?:\\s+[а-яА-Яa-zA-ZёЁ]+)?)", RegexOption.IGNORE_CASE)
        )
        var subject: String? = null
        for (pattern in subjectPatterns) {
            val match = pattern.find(lower)
            if (match != null) {
                subject = match.groupValues[1].trim()
                    .replaceFirstChar { it.uppercase() }
                break
            }
        }

        // Detect day of week
        val dayMap = mapOf(
            "понедельник" to "Monday", "пн" to "Monday", "monday" to "Monday", "mon" to "Monday",
            "вторник" to "Tuesday", "вт" to "Tuesday", "tuesday" to "Tuesday", "tue" to "Tuesday",
            "среда" to "Wednesday", "ср" to "Wednesday", "wednesday" to "Wednesday", "wed" to "Wednesday",
            "четверг" to "Thursday", "чт" to "Thursday", "thursday" to "Thursday", "thu" to "Thursday",
            "пятница" to "Friday", "пт" to "Friday", "friday" to "Friday", "fri" to "Friday",
            "суббота" to "Saturday", "сб" to "Saturday", "saturday" to "Saturday", "sat" to "Saturday",
            "воскресенье" to "Sunday", "вс" to "Sunday", "sunday" to "Sunday", "sun" to "Sunday"
        )
        var dayOfWeek: String? = null
        for ((key, value) in dayMap) {
            if (lower.contains(key)) {
                dayOfWeek = value
                break
            }
        }

        // Detect date patterns
        val dateRegex = Regex("(\\d{1,2})[./\\-](\\d{1,2})(?:[./\\-](\\d{2,4}))?")
        val dateMatch = dateRegex.find(input)
        val date = dateMatch?.let {
            val day = it.groupValues[1].padStart(2, '0')
            val month = it.groupValues[2].padStart(2, '0')
            val year = it.groupValues[3].ifEmpty { java.time.Year.now().value.toString() }
            "$year-$month-$day"
        }

        // Extract title (everything that's not a recognized pattern)
        var title = input
            .replace(Regex("(?:по|по предмету|для|for)\\s+[а-яА-Яa-zA-ZёЁ]+(?:\\s+[а-яА-Яa-zA-ZёЁ]+)?", RegexOption.IGNORE_CASE), "")
            .replace(Regex("(?:до|к|на|deadline|due|by|before)\\s+\\S+", RegexOption.IGNORE_CASE), "")
        dayMap.keys.forEach { title = title.replace(Regex(it, RegexOption.IGNORE_CASE), "") }
        title = title.replace(dateRegex, "").trim()
        if (title.isBlank()) title = input.trim()

        return ParseResult(
            title = title.replaceFirstChar { it.uppercase() },
            subject = subject,
            dayOfWeek = dayOfWeek,
            date = date,
            type = type
        )
    }
}
