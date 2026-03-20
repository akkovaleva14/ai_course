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
    val messages: List<RequestMessage>
)

data class RequestMessage(
    val role: String,
    val content: String
)

data class GigaChatResponse(
    val choices: List<GigaChatChoice>
)

data class GigaChatChoice(
    val message: ResponseMessage
)

data class ResponseMessage(
    val role: String?,
    val content: String?
)
