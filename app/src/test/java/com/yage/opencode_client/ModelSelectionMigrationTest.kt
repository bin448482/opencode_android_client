package com.yage.opencode_client

import com.yage.opencode_client.util.ModelSelectionMigration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ModelSelectionMigrationTest {

    @Test
    fun `legacy indices preserve their original model references`() {
        val expected = listOf(
            "zai-coding-plan/glm-5.2",
            "openai/gpt-5.6-sol",
            "google/gemini-3.5-flash",
            "ds4/deepseek-v4-flash",
            "deepseek/deepseek-v4-pro",
            "ollama-cloud/glm-5.2",
            "openai/gpt-5.6-sol-pro",
            "openai/gpt-5.6-sol-fast",
        )

        expected.forEachIndexed { index, reference ->
            assertEquals(reference, ModelSelectionMigration.referenceForLegacyIndex(index))
        }
    }

    @Test
    fun `unknown legacy indices are not assigned a replacement model`() {
        assertNull(ModelSelectionMigration.referenceForLegacyIndex(-1))
        assertNull(ModelSelectionMigration.referenceForLegacyIndex(8))
    }

    @Test
    fun `legacy session model map retains only valid model references`() {
        val migrated = ModelSelectionMigration.migrateLegacySessionModels(
            mapOf(
                "gpt" to "1",
                "glm" to "0",
                "gemini" to "2",
                "local" to "3",
                "pro" to "4",
                "ollama" to "5",
                "sol-pro" to "6",
                "sol-fast" to "7",
                "invalid" to "not-an-index",
            )
        )

        assertEquals(
            mapOf(
                "gpt" to "openai/gpt-5.6-sol",
                "glm" to "zai-coding-plan/glm-5.2",
                "gemini" to "google/gemini-3.5-flash",
                "local" to "ds4/deepseek-v4-flash",
                "pro" to "deepseek/deepseek-v4-pro",
                "ollama" to "ollama-cloud/glm-5.2",
                "sol-pro" to "openai/gpt-5.6-sol-pro",
                "sol-fast" to "openai/gpt-5.6-sol-fast",
            ),
            migrated
        )
    }
}
