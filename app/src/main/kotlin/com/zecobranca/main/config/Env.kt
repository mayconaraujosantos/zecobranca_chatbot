package com.zecobranca.main.config

object Env {
    val chatProApiToken: String = System.getenv("CHATPRO_API_TOKEN") ?: "aeb9d7999bbcf9879caed8de1ffb6311"
    val chatProInstanceId: String = System.getenv("CHATPRO_INSTANCE_ID") ?: "chatpro-j7phcrjlmn"
    val chatProApiUrl: String = System.getenv("CHATPRO_API_URL") ?: "https://v5.chatpro.com.br/"
    val port: Int = System.getenv("PORT")?.toIntOrNull() ?: 8080
}