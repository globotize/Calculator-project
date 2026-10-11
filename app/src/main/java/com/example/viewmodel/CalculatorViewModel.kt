package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.CalculationEntity
import com.example.engine.MathEvaluator
import com.example.model.AngleMode
import com.example.model.CalculatorState
import com.example.model.ConstantItem
import com.example.model.DarkThemeStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val calculationDao = db.calculationDao()

    private val _uiState = MutableStateFlow(CalculatorState())
    val uiState: StateFlow<CalculatorState> = _uiState.asStateFlow()

    val historyList: StateFlow<List<CalculationEntity>> = calculationDao.getAllHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var justEvaluated = false

    fun onAppendToken(token: String) {
        _uiState.update { state ->
            var currentExpr = state.expression
            val hasError = state.errorMessage != null
            var shouldClearResult = false

            // If we just evaluated or had an error
            if (hasError) {
                currentExpr = ""
                justEvaluated = false
                shouldClearResult = true
            } else if (justEvaluated) {
                // If appending an operator, continue with last evaluated result
                if (isOperator(token)) {
                    val cleanResult = state.result.replace(",", "")
                    currentExpr = cleanResult
                } else {
                    // If typing a number or function, start fresh new calculation
                    currentExpr = ""
                }
                justEvaluated = false
                shouldClearResult = true
            }

            // Smart decimal dot handling: prevent consecutive dots or multiple dots in same number
            if (token == ".") {
                if (currentExpr.isEmpty() || isOperator(currentExpr.takeLast(1)) || currentExpr.endsWith("(")) {
                    currentExpr += "0."
                    val preview = MathEvaluator.evaluatePreview(currentExpr, state.angleMode) ?: ""
                    return@update state.copy(
                        expression = currentExpr,
                        result = if (shouldClearResult) "" else state.result,
                        lastEvaluatedResult = if (shouldClearResult) null else state.lastEvaluatedResult,
                        livePreview = preview,
                        errorMessage = null
                    )
                }
                val lastToken = currentExpr.takeLastWhile { it.isDigit() || it == '.' }
                if (lastToken.contains('.')) {
                    return@update state // ignore extra dot
                }
            }

            val newExpr = currentExpr + token
            val preview = MathEvaluator.evaluatePreview(newExpr, state.angleMode) ?: ""

            state.copy(
                expression = newExpr,
                result = if (shouldClearResult) "" else state.result,
                lastEvaluatedResult = if (shouldClearResult) null else state.lastEvaluatedResult,
                livePreview = preview,
                errorMessage = null
            )
        }
    }

    fun onAppendFunction(funcName: String) {
        onAppendToken("$funcName(")
    }

    fun onBackspace() {
        _uiState.update { state ->
            if (state.errorMessage != null) {
                return@update state.copy(errorMessage = null)
            }
            if (justEvaluated) {
                justEvaluated = false
                return@update state.copy(result = "", lastEvaluatedResult = null)
            }
            if (state.expression.isEmpty()) {
                return@update state
            }

            // Check if removing a multi-character function (e.g. "sin(", "asin(", "sqrt(")
            val expr = state.expression
            val multiCharTokens = listOf(
                "sin(", "cos(", "tan(", "asin(", "acos(", "atan(",
                "sinh(", "cosh(", "tanh(", "asinh(", "acosh(", "atanh(",
                "log2(", "log(", "ln(", "sqrt(", "cbrt(", "abs(", "nPr(", "nCr("
            )

            var newExpr = ""
            var matched = false
            for (token in multiCharTokens) {
                if (expr.endsWith(token)) {
                    newExpr = expr.dropLast(token.length)
                    matched = true
                    break
                }
            }
            if (!matched) {
                newExpr = expr.dropLast(1)
            }

            val preview = MathEvaluator.evaluatePreview(newExpr, state.angleMode) ?: ""
            state.copy(
                expression = newExpr,
                result = "",
                lastEvaluatedResult = null,
                livePreview = preview,
                errorMessage = null
            )
        }
    }

    fun onClear() {
        justEvaluated = false
        _uiState.update {
            it.copy(
                expression = "",
                result = "",
                livePreview = "",
                lastEvaluatedResult = null,
                errorMessage = null
            )
        }
    }

    fun onAllClear() {
        justEvaluated = false
        _uiState.update {
            it.copy(
                expression = "",
                result = "",
                livePreview = "",
                lastEvaluatedResult = null,
                errorMessage = null
            )
        }
    }

    fun onEvaluate() {
        val currentState = _uiState.value
        val expr = currentState.expression.trim()
        if (expr.isEmpty()) return

        val evalResult = MathEvaluator.evaluate(expr, currentState.angleMode)

        evalResult.fold(
            onSuccess = { res ->
                justEvaluated = true
                _uiState.update {
                    it.copy(
                        result = res.formattedResult,
                        livePreview = "",
                        lastEvaluatedResult = res,
                        errorMessage = null
                    )
                }
                // Save calculation to Room history
                viewModelScope.launch {
                    try {
                        calculationDao.insert(
                            CalculationEntity(
                                expression = expr,
                                result = res.formattedResult,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    } catch (_: Exception) {}
                }
            },
            onFailure = { error ->
                _uiState.update {
                    it.copy(
                        errorMessage = error.message ?: "Invalid Expression"
                    )
                }
            }
        )
    }

    fun onToggleSign() {
        _uiState.update { state ->
            val expr = state.expression
            if (expr.isEmpty()) {
                return@update state.copy(expression = "-")
            }
            if (expr.startsWith("-(") && expr.endsWith(")")) {
                val unwrapped = expr.removePrefix("-(").removeSuffix(")")
                val preview = MathEvaluator.evaluatePreview(unwrapped, state.angleMode) ?: ""
                state.copy(expression = unwrapped, livePreview = preview)
            } else if (expr.startsWith("-") && !expr.drop(1).contains(Regex("[+\\-×÷*/]"))) {
                val positive = expr.drop(1)
                val preview = MathEvaluator.evaluatePreview(positive, state.angleMode) ?: ""
                state.copy(expression = positive, livePreview = preview)
            } else if (!expr.contains(Regex("[+\\-×÷*/]"))) {
                val negative = "-$expr"
                val preview = MathEvaluator.evaluatePreview(negative, state.angleMode) ?: ""
                state.copy(expression = negative, livePreview = preview)
            } else {
                val wrapped = "-($expr)"
                val preview = MathEvaluator.evaluatePreview(wrapped, state.angleMode) ?: ""
                state.copy(expression = wrapped, livePreview = preview)
            }
        }
    }

    fun onToggleAngleMode() {
        _uiState.update { state ->
            val newMode = if (state.angleMode == AngleMode.DEG) AngleMode.RAD else AngleMode.DEG
            val preview = MathEvaluator.evaluatePreview(state.expression, newMode) ?: ""
            state.copy(angleMode = newMode, livePreview = preview)
        }
    }

    fun onToggleSecondMode() {
        _uiState.update { it.copy(isSecondMode = !it.isSecondMode) }
    }

    fun onToggleHypMode() {
        _uiState.update { it.copy(isHypMode = !it.isHypMode) }
    }

    fun onToggleScientificExpanded() {
        _uiState.update { it.copy(isScientificExpanded = !it.isScientificExpanded) }
    }

    fun onInsertConstant(constant: ConstantItem) {
        onAppendToken(constant.symbol)
    }

    // Memory operations
    fun onMemoryStore() {
        val currVal = getEffectiveCurrentValue() ?: return
        _uiState.update { it.copy(memoryValue = currVal) }
    }

    fun onMemoryAdd() {
        val currVal = getEffectiveCurrentValue() ?: return
        _uiState.update { state ->
            val existing = state.memoryValue ?: 0.0
            state.copy(memoryValue = existing + currVal)
        }
    }

    fun onMemorySubtract() {
        val currVal = getEffectiveCurrentValue() ?: return
        _uiState.update { state ->
            val existing = state.memoryValue ?: 0.0
            state.copy(memoryValue = existing - currVal)
        }
    }

    fun onMemoryRecall() {
        val mem = _uiState.value.memoryValue ?: return
        val formatted = MathEvaluator.formatNumber(mem).replace(",", "")
        onAppendToken(formatted)
    }

    fun onMemoryClear() {
        _uiState.update { it.copy(memoryValue = null) }
    }

    private fun getEffectiveCurrentValue(): Double? {
        val state = _uiState.value
        state.lastEvaluatedResult?.value?.let { return it }
        val preview = MathEvaluator.evaluate(state.expression, state.angleMode).getOrNull()
        return preview?.value
    }

    // Theme & UX
    fun onSetTheme(theme: DarkThemeStyle) {
        _uiState.update { it.copy(activeTheme = theme) }
    }

    fun onToggleHaptic() {
        _uiState.update { it.copy(hapticFeedbackEnabled = !it.hapticFeedbackEnabled) }
    }

    // History interaction
    fun onRestoreHistoryItem(item: CalculationEntity) {
        justEvaluated = false
        val preview = MathEvaluator.evaluatePreview(item.expression, _uiState.value.angleMode) ?: ""
        _uiState.update {
            it.copy(
                expression = item.expression,
                result = item.result,
                livePreview = preview,
                errorMessage = null
            )
        }
    }

    fun onUseHistoryResult(resultStr: String) {
        val clean = resultStr.replace(",", "")
        onAppendToken(clean)
    }

    fun onToggleStarHistory(item: CalculationEntity) {
        viewModelScope.launch {
            calculationDao.update(item.copy(isStarred = !item.isStarred))
        }
    }

    fun onDeleteHistoryItem(item: CalculationEntity) {
        viewModelScope.launch {
            calculationDao.delete(item)
        }
    }

    fun onClearAllHistory() {
        viewModelScope.launch {
            calculationDao.clearAll()
        }
    }

    private fun isOperator(token: String): Boolean {
        return token in setOf("+", "−", "-", "×", "*", "÷", "/", "^", "%", "mod")
    }
}
