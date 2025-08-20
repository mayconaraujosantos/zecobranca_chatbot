package com.zecobranca.main.adapters

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.zecobranca.presentation.protocols.Controller
import com.zecobranca.presentation.protocols.HttpRequest
import io.javalin.http.Context
import org.slf4j.LoggerFactory

object JavalinRouteAdapter {

  private val mapper = jacksonObjectMapper()

  suspend fun adapt(controller: Controller): (Context) -> Unit = { ctx ->
    val logger = LoggerFactory.getLogger("Adapter")

    kotlinx.coroutines.runBlocking {
      try {
        val httpRequest = HttpRequest(
          body = ctx.body(),
          headers = ctx.headerMap(),
          params = ctx.pathParamMap(),
          query = ctx.queryParamMap().mapValues { it.value.firstOrNull() ?: "" },
        )

        val httpResponse = controller.handle(httpRequest)
        ctx.status(httpResponse.statusCode)
        httpResponse.headers.forEach { (key, value) -> ctx.header(key, value) }

        when (httpResponse.body) {
          is String -> ctx.result(httpResponse.body)
          else -> {
            try {
              ctx.json(httpResponse.body)
            } catch (e: Exception) {
              logger.error("Failed to serialize response body as JSON", e)
              ctx.status(500).result("Internal server error")
            }
          }
        }
      } catch (e: Exception) {
        logger.error("Error processing request", e)
        ctx.status(500).json(mapOf("error" to "Internal server error"))
      }
    }
  }
}
