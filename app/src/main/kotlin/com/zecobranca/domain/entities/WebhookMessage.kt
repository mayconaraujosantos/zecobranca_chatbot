package com.zecobranca.domain.entities

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class WebhookMessage(
        val id: String?,
        val from: String?, // remetente do WhatsApp
        val body: String?, // texto da mensagem
        val type: String?, // tipo: "received", "charge_status", etc.
        val timestamp: Long?,
        val instanceId: String?, // id da instância
        val status: Int? = null, // status do ChatPro (ex: 400, 200)
        val chargeStatus: String? = null, // status da cobrança
)
