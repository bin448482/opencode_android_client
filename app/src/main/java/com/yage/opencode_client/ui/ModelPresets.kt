package com.yage.opencode_client.ui

/**
 * Curated model presets for the model selector, matching iOS implementation.
 * Only the visible subset is shown in the dropdown instead of the full API list.
 */
object ModelPresets {
    private val allPersonalPresets: List<AppState.ModelOption> = listOf(
        AppState.ModelOption("GLM-5.2", "zai-coding-plan", "glm-5.2"),
        AppState.ModelOption("GPT-5.6 Sol", "openai", "gpt-5.6-sol"),
        AppState.ModelOption("Gemini 3.5 Flash", "google", "gemini-3.5-flash"),
        AppState.ModelOption("DeepSeek Local", "ds4", "deepseek-v4-flash"),
        AppState.ModelOption("DeepSeek V4 Pro", "deepseek", "deepseek-v4-pro"),
        AppState.ModelOption("Ollama GLM 5.2", "ollama-cloud", "glm-5.2"),
        AppState.ModelOption("GPT-5.6 Sol Pro", "openai", "gpt-5.6-sol-pro"),
        AppState.ModelOption("GPT-5.6 Sol Fast", "openai", "gpt-5.6-sol-fast"),
        AppState.ModelOption("Kimi K3", "kimi-for-coding", "k3"),
    )

    private val hiddenReferences = setOf(
        "zai-coding-plan/glm-5.2",
        "google/gemini-3.5-flash",
        "ollama-cloud/glm-5.2",
        "openai/gpt-5.6-sol-pro",
        "openai/gpt-5.6-sol-fast",
    )

    val list: List<AppState.ModelOption> = allPersonalPresets.filterNot {
        reference(it) in hiddenReferences
    }

    fun reference(providerId: String, modelId: String): String = "$providerId/$modelId"

    fun reference(model: AppState.ModelOption): String = reference(model.providerId, model.modelId)

    fun findByReference(modelReference: String?): AppState.ModelOption? = list.firstOrNull {
        reference(it) == modelReference
    }
}
