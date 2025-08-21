package com.zecobranca.domain.entities

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class WebhookMessage(
        @JsonProperty("id") val id: String?,
        @JsonProperty("from") val from: String?, // remetente do WhatsApp
        @JsonProperty("body") val body: String?, // texto da mensagem
        @JsonProperty("type")
        val type: String?, // tipo: "received", "receveid_message", "charge_status", etc.
        @JsonProperty("timestamp") val timestamp: Long?,
        @JsonProperty("instanceId") val instanceId: String?, // id da instância
        @JsonProperty("status") val status: Int? = null, // status do ChatPro (ex: 400, 200)
        @JsonProperty("chargeStatus") val chargeStatus: String? = null, // status da cobrança
        @JsonProperty("fromMe")
        val fromMe: Boolean? =
                null, // indica se a mensagem foi enviada por nós (true) ou pelo usuário (false)

        // Mapeamento para estrutura aninhada do ChatPro (formato atual)
        @JsonProperty("Body") val bodyInfo: BodyInfo? = null,

        // Mapeamento para formato oficial do ChatPro (array format)
        @JsonProperty("cmd") val cmd: String? = null, // comando do webhook (ex: "ack")
        @JsonProperty("ack") val ack: Int? = null, // status do ACK (0-4)
        @JsonProperty("to") val to: String? = null, // destinatário
        @JsonProperty("t") val t: Long? = null, // timestamp em segundos
        @JsonProperty("isSync") val isSync: Boolean? = null // se é sincronização de dispositivo
) {
  // Método para obter o ID correto (prioriza Body.Info.Id, depois id direto)
  fun getActualId(): String? = bodyInfo?.info?.id ?: id

  // Método para obter o remetente correto (prioriza Body.Info, depois campo direto)
  fun getActualFrom(): String? {
    val remoteJid = bodyInfo?.info?.remoteJid
    val senderJid = bodyInfo?.info?.senderJid
    return remoteJid?.replace("@s.whatsapp.net", "")?.replace("@g.us", "")
            ?: senderJid?.replace("@s.whatsapp.net", "")?.replace("@g.us", "")
                    ?: from?.replace("@s.whatsapp.net", "")?.replace("@g.us", "")
  }

  // Método para obter o texto correto (prioriza Body.Text, depois body direto)
  fun getActualBody(): String? = bodyInfo?.text ?: body

  // Método para obter o timestamp correto (prioriza Body.Info.Timestamp, depois timestamp direto,
  // depois t)
  fun getActualTimestamp(): Long? =
          bodyInfo?.info?.timestamp
                  ?: timestamp ?: t?.let { it * 1000 } // converter segundos para milissegundos

  // Método para obter o fromMe correto (prioriza Body.Info.fromMe, depois fromMe direto)
  fun getActualFromMe(): Boolean? = bodyInfo?.info?.fromMe ?: fromMe

  // Método para obter o status correto (prioriza Body.Info.status, depois status direto, depois
  // ack)
  fun getActualStatus(): Int? = bodyInfo?.info?.status ?: status ?: ack

  // Método para determinar o tipo do webhook baseado no comando e estrutura
  fun getActualType(): String {
    return when {
      cmd == "ack" -> "ack_update"
      type == "receveid_message" -> "received"
      type != null -> type!!
      else -> "unknown"
    }
  }

  // Método para verificar se é um evento de ACK
  fun isAckEvent(): Boolean = cmd == "ack" || ack != null

  // Método para obter a descrição do status ACK
  fun getAckDescription(): String? {
    return when (getActualStatus()) {
      0 -> "Clock - Mensagem ainda não foi enviada"
      1 -> "Sent - Mensagem enviada"
      2 -> "Received - Mensagem recebida"
      3 -> "Read - Mensagem lida"
      4 -> "Played - Áudio foi reproduzido"
      else -> null
    }
  }
}

// Estrutura aninhada do ChatPro (formato atual)
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
