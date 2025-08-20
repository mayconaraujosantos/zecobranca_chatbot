package com.zecobranca.data.usecases

import com.zecobranca.domain.entities.ConversationStepEnum
import com.zecobranca.domain.entities.WebhookMessage
import com.zecobranca.domain.usecases.ProcessWebhookMessageData
import com.zecobranca.domain.usecases.SendMessage
import com.zecobranca.infra.db.memory.MemoryConversationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProcessWebhookMessageDataTest {
  private lateinit var repo: MemoryConversationRepository
  private lateinit var sendMessage: SendMessage
  private lateinit var sut: ProcessWebhookMessageData

  @BeforeEach
  fun setUp() {
    repo = MemoryConversationRepository()
    sendMessage = mockk()
    sut = ProcessWebhookMessageData(sendMessage, repo)
  }

  @Test
  fun `should show menu when message is not recognized in INITIAL_MENU`() = runTest {
    coEvery { sendMessage.send(any()) } returns true

    val input = WebhookMessage(
      instanceId = "inst",
      from = "5511999999999",
      body = "Hello!",
      id = "some-unique-id",
      type = "received",
      timestamp = System.currentTimeMillis(),
    )

    val result = sut.process(input)

    assertTrue(result.success)
    val state = repo.loadByUserId("5511999999999")
    assertEquals(ConversationStepEnum.MENU_INICIAL, state?.step)

    coVerify {
      sendMessage.send(
        withArg { msg ->
          assertEquals("5511999999999", msg.to)
          assertTrue(msg.text.contains("1"))
          assertTrue(msg.text.contains("2"))
        },
      )
    }
  }
}
