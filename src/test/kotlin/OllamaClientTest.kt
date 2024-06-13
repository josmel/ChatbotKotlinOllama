import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class OllamaClientTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var ollamaClient: OllamaClient

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        ollamaClient = OllamaClient().apply {
            val baseUrlField = this::class.java.getDeclaredField("baseUrl")
            baseUrlField.isAccessible = true
            baseUrlField.set(this, mockWebServer.url("/api/generate").toString())
        }
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `test streamResponse returns expected response`() {
        val responseChunks = listOf(
            JSONObject().put("response", "Hello").toString(),
            JSONObject().put("response", " there").toString(),
            JSONObject().put("response", ", how are you?").toString()
        )

        responseChunks.forEach { chunk ->
            mockWebServer.enqueue(MockResponse().setBody(chunk).setResponseCode(200))
        }

        val completeResponse = StringBuilder()
        val onCompleteCalled = arrayOf(false)

        ollamaClient.streamResponse(
            prompt = "hello",
            onResponse = { responseFragment ->
                completeResponse.append(responseFragment)
            },
            onComplete = {
                onCompleteCalled[0] = true
                assertEquals("Hello there, how are you?", completeResponse.toString())
            },
            onError = { e ->
                throw AssertionError("Error in streaming response", e)
            }
        )

        Thread.sleep(1000) // Esperar un poco para asegurarse de que la respuesta se procese
        assertEquals(true, onCompleteCalled[0])
    }

    @Test
    fun `test streamResponse handles error`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("Internal Server Error"))

        var errorCalled = false

        ollamaClient.streamResponse(
            prompt = "hello",
            onResponse = { _ ->
                throw AssertionError("This should not be called on error")
            },
            onComplete = {
                throw AssertionError("This should not be called on error")
            },
            onError = { e ->
                errorCalled = true
                assertNotNull(e)
            }
        )

        Thread.sleep(1000) // Esperar un poco para asegurarse de que la respuesta se procese
        assertEquals(true, errorCalled)
    }
}
