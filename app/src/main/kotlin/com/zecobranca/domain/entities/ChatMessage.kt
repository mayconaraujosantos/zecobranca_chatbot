package com.zecobranca.domain.entities

data class ChatMessage(
  val to: String, // número do WhatsApp do destinatário
  val text: String, // mensagem
  val instanceId: String, // id da instância do ChatPro
)
