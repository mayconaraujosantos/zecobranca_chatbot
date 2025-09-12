package com.zecobranca.main.config

object Env {
    val chatProApiToken: String = System.getenv("CHATPRO_API_TOKEN") ?: "cd98f539046023b02d5cab608bfd3457"
    val chatProInstanceId: String = System.getenv("CHATPRO_INSTANCE_ID") ?: "chatpro-1vqosd3o0b"
    val chatProApiUrl: String = System.getenv("CHATPRO_API_URL") ?: "https://v5.chatpro.com.br/"
    val port: Int = System.getenv("PORT")?.toIntOrNull() ?: 8080
}
