import kotlinx.coroutines.*

class ConversationHandler(private val ollamaClient: OllamaClient) {
    private val conversationHistory = mutableListOf<String>()

    fun start() = runBlocking {
        while (true) {
            print("You: ")
            val userInput = readLine()
            if (userInput.isNullOrEmpty()) break
            conversationHistory.add("You: $userInput")
            val context = conversationHistory.joinToString("\n")

            var completeResponse = ""
            ollamaClient.streamResponse(
                context,
                onResponse = { responseFragment ->
                    completeResponse += responseFragment
                    print("\rOllama: $completeResponse")
                },
                onComplete = {
                    println() // Move to the next line after completion
                    conversationHistory.add("Ollama: $completeResponse")
                    print("You: ")
                },
                onError = { e ->
                    println("\nOllama: Error - ${e.message}")
                    print("You: ")
                }
            )
        }
    }
}
