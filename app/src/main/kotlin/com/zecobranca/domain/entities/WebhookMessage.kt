package com.zecobranca.domain.entities

data class WebhookMessage(
  val id: String?,
  val from: String?, // remetente do WhatsApp
  val body: String?, // texto da mensagem
  val type: String?, // tipo: "received"
  val timestamp: Long?,
  val instanceId: String?, // id da instância
)
