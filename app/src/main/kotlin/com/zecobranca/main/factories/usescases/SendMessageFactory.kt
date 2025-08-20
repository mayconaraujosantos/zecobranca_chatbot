package com.zecobranca.main.factories.usescases

import com.zecobranca.infra.http.ChatProHttpClient
import com.zecobranca.infra.http.OkHttpAdapter
import com.zecobranca.main.config.Env

object SendMessageFactory {
  fun make(): ChatProHttpClient = ChatProHttpClient(
    httpClient = OkHttpAdapter(),
    apiUrl = Env.chatProApiUrl,
    apiToken = Env.chatProApiToken,
  )
}
