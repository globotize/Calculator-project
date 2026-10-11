package com.example.model

enum class AngleMode {
    DEG,
    RAD
}

enum class DarkThemeStyle(val displayName: String) {
    CYBER_DARK("Cyber Slate"),
    OLED_BLACK("OLED Black"),
    MIDNIGHT_NAVY("Midnight Navy"),
    LIGHT("Minimal Light")
}

data class ConstantItem(
    val symbol: String,
    val name: String,
    val valueString: String,
    val category: String,
    val unit: String = ""
)

data class EvaluationResult(
    val value: Double,
    val formattedResult: String,
    val fractionApprox: String? = null,
    val hex: String? = null,
    val bin: String? = null,
    val oct: String? = null
)

data class CalculatorState(
    val expression: String = "",
    val result: String = "",
    val livePreview: String = "",
    val lastEvaluatedResult: EvaluationResult? = null,
    val angleMode: AngleMode = AngleMode.DEG,
    val isSecondMode: Boolean = false,
    val isHypMode: Boolean = false,
    val memoryValue: Double? = null,
    val activeTheme: DarkThemeStyle = DarkThemeStyle.CYBER_DARK,
    val hapticFeedbackEnabled: Boolean = true,
    val isScientificExpanded: Boolean = true,
    val errorMessage: String? = null
)
