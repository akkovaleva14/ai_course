import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.treeToValue

@JsonIgnoreProperties(ignoreUnknown = true)
data class WeatherArguments(
    val city: String? = null
)

data class WeatherResult(
    val city: String,
    val temperatureC: Int?,
    val condition: String,
    val precipitation: String? = null,
    val windSpeedMs: Int? = null
)

fun runFunctionCallingDemo(client: GigaChatClient, model: String) {
    val mapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    val userQuestion = "Какая погода в Москве?"

    println("User: $userQuestion")
    println()

    val weatherFunction = FunctionDefinition(
        name = "getWeather",
        description = "Возвращает текущую погоду по названию города",
        parameters = FunctionParameters(
            type = "object",
            properties = mapOf(
                "city" to FunctionProperty(
                    type = "string",
                    description = "Название города"
                )
            ),
            required = listOf("city")
        )
    )

    val firstResponse = client.ask(
        model = model,
        temperature = 0.1,
        messages = listOf(
            RequestMessage(role = "user", content = userQuestion)
        ),
        functions = listOf(weatherFunction),
        functionCall = "auto"
    )

    val firstChoice = firstResponse.firstChoice()
    val firstMessage = firstChoice.message

    println("Raw first response:")
    println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(firstResponse))
    println()

    if (firstChoice.finish_reason != "function_call") {
        val directAnswer = firstMessage.content?.trim().orEmpty()
        println("Model did not request a function call.")
        println("Final answer:")
        println(directAnswer.ifBlank { "<empty>" })
        return
    }

    val responseFunctionCall = firstMessage.function_call
        ?: error("finish_reason=function_call, but message.function_call is missing")

    val functionName = responseFunctionCall.name?.trim().orEmpty()

    if (functionName != "getWeather") {
        error("Unsupported function requested: $functionName")
    }

    val argumentsNode = responseFunctionCall.arguments
        ?: error("Function arguments are missing")

    val parsedArguments = try {
        mapper.treeToValue<WeatherArguments>(argumentsNode)
    } catch (e: Exception) {
        throw IllegalStateException(
            "Failed to parse function arguments: ${argumentsNode.toPrettyString()}",
            e
        )
    }

    val city = parsedArguments.city?.trim().orEmpty()
    if (city.isBlank()) {
        error("Function argument 'city' is missing")
    }

    println("Model requested function call: $functionName(city=$city)")
    println()

    val functionResult = getWeather(city)
    val functionResultJson = mapper.writeValueAsString(functionResult)

    println("Function result:")
    println(functionResultJson)
    println()

    val secondResponse = client.ask(
        model = model,
        temperature = 0.3,
        messages = listOf(
            RequestMessage(role = "user", content = userQuestion),
            RequestMessage(
                role = "assistant",
                content = null,
                function_call = RequestFunctionCall(
                    name = functionName,
                    arguments = argumentsNode
                )
            ),
            RequestMessage(
                role = "function",
                name = functionName,
                content = functionResultJson
            )
        )
    )

    val finalAnswer = secondResponse.firstMessageContent()

    println("Final answer:")
    println(finalAnswer)
}

fun getWeather(city: String): WeatherResult {
    return when (city.trim().lowercase()) {
        "москва", "moscow" -> WeatherResult(
            city = "Москва",
            temperatureC = -2,
            condition = "snow",
            precipitation = "snow",
            windSpeedMs = 5
        )

        "санкт-петербург", "saint petersburg", "st. petersburg", "saint-petersburg" -> WeatherResult(
            city = "Санкт-Петербург",
            temperatureC = 0,
            condition = "rain",
            precipitation = "rain",
            windSpeedMs = 7
        )

        "казань", "kazan" -> WeatherResult(
            city = "Казань",
            temperatureC = -5,
            condition = "clear",
            precipitation = "none",
            windSpeedMs = 3
        )

        "новосибирск", "novosibirsk" -> WeatherResult(
            city = "Новосибирск",
            temperatureC = -8,
            condition = "cloudy",
            precipitation = "none",
            windSpeedMs = 4
        )

        else -> WeatherResult(
            city = city,
            temperatureC = null,
            condition = "unknown",
            precipitation = null,
            windSpeedMs = null
        )
    }
}
