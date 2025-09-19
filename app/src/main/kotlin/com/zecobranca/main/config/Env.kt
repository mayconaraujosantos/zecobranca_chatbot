package com.zecobranca.main.config

object Env {
    val chatProApiToken: String = System.getenv("CHATPRO_API_TOKEN") ?: ""
    val chatProInstanceId: String = System.getenv("CHATPRO_INSTANCE_ID") ?: ""
    val chatProApiUrl: String = System.getenv("CHATPRO_API_URL") ?: "https://v5.chatpro.com.br/"
    val port: Int = System.getenv("PORT")?.toIntOrNull() ?: 8080
}
