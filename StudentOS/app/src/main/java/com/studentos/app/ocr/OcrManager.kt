package com.studentos.app.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

data class OcrResult(
    val fullText: String,
    val blocks: List<TextBlock>
)

data class TextBlock(
    val text: String,
    val confidence: Float = 0f
)

@Singleton
class OcrManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val recognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun recognizeText(imageUri: Uri): Result<OcrResult> {
        return try {
            val image = InputImage.fromFilePath(context, imageUri)
            val result = suspendCancellableCoroutine { continuation ->
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val blocks = visionText.textBlocks.map { block ->
                            TextBlock(
                                text = block.text,
                                confidence = block.lines.firstOrNull()?.confidence ?: 0f
                            )
                        }
                        continuation.resume(
                            OcrResult(
                                fullText = visionText.text,
                                blocks = blocks
                            )
                        )
                    }
                    .addOnFailureListener { e ->
                        continuation.resume(null)
                    }
            }

            if (result != null) {
                Result.success(result)
            } else {
                Result.failure(Exception("OCR recognition failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Parse OCR text into structured academic data.
     * Tries to extract: task title, deadline, subject hints
     */
    fun parseOcrText(text: String): OcrParsedData {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }

        var title = ""
        var deadline: String? = null
        var subject: String? = null

        for (line in lines) {
            val lower = line.lowercase()

            // Detect lab/task patterns
            if (lower.contains("лаб") || lower.contains("lab") ||
                lower.contains("задани") || lower.contains("дз") ||
                lower.contains("hw") || lower.contains("homework")) {
                title = line
            }

            // Detect deadline patterns
            val dateRegex = Regex("(\\d{1,2})[./\\-](\\d{1,2})(?:[./\\-](\\d{2,4}))?")
            dateRegex.find(line)?.let {
                val day = it.groupValues[1]
                val month = it.groupValues[2]
                deadline = "$day.$month"
            }

            // Detect "до" / "сдать" / "deadline" / "due"
            if (lower.contains("до ") || lower.contains("сдать") || lower.contains("deadline") || lower.contains("due")) {
                dateRegex.find(line)?.let {
                    deadline = it.value
                }
            }
        }

        if (title.isEmpty() && lines.isNotEmpty()) {
            title = lines.first()
        }

        return OcrParsedData(
            title = title,
            deadline = deadline,
            subject = subject,
            rawText = text
        )
    }
}

data class OcrParsedData(
    val title: String,
    val deadline: String?,
    val subject: String?,
    val rawText: String
)
