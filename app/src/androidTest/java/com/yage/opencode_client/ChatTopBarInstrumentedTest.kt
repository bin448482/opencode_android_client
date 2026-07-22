package com.yage.opencode_client

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.yage.opencode_client.ui.AppState
import com.yage.opencode_client.ui.chat.ChatTopBar
import com.yage.opencode_client.ui.chat.ChatTopBarActions
import com.yage.opencode_client.ui.chat.ChatTopBarState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class ChatTopBarInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun modelMenuShowsServerDefaultAndScrollsToAllPublishedModels() {
        val models = (1..32).map { index ->
            AppState.ModelOption("Model $index", "catalog", "model-$index")
        }
        val selectedReference = AtomicReference<String?>(null)
        val defaultSelections = AtomicInteger(0)

        composeRule.setContent {
            MaterialTheme {
                ChatTopBar(
                    state = ChatTopBarState(
                        sessions = emptyList(),
                        currentSessionId = null,
                        sessionStatuses = emptyMap(),
                        hasMoreSessions = false,
                        isLoadingMoreSessions = false,
                        availableModels = models,
                        selectedModelReference = null,
                        contextUsage = null
                    ),
                    actions = ChatTopBarActions(
                        onSelectSession = {},
                        onCreateSession = {},
                        onDeleteSession = {},
                        onLoadMoreSessions = {},
                        onSelectModel = { selectedReference.set(it.reference) },
                        onSelectServerDefault = { defaultSelections.incrementAndGet() }
                    )
                )
            }
        }

        composeRule.onNodeWithTag("chat.model_selector").performClick()
        composeRule.onNodeWithTag("chat.model_menu")
            .performScrollToNode(hasText("Model 32"))
        composeRule.onNodeWithTag("chat.model.catalog/model-32")
            .assertIsDisplayed()
            .performClick()

        assertEquals("catalog/model-32", selectedReference.get())

        composeRule.onNodeWithTag("chat.model_selector").performClick()
        composeRule.onNodeWithTag("chat.model_server_default").performClick()

        assertEquals(1, defaultSelections.get())
    }
}
