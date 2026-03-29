import kotlin.system.exitProcess

fun main(args: Array<String>) {
    try {
        val authorizationKey = System.getenv("GIGACHAT_AUTH_KEY")
            ?.trim()
            ?.removePrefix("Basic ")
            ?: error(
                "Environment variable GIGACHAT_AUTH_KEY is not set. " +
                        "Expected Base64(clientId:clientSecret)."
            )

        val unsafeSsl = System.getenv("GIGACHAT_UNSAFE_SSL")
            ?.trim()
            ?.equals("true", ignoreCase = true) == true

        println("Starting GigaChat app...")
        println("Unsafe SSL mode: $unsafeSsl")

        val client = GigaChatClient(
            authorizationKey = authorizationKey,
            scope = "GIGACHAT_API_PERS",
            unsafeSsl = unsafeSsl
        )

        val models = client.getModels()

        val defaultModel = models.firstOrNull { it.equals("GigaChat", ignoreCase = true) }
            ?: models.firstOrNull { it.contains("Lite", ignoreCase = true) }
            ?: models.first()

        when (args.firstOrNull()) {
            "structured-output" -> {
                println("Running structured output demo...")
                println("Model: $defaultModel")
                runStructuredOutputDemo(client, defaultModel)
            }

            "function-calling" -> {
                println("Running function calling demo...")
                println("Model: $defaultModel")
                runFunctionCallingDemo(client, defaultModel)
            }

            null, "chat" -> {
                val chat = CliChat(client, models)
                chat.run()
            }

            else -> {
                println("Unknown mode: ${args[0]}")
                println("Available modes:")
                println("  chat")
                println("  structured-output")
                println("  function-calling")
            }
        }
    } catch (t: Throwable) {
        System.err.println()
        System.err.println("Application failed: ${t::class.qualifiedName}: ${t.message}")
        t.printStackTrace()
        exitProcess(1)
    }
}
