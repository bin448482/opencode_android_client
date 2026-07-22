package com.yage.opencode_client

import com.yage.opencode_client.util.ModelSelectionMigration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ModelSelectionMigrationTest {

    @Test
    fun `legacy indices preserve only models that remain in the curated whitelist`() {
        val expected = listOf(
            null,
            "openai/gpt-5.6-sol",
            null,
            "ds4/deepseek-v4-flash",
            "deepseek/deepseek-v4-pro",
            null,
            null,
            null,
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
                "local" to "3",
                "pro" to "4",
                "removed" to "0",
                "invalid" to "not-an-index",
            )
        )

        assertEquals(
            mapOf(
                "gpt" to "openai/gpt-5.6-sol",
                "local" to "ds4/deepseek-v4-flash",
                "pro" to "deepseek/deepseek-v4-pro",
            ),
            migrated
        )
    }
}
