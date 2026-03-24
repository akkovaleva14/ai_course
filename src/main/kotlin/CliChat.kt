import java.io.BufferedReader
import java.io.InputStreamReader

class CliChat(
    private val client: GigaChatClient,
    availableModels: List<String>
) {
    private val input = BufferedReader(InputStreamReader(System.`in`, Charsets.UTF_8))
    private val history = mutableListOf<RequestMessage>()

    private var systemPrompt: String = "Ты полезный AI-ассистент."
    private var temperature: Double = 0.87
    private var selectedModel: String = chooseDefaultModel(availableModels)

    private val modelAliases = buildModelAliases(availableModels)

    fun run() {
        println("GigaChat CLI Chat")
        printState()
        printHelp()

        while (true) {
            print("You: ")
            System.out.flush()

            val raw = try {
                input.readLine()
            } catch (e: Exception) {
                throw RuntimeException("Failed to read console input: ${e.message}", e)
            }

            if (raw == null) {
                println()
                println("Input stream closed. Exiting chat.")
                return
            }

            val line = raw.trim()

            if (line.isBlank()) {
                continue
            }

            val parts = line.split("\\s+".toRegex(), limit = 2)
            val command = parts[0]
            val argument = parts.getOrElse(1) { "" }.trim()

            when (command.lowercase()) {
                "/quit" -> {
                    println("Bye!")
                    return
                }

                "/help" -> {
                    printHelp()
                }

                "/history" -> {
                    printHistory()
                }

                "/model" -> {
                    if (argument.isBlank()) {
                        println("Usage: /model Lite|Pro|Max")
                    } else {
                        handleModelCommand(argument)
                    }
                }

                "/temp" -> {
                    if (argument.isBlank()) {
                        println("Usage: /temp <0..2>")
                    } else {
                        handleTempCommand(argument)
                    }
                }

                "/system" -> {
                    if (argument.isBlank()) {
                        println("Usage: /system <text>")
                    } else {
                        handleSystemCommand(argument)
                    }
                }

                else -> {
                    handleUserMessage(line)
                }
            }
        }
    }

    private fun handleModelCommand(argument: String) {
        val resolved = modelAliases[argument.lowercase()]

        if (resolved == null) {
            println("Unknown model: $argument")
            println("Available aliases: Lite, Pro, Max")
            println("Available actual models: ${modelAliases.values.distinct().joinToString()}")
            return
        }

        selectedModel = resolved
        println("Model switched to $selectedModel")
        printState()
    }

    private fun handleTempCommand(argument: String) {
        val value = argument.toDoubleOrNull()
        if (value == null || value < 0.0 || value > 2.0) {
            println("Temperature must be a number from 0 to 2")
            return
        }

        temperature = value
        println("Temperature set to $temperature")
        printState()
    }

    private fun handleSystemCommand(argument: String) {
        if (argument.isBlank()) {
            println("System prompt cannot be empty")
            return
        }

        systemPrompt = argument
        println("System prompt updated")
        printState()
    }

    private fun handleUserMessage(input: String) {
        history += RequestMessage(role = "user", content = input)

        val requestMessages = buildList {
            add(RequestMessage(role = "system", content = systemPrompt))
            addAll(history)
        }

        val answer = client.ask(
            model = selectedModel,
            temperature = temperature,
            messages = requestMessages
        )

        history += RequestMessage(role = "assistant", content = answer)
        println("GigaChat: $answer")
    }

    private fun printState() {
        println("Model: $selectedModel, Temp: $temperature, System: \"$systemPrompt\"")
    }

    private fun printHelp() {
        println("Commands:")
        println("/model Lite|Pro|Max")
        println("/temp <0..2>")
        println("/system <text>")
        println("/history")
        println("/help")
        println("/quit")
    }

    private fun printHistory() {
        if (history.isEmpty()) {
            println("History is empty")
            return
        }

        println("History:")
        history.forEachIndexed { index, message ->
            println("${index + 1}. ${message.role}: ${message.content}")
        }
    }

    private fun chooseDefaultModel(models: List<String>): String {
        return models.firstOrNull { it.equals("GigaChat", ignoreCase = true) }
            ?: models.firstOrNull { it.contains("Lite", ignoreCase = true) }
            ?: models.first()
    }

    private fun buildModelAliases(models: List<String>): Map<String, String> {
        val gigaModels = models.filter { it.startsWith("GigaChat", ignoreCase = true) }

        fun pick(vararg candidates: String): String? {
            return candidates.firstNotNullOfOrNull { candidate ->
                gigaModels.firstOrNull { it.equals(candidate, ignoreCase = true) }
            }
        }

        val lite = pick("GigaChat", "GigaChat-2")
        val pro = pick("GigaChat-Pro", "GigaChat-2-Pro", "GigaChat-Pro-preview")
        val max = pick("GigaChat-Max", "GigaChat-2-Max", "GigaChat-Max-preview")

        val result = mutableMapOf<String, String>()

        lite?.let {
            result["lite"] = it
            result["gigachat"] = it
        }

        pro?.let {
            result["pro"] = it
        }

        max?.let {
            result["max"] = it
        }

        gigaModels.forEach { model ->
            result[model.lowercase()] = model
        }

        return result
    }
}
