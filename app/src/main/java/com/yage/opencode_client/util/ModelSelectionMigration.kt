package com.yage.opencode_client.util

internal object ModelSelectionMigration {
    const val CURRENT_SCHEMA_VERSION = 2

    private val legacyReferences = listOf(
        null,
        "openai/gpt-5.6-sol",
        null,
        "ds4/deepseek-v4-flash",
        "deepseek/deepseek-v4-pro",
        null,
        null,
        null,
    )

    fun referenceForLegacyIndex(index: Int): String? = legacyReferences.getOrNull(index)

    fun migrateLegacySessionModels(models: Map<String, String>): Map<String, String> = models.mapNotNull { (sessionId, index) ->
        index.toIntOrNull()?.let(::referenceForLegacyIndex)?.let { reference -> sessionId to reference }
    }.toMap()
}
