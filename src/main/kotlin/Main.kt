import kotlin.system.exitProcess

fun main() {
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

        println("Starting GigaChat CLI...")
        println("Unsafe SSL mode: $unsafeSsl")

        val client = GigaChatClient(
            authorizationKey = authorizationKey,
            scope = "GIGACHAT_API_PERS",
            unsafeSsl = unsafeSsl
        )

        val models = client.getModels()
        val chat = CliChat(client, models)
        chat.run()
    } catch (t: Throwable) {
        System.err.println()
        System.err.println("Application failed: ${t::class.qualifiedName}: ${t.message}")
        t.printStackTrace()
        exitProcess(1)
    }
}
