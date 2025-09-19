package com.zecobranca.domain.entities

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class WebhookMessage(
  @JsonProperty("id") val id: String?,
  @JsonProperty("from") val from: String?, // remetente do WhatsApp
  @JsonProperty("body") val body: String?, // texto da mensagem
  @JsonProperty("type") val type: String?, // tipo: "received", "receveid_message", "charge_status", etc.
  @JsonProperty("timestamp") val timestamp: Long?,
  @JsonProperty("instanceId") val instanceId: String?, // id da instância
  @JsonProperty("status") val status: Int? = null, // status do ChatPro (ex: 400, 200)
  @JsonProperty("chargeStatus") val chargeStatus: String? = null, // status da cobrança
  @JsonProperty("fromMe") val fromMe: Boolean? = null, // indica se a mensagem foi enviada por nós

  // Mapeamento para estrutura aninhada do ChatPro (formato atual)
  @JsonProperty("Body") val bodyInfo: BodyInfo? = null,

  // *** NOVOS CAMPOS PARA FORMATO DE ARRAY DO CHATPRO ***
  @JsonProperty("cmd") val cmd: String? = null, // comando do webhook (ex: "ack")
  @JsonProperty("ack") val ack: Int? = null, // status do ACK (0-4)
  @JsonProperty("to") val to: String? = null, // destinatário
  @JsonProperty("t") val t: Long? = null, // timestamp em segundos
  @JsonProperty("isSync") val isSync: Boolean? = null, // se é sincronização de dispositivo

  // *** CAMPOS PARA FORMATO SIMPLES ***
  @JsonProperty("number") val number: String? = null, // número do formato simples
  @JsonProperty("message") val message: String? = null, // mensagem do formato simples
  @JsonProperty("quoted_message_id") val quotedMessageId: String? = null, // ID da mensagem citada

  // *** NOVOS CAMPOS PARA DETECÇÃO DE LOOPS ***
  @JsonProperty("messageId") val messageId: String? = null, // ID alternativo da mensagem
  @JsonProperty("pushName") val pushName: String? = null, // nome do contato
  @JsonProperty("participant") val participant: String? = null // participante em grupo
) {

  // *** MELHORADO: Método para obter o ID correto ***
  fun getActualId(): String? =
    bodyInfo?.info?.id ?:
    id ?:
    messageId ?:
    quotedMessageId ?:
    System.currentTimeMillis().toString()

  // *** MELHORADO: Método para obter o remetente correto ***
  fun getActualFrom(): String? {
    val remoteJid = bodyInfo?.info?.remoteJid
    val senderJid = bodyInfo?.info?.senderJid
    val participantJid = participant

    // Limpar JID do WhatsApp
    fun cleanJid(jid: String?): String? =
      jid?.replace("@s.whatsapp.net", "")
        ?.replace("@g.us", "")
        ?.replace("@c.us", "")

    return cleanJid(remoteJid) ?:
    cleanJid(senderJid) ?:
    cleanJid(participantJid) ?:
    cleanJid(from) ?:
    number
  }

  // *** MELHORADO: Método para obter o texto correto ***
  fun getActualBody(): String? =
    bodyInfo?.text ?:
    body ?:
    message

  // *** MELHORADO: Método para obter o timestamp correto ***
  fun getActualTimestamp(): Long? =
    bodyInfo?.info?.timestamp ?:
    timestamp ?:
    t?.let { it * 1000 } // Converter de segundos para milissegundos

  // *** MELHORADO: Método para obter o fromMe correto ***
  fun getActualFromMe(): Boolean? =
    bodyInfo?.info?.fromMe ?:
    fromMe

  // *** MELHORADO: Método para obter o status correto ***
  fun getActualStatus(): Int? =
    bodyInfo?.info?.status ?:
    status ?:
    ack

  // *** MELHORADO: Método para determinar o tipo do webhook ***
  fun getActualType(): String {
    return when {
      // Eventos de ACK têm prioridade
      cmd == "ack" || ack != null -> "ack_update"

      // Corrigir erro de digitação comum do ChatPro
      type == "receveid_message" -> "received"

      // Usar tipo definido se válido
      type != null && type.isNotBlank() && type != "unknown" -> type.lowercase()

      // *** NOVO: Se tem remetente e corpo, assumir como mensagem recebida ***
      getActualFrom() != null && getActualBody() != null && !getActualBody().isNullOrBlank() -> "received"

      // Detectar formato simples (number + message)
      number != null && message != null -> "received"

      // Detectar mensagens do próprio bot
      getActualFromMe() == true -> "sent"

      // Padrão
      else -> "unknown"
    }
  }

  // *** MELHORADO: Método para verificar se é um evento de ACK ***
  fun isAckEvent(): Boolean =
    cmd == "ack" ||
      ack != null ||
      getActualType() == "ack_update"

  // *** NOVO: Método para verificar se é mensagem do próprio bot ***
  fun isBotMessage(): Boolean {
    val messageText = getActualBody()
    return getActualFromMe() == true ||
      messageText?.contains("ZéCobrança 🤖") == true ||
      messageText?.contains("1️⃣") == true ||
      messageText?.contains("2️⃣") == true ||
      messageText?.contains("Consultar Débito") == true ||
      messageText?.contains("Pagamento") == true
  }

  // *** NOVO: Método para verificar se é mensagem de grupo ***
  fun isGroupMessage(): Boolean =
    getActualFrom()?.contains("@g.us") == true ||
      bodyInfo?.info?.remoteJid?.contains("@g.us") == true

  // *** MELHORADO: Método para obter a descrição do status ACK ***
  fun getAckDescription(): String? {
    return when (getActualStatus()) {
      0 -> "Clock - Mensagem ainda não foi enviada"
      1 -> "Sent - Mensagem enviada para o servidor"
      2 -> "Delivered - Mensagem entregue ao dispositivo"
      3 -> "Read - Mensagem lida pelo destinatário"
      4 -> "Played - Mensagem de áudio/vídeo reproduzida"
      else -> null
    }
  }

  // *** NOVO: Método para verificar se deve ser processado ***
  fun shouldBeProcessed(): Boolean {
    return when {
      // Não processar ACKs
      isAckEvent() -> false

      // Não processar mensagens do próprio bot
      isBotMessage() -> false

      // Não processar mensagens sem remetente
      getActualFrom().isNullOrBlank() -> false

      // Não processar mensagens vazias
      getActualBody().isNullOrBlank() -> false

      // Processar apenas mensagens recebidas
      getActualType() == "received" -> true

      // Não processar outros tipos por padrão
      else -> false
    }
  }

  // *** NOVO: Método para obter nome do contato ***
  fun getContactName(): String? =
    bodyInfo?.info?.pushName ?:
    pushName

  // *** NOVO: Método para debug/log ***
  fun toLogString(): String =
    "WebhookMessage(from=${getActualFrom()}, type=${getActualType()}, body='${getActualBody()?.take(50)}...', timestamp=${getActualTimestamp()})"
}

// *** ESTRUTURA ANINHADA DO CHATPRO (FORMATO ATUAL) ***
@JsonIgnoreProperties(ignoreUnknown = true)
data class BodyInfo(
  @JsonProperty("Info") val info: MessageInfo? = null,
  @JsonProperty("Text") val text: String? = null,
  @JsonProperty("ExtendedText") val extendedText: String? = null, // Texto estendido
  @JsonProperty("MediaType") val mediaType: String? = null, // Tipo de mídia
  @JsonProperty("Caption") val caption: String? = null // Legenda de mídia
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MessageInfo(
  @JsonProperty("Id") val id: String? = null,
  @JsonProperty("RemoteJid") val remoteJid: String? = null,
  @JsonProperty("SenderJid") val senderJid: String? = null,
  @JsonProperty("FromMe") val fromMe: Boolean? = null,
  @JsonProperty("Timestamp") val timestamp: Long? = null,
  @JsonProperty("PushName") val pushName: String? = null,
  @JsonProperty("Status") val status: Int? = null,
  @JsonProperty("Participant") val participant: String? = null // Para mensagens de grupo
)
