import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.JsonNode

@JsonInclude(JsonInclude.Include.NON_NULL)
data class McpJsonRpcRequest(
    val jsonrpc: String = "2.0",
    val id: JsonNode? = null,
    val method: String,
    val params: JsonNode? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class McpJsonRpcResponse(
    val jsonrpc: String = "2.0",
    val id: JsonNode? = null,
    val result: Any? = null,
    val error: McpErrorObject? = null
)

data class McpErrorObject(
    val code: Int,
    val message: String
)

data class McpTool(
    val name: String,
    val description: String,
    val inputSchema: Map<String, Any>
)

data class McpToolsListResult(
    val tools: List<McpTool>
)

data class McpTextContent(
    val type: String = "text",
    val text: String
)

data class McpCallToolResult(
    val content: List<McpTextContent>,
    val isError: Boolean = false
)
