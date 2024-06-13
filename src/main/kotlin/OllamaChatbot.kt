fun main() {
    val ollamaClient = OllamaClient()
    val conversationHandler = ConversationHandler(ollamaClient)
    conversationHandler.start()
}
