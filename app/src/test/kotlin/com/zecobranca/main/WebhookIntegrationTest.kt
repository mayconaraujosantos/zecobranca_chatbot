package com.zecobranca.main

import com.zecobranca.main.config.Application.setupApp
import io.javalin.Javalin
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class WebhookIntegrationTest {
  private lateinit var app: Javalin
  private val client = HttpClient.newHttpClient()
  private val port = 8080

  @BeforeEach
  fun setup() {
    runBlocking {
      app = setupApp()
      app.start(port)
    }
  }

  @AfterEach
  fun tearDown() {
    app.stop()
  }

  @Test
  fun `should return 200 for health check`() = runTest {
    val request =
            HttpRequest.newBuilder().uri(URI.create("http://localhost:$port/health")).GET().build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())

    assert(response.statusCode() == 200)
    assert(response.body().contains("OK"))
  }
}
