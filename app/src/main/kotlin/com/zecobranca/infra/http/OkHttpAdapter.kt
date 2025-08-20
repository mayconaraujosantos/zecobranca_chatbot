package com.zecobranca.infra.http

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.zecobranca.data.protocols.http.HttpPostClient
import com.zecobranca.presentation.protocols.HttpResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class OkHttpAdapter : HttpPostClient {
  private val client = OkHttpClient()
  private val mapper = jacksonObjectMapper()
  override fun post(url: String, headers: Map<String, String>, body: Any): HttpResponse {
    val json = mapper.writeValueAsString(body)
    val requestBody = json.toRequestBody("application/json".toMediaType())

    val builder = Request.Builder()
      .url(url)
      .post(requestBody)
    headers.forEach { (key, value) -> builder.addHeader(key, value) }
    val response = client.newCall(builder.build()).execute()
    return HttpResponse(
      statusCode = response.code,
      body = response.body?.string() ?: "",
    )
  }
}
