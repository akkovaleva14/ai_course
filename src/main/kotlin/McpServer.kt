import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class McpServer(
    private val weatherService: WeatherService = WeatherService()
) {
    private val mapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    fun run() {
        System.err.println("Starting MCP server over stdio...")

        while (true) {
            val line = readLine() ?: break
            if (line.isBlank()) {
                continue
            }

            try {
                val request = mapper.readValue(line, McpJsonRpcRequest::class.java)
                val response = handleRequest(request)
                if (response != null) {
                    println(mapper.writeValueAsString(response))
                    System.out.flush()
                }
            } catch (e: Exception) {
                System.err.println("Failed to process MCP message: ${e.message}")
            }
        }

        System.err.println("MCP server stopped.")
    }

    private fun handleRequest(request: McpJsonRpcRequest): McpJsonRpcResponse? {
        return when (request.method) {
            "initialize" -> {
                McpJsonRpcResponse(
                    id = request.id,
                    result = mapOf(
                        "protocolVersion" to "2024-11-05",
                        "serverInfo" to mapOf(
                            "name" to "weather-mcp-server",
                            "version" to "1.0.0"
                        ),
                        "capabilities" to mapOf(
                            "tools" to mapOf<String, Any>()
                        )
                    )
                )
            }

            "notifications/initialized" -> {
                null
            }

            "tools/list" -> {
                McpJsonRpcResponse(
                    id = request.id,
                    result = McpToolsListResult(
                        tools = listOf(
                            McpTool(
                                name = "get_weather",
                                description = "Возвращает текущую погоду по названию города",
                                inputSchema = mapOf(
                                    "type" to "object",
                                    "properties" to mapOf(
                                        "city" to mapOf(
                                            "type" to "string",
                                            "description" to "Название города"
                                        )
                                    ),
                                    "required" to listOf("city")
                                )
                            )
                        )
                    )
                )
            }

            "tools/call" -> {
                handleToolCall(request)
            }

            else -> {
                McpJsonRpcResponse(
                    id = request.id,
                    error = McpErrorObject(
                        code = -32601,
                        message = "Method not found: ${request.method}"
                    )
                )
            }
        }
    }

    private fun handleToolCall(request: McpJsonRpcRequest): McpJsonRpcResponse {
        val params = request.params
            ?: return McpJsonRpcResponse(
                id = request.id,
                error = McpErrorObject(
                    code = -32602,
                    message = "Missing params"
                )
            )

        val toolName = params.get("name")?.asText()?.trim().orEmpty()
        val arguments = params.get("arguments")

        if (toolName != "get_weather") {
            return McpJsonRpcResponse(
                id = request.id,
                error = McpErrorObject(
                    code = -32602,
                    message = "Unknown tool: $toolName"
                )
            )
        }

        val city = arguments?.get("city")?.asText()?.trim().orEmpty()

        if (city.isBlank()) {
            return McpJsonRpcResponse(
                id = request.id,
                result = McpCallToolResult(
                    content = listOf(
                        McpTextContent(
                            text = """{"error":"Tool argument 'city' is required"}"""
                        )
                    ),
                    isError = true
                )
            )
        }

        val result = weatherService.getWeather(city)
        val resultJson = mapper.writeValueAsString(result)

        return McpJsonRpcResponse(
            id = request.id,
            result = McpCallToolResult(
                content = listOf(
                    McpTextContent(text = resultJson)
                ),
                isError = false
            )
        )
    }
}
