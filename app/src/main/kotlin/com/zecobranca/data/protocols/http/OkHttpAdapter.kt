package com.zecobranca.data.protocols.http

import com.fasterxml.jackson.databind.ObjectMapper
import com.zecobranca.presentation.protocols.HttpResponse
import java.util.concurrent.TimeUnit
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class OkHttpAdapter : HttpPostClient {
  private val client =
          OkHttpClient.Builder()
                  .connectTimeout(10, TimeUnit.SECONDS)
                  .readTimeout(30, TimeUnit.SECONDS)
                  .writeTimeout(30, TimeUnit.SECONDS)
                  .build()
  private val mapper = ObjectMapper()

  override fun post(
          url: String,
          headers: Map<String, String>,
          body: Any,
  ): HttpResponse {
    val json = mapper.writeValueAsString(body)
    val reqBody = json.toRequestBody("application/json".toMediaType())

    val builder = Request.Builder().url(url).post(reqBody)
    headers.forEach { (k, v) -> builder.addHeader(k, v) }

    val resp = client.newCall(builder.build()).execute()
    return HttpResponse(statusCode = resp.code, body = resp.body?.string() ?: "")
  }
}
