package com.yage.opencode_client.util

internal object ModelSelectionMigration {
    const val CURRENT_SCHEMA_VERSION = 2

    private val legacyReferences = listOf(
        "zai-coding-plan/glm-5.2",
        "openai/gpt-5.6-sol",
        "google/gemini-3.5-flash",
        "ds4/deepseek-v4-flash",
        "deepseek/deepseek-v4-pro",
        "ollama-cloud/glm-5.2",
        "openai/gpt-5.6-sol-pro",
        "openai/gpt-5.6-sol-fast",
    )

    fun referenceForLegacyIndex(index: Int): String? = legacyReferences.getOrNull(index)

    fun migrateLegacySessionModels(models: Map<String, String>): Map<String, String> = models.mapNotNull { (sessionId, index) ->
        index.toIntOrNull()?.let(::referenceForLegacyIndex)?.let { reference -> sessionId to reference }
    }.toMap()
}
