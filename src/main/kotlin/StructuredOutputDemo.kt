import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

enum class Sentiment {
    positive, neutral, negative
}

enum class Category {
    ui, stability, performance, billing, notifications, other
}

enum class Priority {
    low, medium, high
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class ReviewClassification(
    val sentiment: Sentiment,
    val category: Category,
    val priority: Priority,
    val summary: String
)

fun extractJsonFallback(raw: String): String {
    val withoutMarkdown = raw
        .replace("```json", "")
        .replace("```", "")
        .trim()

    if (withoutMarkdown.startsWith("{") && withoutMarkdown.endsWith("}")) {
        return withoutMarkdown
    }

    val start = withoutMarkdown.indexOf('{')
    val end = withoutMarkdown.lastIndexOf('}')

    if (start >= 0 && end > start) {
        return withoutMarkdown.substring(start, end + 1)
    }

    error("Could not extract JSON from model response: $raw")
}

fun validateReviewClassification(result: ReviewClassification) {
    require(result.summary.isNotBlank()) {
        "Summary must not be blank"
    }
}

fun buildReviewClassifierSystemPrompt(): String {
    return """
        Ты — сервис классификации пользовательских отзывов о мобильном приложении.
        Твоя задача — вернуть только валидный JSON-объект.

        Запрещено:
        - использовать markdown
        - добавлять пояснения
        - добавлять текст до или после JSON
        - добавлять поля, которых нет в схеме

        Верни строго один JSON-объект следующего вида:
        {
          "sentiment": "positive | neutral | negative",
          "category": "ui | stability | performance | billing | notifications | other",
          "priority": "low | medium | high",
          "summary": "краткое резюме отзыва"
        }
    """.trimIndent()
}

fun classifyReview(
    client: GigaChatClient,
    model: String,
    reviewText: String
): ReviewClassification {
    val mapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    val systemPrompt = buildReviewClassifierSystemPrompt()

    val userPrompt = """
        Классифицируй отзыв пользователя:
        "$reviewText"
    """.trimIndent()

    val rawResponse = client.ask(
        model = model,
        temperature = 0.2,
        messages = listOf(
            RequestMessage(role = "system", content = systemPrompt),
            RequestMessage(role = "user", content = userPrompt)
        )
    )

    val rawContent = rawResponse.firstMessageContent()

    println("Raw model response:")
    println(rawContent)
    println()

    val result = try {
        val cleanJson = extractJsonFallback(rawContent)
        mapper.readValue<ReviewClassification>(cleanJson)
    } catch (e: Exception) {
        throw IllegalStateException(
            "Failed to parse structured output from model response: $rawContent",
            e
        )
    }

    validateReviewClassification(result)
    return result
}

fun printReviewClassification(result: ReviewClassification) {
    println("Parsed result:")
    println("  Sentiment : ${result.sentiment}")
    println("  Category  : ${result.category}")
    println("  Priority  : ${result.priority}")
    println("  Summary   : ${result.summary}")
}

fun runStructuredOutputDemo(client: GigaChatClient, model: String) {
    val sampleReviews = listOf(
        "После последнего обновления приложение зависает на экране логина и иногда вылетает.",
        "Уведомления иногда приходят с задержкой, но в целом приложение работает нормально.",
        "Очень понравился новый интерфейс, всё стало заметно удобнее."
    )

    sampleReviews.forEachIndexed { index, review ->
        println("Example ${index + 1}:")
        println("Review:")
        println(review)
        println()

        val result = classifyReview(
            client = client,
            model = model,
            reviewText = review
        )

        printReviewClassification(result)
        println()
        println("--------------------------------------------------")
        println()
    }
}
