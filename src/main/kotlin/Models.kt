import com.fasterxml.jackson.databind.JsonNode

data class OAuthTokenResponse(
    val access_token: String,
    val expires_at: Long
)

data class ModelsResponse(
    val data: List<ModelInfo>
)

data class ModelInfo(
    val id: String
)

data class GigaChatRequest(
    val model: String,
    val temperature: Double,
    val messages: List<RequestMessage>,
    val functions: List<FunctionDefinition>? = null,
    val function_call: String? = null
)

data class RequestMessage(
    val role: String,
    val content: String? = null,
    val name: String? = null,
    val function_call: RequestFunctionCall? = null
)

data class RequestFunctionCall(
    val name: String,
    val arguments: JsonNode
)

data class FunctionDefinition(
    val name: String,
    val description: String,
    val parameters: FunctionParameters
)

data class FunctionParameters(
    val type: String,
    val properties: Map<String, FunctionProperty>,
    val required: List<String>
)

data class FunctionProperty(
    val type: String,
    val description: String
)

data class GigaChatResponse(
    val choices: List<GigaChatChoice>
) {
    fun firstChoice(): GigaChatChoice {
        return choices.firstOrNull() ?: error("Empty response from GigaChat")
    }

    fun firstMessageContent(): String {
        return firstChoice().message.content
            ?: error("Empty message content from GigaChat")
    }
}

data class GigaChatChoice(
    val message: ResponseMessage,
    val finish_reason: String? = null
)

data class ResponseMessage(
    val role: String? = null,
    val content: String? = null,
    val function_call: ResponseFunctionCall? = null
)

data class ResponseFunctionCall(
    val name: String? = null,
    val arguments: JsonNode? = null
)
