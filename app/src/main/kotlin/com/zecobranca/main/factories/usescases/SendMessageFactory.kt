package com.zecobranca.main.factories.usescases

import com.zecobranca.data.usecases.SendMessageData
import com.zecobranca.infra.http.OkHttpAdapter
import com.zecobranca.main.config.Env

object SendMessageFactory {
  fun make(): SendMessageData =
          SendMessageData(
                  httpClient = OkHttpAdapter(),
                  chatProApiToken = Env.chatProApiToken,
          )
}
