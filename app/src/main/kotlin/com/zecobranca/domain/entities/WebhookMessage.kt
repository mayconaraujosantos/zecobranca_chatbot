package com.zecobranca.domain.entities

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class WebhookMessage(
        val id: String?,
        val from: String?, // remetente do WhatsApp
        val body: String?, // texto da mensagem
        val type: String?, // tipo: "received", "receveid_message", "charge_status", etc.
        val timestamp: Long?,
        val instanceId: String?, // id da instância
        val status: Int? = null, // status do ChatPro (ex: 400, 200)
        val chargeStatus: String? = null, // status da cobrança
        val fromMe: Boolean? =
                null, // indica se a mensagem foi enviada por nós (true) ou pelo usuário (false)

        // Mapeamento para estrutura aninhada do ChatPro
        @JsonProperty("Body") val bodyInfo: BodyInfo? = null
) {
  // Método para obter o ID correto (prioriza Body.Info.Id, depois id direto)
  fun getActualId(): String? = bodyInfo?.info?.id ?: id

  // Método para obter o remetente correto (prioriza Body.Info, depois campo direto)
  fun getActualFrom(): String? {
    val remoteJid = bodyInfo?.info?.remoteJid
    val senderJid = bodyInfo?.info?.senderJid
    return remoteJid?.replace("@s.whatsapp.net", "")?.replace("@g.us", "")
            ?: senderJid?.replace("@s.whatsapp.net", "")?.replace("@g.us", "") ?: from
  }

  // Método para obter o texto correto (prioriza Body.Text, depois body direto)
  fun getActualBody(): String? = bodyInfo?.text ?: body

  // Método para obter o timestamp correto (prioriza Body.Info.Timestamp, depois timestamp direto)
  fun getActualTimestamp(): Long? = bodyInfo?.info?.timestamp ?: timestamp

  // Método para obter o fromMe correto (prioriza Body.Info.fromMe, depois fromMe direto)
  fun getActualFromMe(): Boolean? = bodyInfo?.info?.fromMe ?: fromMe

  // Método para obter o status correto (prioriza Body.Info.status, depois status direto)
  fun getActualStatus(): Int? = bodyInfo?.info?.status ?: status
}

// Estrutura aninhada do ChatPro
@JsonIgnoreProperties(ignoreUnknown = true)
data class BodyInfo(@JsonProperty("Info") val info: MessageInfo? = null, val text: String? = null)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MessageInfo(
        val id: String? = null,
        val remoteJid: String? = null,
        val senderJid: String? = null,
        val fromMe: Boolean? = null,
        val timestamp: Long? = null,
        val pushName: String? = null,
        val status: Int? = null
)
