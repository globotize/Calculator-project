package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CalculatorState
import com.example.ui.theme.CalculatorTheme

@Composable
fun CalculatorKeypad(
    state: CalculatorState,
    onAppendToken: (String) -> Unit,
    onAppendFunction: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onAllClear: () -> Unit,
    onEvaluate: () -> Unit,
    onToggleSign: () -> Unit,
    onToggleSecondMode: () -> Unit,
    onToggleHypMode: () -> Unit,
    onToggleScientificExpanded: () -> Unit,
    onMemoryStore: () -> Unit,
    onMemoryAdd: () -> Unit,
    onMemorySubtract: () -> Unit,
    onMemoryRecall: () -> Unit,
    onMemoryClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = CalculatorTheme.colors
    val haptic = LocalHapticFeedback.current

    fun playHaptic() {
        if (state.hapticFeedbackEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val sciKeyHeight = 38.dp
    val numKeyHeight = 50.dp

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Scientific Toggle Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Expand / Collapse Scientific Function Drawer
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceVariant)
                    .clickable {
                        playHaptic()
                        onToggleScientificExpanded()
                    }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .testTag("toggle_scientific_button"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (state.isScientificExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Toggle Scientific Functions",
                    tint = colors.sciKeyAccent,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = if (state.isScientificExpanded) "Scientific (Expanded)" else "Scientific",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.sciKeyAccent
                )
            }

            // Quick Parentheses & Exponent Strip
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuickStripButton(label = "(", onClick = { playHaptic(); onAppendToken("(") })
                QuickStripButton(label = ")", onClick = { playHaptic(); onAppendToken(")") })
                QuickStripButton(label = "xʸ", onClick = { playHaptic(); onAppendToken("^") })
                QuickStripButton(label = "√", onClick = { playHaptic(); onAppendFunction("sqrt") })
                QuickStripButton(label = "π", onClick = { playHaptic(); onAppendToken("π") })
            }
        }

        // Expandable Scientific Function Keypad Section
        AnimatedVisibility(
            visible = state.isScientificExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                // Sci Row 1: 2nd, HYP, sin/asin, cos/acos, tan/atan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    CalcKey(
                        text = "2nd",
                        keyHeight = sciKeyHeight,
                        isAccent = state.isSecondMode,
                        bgColor = if (state.isSecondMode) colors.sciKeyAccent.copy(alpha = 0.25f) else colors.sciKey,
                        textColor = colors.sciKeyAccent,
                        onClick = { playHaptic(); onToggleSecondMode() },
                        testTag = "key_2nd"
                    )
                    CalcKey(
                        text = "hyp",
                        keyHeight = sciKeyHeight,
                        isAccent = state.isHypMode,
                        bgColor = if (state.isHypMode) colors.sciKeyAccent.copy(alpha = 0.25f) else colors.sciKey,
                        textColor = colors.sciKeyAccent,
                        onClick = { playHaptic(); onToggleHypMode() },
                        testTag = "key_hyp"
                    )

                    val sinLabel = when {
                        state.isHypMode && state.isSecondMode -> "asinh"
                        state.isHypMode -> "sinh"
                        state.isSecondMode -> "sin⁻¹"
                        else -> "sin"
                    }
                    val sinFunc = when {
                        state.isHypMode && state.isSecondMode -> "asinh"
                        state.isHypMode -> "sinh"
                        state.isSecondMode -> "asin"
                        else -> "sin"
                    }
                    CalcKey(
                        text = sinLabel,
                        keyHeight = sciKeyHeight,
                        bgColor = colors.sciKey,
                        textColor = colors.sciKeyText,
                        onClick = { playHaptic(); onAppendFunction(sinFunc) },
                        testTag = "key_sin"
                    )

                    val cosLabel = when {
                        state.isHypMode && state.isSecondMode -> "acosh"
                        state.isHypMode -> "cosh"
                        state.isSecondMode -> "cos⁻¹"
                        else -> "cos"
                    }
                    val cosFunc = when {
                        state.isHypMode && state.isSecondMode -> "acosh"
                        state.isHypMode -> "cosh"
                        state.isSecondMode -> "acos"
                        else -> "cos"
                    }
                    CalcKey(
                        text = cosLabel,
                        keyHeight = sciKeyHeight,
                        bgColor = colors.sciKey,
                        textColor = colors.sciKeyText,
                        onClick = { playHaptic(); onAppendFunction(cosFunc) },
                        testTag = "key_cos"
                    )

                    val tanLabel = when {
                        state.isHypMode && state.isSecondMode -> "atanh"
                        state.isHypMode -> "tanh"
                        state.isSecondMode -> "tan⁻¹"
                        else -> "tan"
                    }
                    val tanFunc = when {
                        state.isHypMode && state.isSecondMode -> "atanh"
                        state.isHypMode -> "tanh"
                        state.isSecondMode -> "atan"
                        else -> "tan"
                    }
                    CalcKey(
                        text = tanLabel,
                        keyHeight = sciKeyHeight,
                        bgColor = colors.sciKey,
                        textColor = colors.sciKeyText,
                        onClick = { playHaptic(); onAppendFunction(tanFunc) },
                        testTag = "key_tan"
                    )
                }

                // Sci Row 2: ln/eˣ, log/10ˣ, x², x³, 1/x
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    val lnLabel = if (state.isSecondMode) "eˣ" else "ln"
                    CalcKey(
                        text = lnLabel,
                        keyHeight = sciKeyHeight,
                        bgColor = colors.sciKey,
                        textColor = colors.sciKeyText,
                        onClick = {
                            playHaptic()
                            if (state.isSecondMode) onAppendToken("e^") else onAppendFunction("ln")
                        },
                        testTag = "key_ln"
                    )

                    val logLabel = if (state.isSecondMode) "10ˣ" else "log"
                    CalcKey(
                        text = logLabel,
                        keyHeight = sciKeyHeight,
                        bgColor = colors.sciKey,
                        textColor = colors.sciKeyText,
                        onClick = {
                            playHaptic()
                            if (state.isSecondMode) onAppendToken("10^") else onAppendFunction("log")
                        },
                        testTag = "key_log"
                    )

                    CalcKey(
                        text = if (state.isSecondMode) "√x" else "x²",
                        keyHeight = sciKeyHeight,
                        bgColor = colors.sciKey,
                        textColor = colors.sciKeyText,
                        onClick = {
                            playHaptic()
                            if (state.isSecondMode) onAppendFunction("sqrt") else onAppendToken("^2")
                        },
                        testTag = "key_sqr"
                    )

                    CalcKey(
                        text = if (state.isSecondMode) "∛x" else "x³",
                        keyHeight = sciKeyHeight,
                        bgColor = colors.sciKey,
                        textColor = colors.sciKeyText,
                        onClick = {
                            playHaptic()
                            if (state.isSecondMode) onAppendFunction("cbrt") else onAppendToken("^3")
                        },
                        testTag = "key_cube"
                    )

                    CalcKey(
                        text = "1/x",
                        keyHeight = sciKeyHeight,
                        bgColor = colors.sciKey,
                        textColor = colors.sciKeyText,
                        onClick = { playHaptic(); onAppendToken("^(-1)") },
                        testTag = "key_reciprocal"
                    )
                }

                // Sci Row 3: n!, nPr, nCr, |x|, constants (e)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    CalcKey(
                        text = "x!",
                        keyHeight = sciKeyHeight,
                        bgColor = colors.sciKey,
                        textColor = colors.sciKeyText,
                        onClick = { playHaptic(); onAppendToken("!") },
                        testTag = "key_factorial"
                    )
                    CalcKey(
                        text = "nPr",
                        keyHeight = sciKeyHeight,
                        bgColor = colors.sciKey,
                        textColor = colors.sciKeyText,
                        onClick = { playHaptic(); onAppendFunction("nPr") },
                        testTag = "key_npr"
                    )
                    CalcKey(
                        text = "nCr",
                        keyHeight = sciKeyHeight,
                        bgColor = colors.sciKey,
                        textColor = colors.sciKeyText,
                        onClick = { playHaptic(); onAppendFunction("nCr") },
                        testTag = "key_ncr"
                    )
                    CalcKey(
                        text = "|x|",
                        keyHeight = sciKeyHeight,
                        bgColor = colors.sciKey,
                        textColor = colors.sciKeyText,
                        onClick = { playHaptic(); onAppendFunction("abs") },
                        testTag = "key_abs"
                    )
                    CalcKey(
                        text = "e",
                        keyHeight = sciKeyHeight,
                        bgColor = colors.sciKey,
                        textColor = colors.sciKeyText,
                        onClick = { playHaptic(); onAppendToken("e") },
                        testTag = "key_e_const"
                    )
                }

                // Sci Row 4: Memory bar (MC, MR, M+, M-, MS)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    CalcKey(
                        text = "MC",
                        keyHeight = sciKeyHeight,
                        bgColor = colors.surfaceVariant,
                        textColor = colors.textSecondary,
                        onClick = { playHaptic(); onMemoryClear() },
                        testTag = "key_mc"
                    )
                    CalcKey(
                        text = "MR",
                        keyHeight = sciKeyHeight,
                        bgColor = colors.surfaceVariant,
                        textColor = if (state.memoryValue != null) colors.operatorText else colors.textSecondary,
                        onClick = { playHaptic(); onMemoryRecall() },
                        testTag = "key_mr"
                    )
                    CalcKey(
                        text = "M+",
                        keyHeight = sciKeyHeight,
                        bgColor = colors.surfaceVariant,
                        textColor = colors.operatorText,
                        onClick = { playHaptic(); onMemoryAdd() },
                        testTag = "key_m_plus"
                    )
                    CalcKey(
                        text = "M-",
                        keyHeight = sciKeyHeight,
                        bgColor = colors.surfaceVariant,
                        textColor = colors.operatorText,
                        onClick = { playHaptic(); onMemorySubtract() },
                        testTag = "key_m_minus"
                    )
                    CalcKey(
                        text = "MS",
                        keyHeight = sciKeyHeight,
                        bgColor = colors.surfaceVariant,
                        textColor = colors.operatorText,
                        onClick = { playHaptic(); onMemoryStore() },
                        testTag = "key_ms"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Main Primary Keypad (4x5)
        // Row 1: AC, Backspace, %, ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            CalcKey(
                text = "AC",
                keyHeight = numKeyHeight,
                bgColor = colors.clearKey,
                textColor = colors.clearText,
                onClick = { playHaptic(); onAllClear() },
                testTag = "key_ac"
            )
            CalcKey(
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Backspace",
                        tint = colors.clearText,
                        modifier = Modifier.size(20.dp)
                    )
                },
                keyHeight = numKeyHeight,
                bgColor = colors.clearKey,
                onClick = { playHaptic(); onBackspace() },
                testTag = "key_backspace"
            )
            CalcKey(
                text = "%",
                keyHeight = numKeyHeight,
                bgColor = colors.operatorKey,
                textColor = colors.operatorText,
                onClick = { playHaptic(); onAppendToken("%") },
                testTag = "key_percent"
            )
            CalcKey(
                text = "÷",
                keyHeight = numKeyHeight,
                bgColor = colors.operatorKey,
                textColor = colors.operatorText,
                onClick = { playHaptic(); onAppendToken("÷") },
                testTag = "key_divide"
            )
        }

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            CalcKey(text = "7", keyHeight = numKeyHeight, onClick = { playHaptic(); onAppendToken("7") }, testTag = "key_7")
            CalcKey(text = "8", keyHeight = numKeyHeight, onClick = { playHaptic(); onAppendToken("8") }, testTag = "key_8")
            CalcKey(text = "9", keyHeight = numKeyHeight, onClick = { playHaptic(); onAppendToken("9") }, testTag = "key_9")
            CalcKey(
                text = "×",
                keyHeight = numKeyHeight,
                bgColor = colors.operatorKey,
                textColor = colors.operatorText,
                onClick = { playHaptic(); onAppendToken("×") },
                testTag = "key_multiply"
            )
        }

        // Row 3: 4, 5, 6, −
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            CalcKey(text = "4", keyHeight = numKeyHeight, onClick = { playHaptic(); onAppendToken("4") }, testTag = "key_4")
            CalcKey(text = "5", keyHeight = numKeyHeight, onClick = { playHaptic(); onAppendToken("5") }, testTag = "key_5")
            CalcKey(text = "6", keyHeight = numKeyHeight, onClick = { playHaptic(); onAppendToken("6") }, testTag = "key_6")
            CalcKey(
                text = "−",
                keyHeight = numKeyHeight,
                bgColor = colors.operatorKey,
                textColor = colors.operatorText,
                onClick = { playHaptic(); onAppendToken("−") },
                testTag = "key_minus"
            )
        }

        // Row 4: 1, 2, 3, +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            CalcKey(text = "1", keyHeight = numKeyHeight, onClick = { playHaptic(); onAppendToken("1") }, testTag = "key_1")
            CalcKey(text = "2", keyHeight = numKeyHeight, onClick = { playHaptic(); onAppendToken("2") }, testTag = "key_2")
            CalcKey(text = "3", keyHeight = numKeyHeight, onClick = { playHaptic(); onAppendToken("3") }, testTag = "key_3")
            CalcKey(
                text = "+",
                keyHeight = numKeyHeight,
                bgColor = colors.operatorKey,
                textColor = colors.operatorText,
                onClick = { playHaptic(); onAppendToken("+") },
                testTag = "key_plus"
            )
        }

        // Row 5: ±, 0, ., =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            CalcKey(
                text = "±",
                keyHeight = numKeyHeight,
                bgColor = colors.numKey,
                textColor = colors.textSecondary,
                onClick = { playHaptic(); onToggleSign() },
                testTag = "key_plus_minus"
            )
            CalcKey(text = "0", keyHeight = numKeyHeight, onClick = { playHaptic(); onAppendToken("0") }, testTag = "key_0")
            CalcKey(
                text = ".",
                keyHeight = numKeyHeight,
                bgColor = colors.numKey,
                textColor = colors.numKeyText,
                onClick = { playHaptic(); onAppendToken(".") },
                testTag = "key_dot"
            )
            CalcKey(
                text = "=",
                keyHeight = numKeyHeight,
                bgColor = colors.equalsKey,
                textColor = colors.equalsText,
                onClick = { playHaptic(); onEvaluate() },
                testTag = "key_equals"
            )
        }
    }
}

@Composable
private fun RowScope.CalcKey(
    text: String? = null,
    icon: (@Composable () -> Unit)? = null,
    keyHeight: Dp = 50.dp,
    bgColor: Color? = null,
    textColor: Color? = null,
    isAccent: Boolean = false,
    onClick: () -> Unit,
    testTag: String
) {
    val colors = CalculatorTheme.colors
    val effectiveBg = bgColor ?: colors.numKey
    val effectiveText = textColor ?: colors.numKeyText

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .weight(1f)
            .height(keyHeight)
            .scale(if (isPressed) 0.94f else 1.0f)
            .clip(RoundedCornerShape(14.dp))
            .background(effectiveBg)
            .border(
                1.dp,
                if (isAccent) colors.sciKeyAccent else colors.border.copy(alpha = 0.4f),
                RoundedCornerShape(14.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = colors.sciKeyAccent),
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            icon()
        } else if (text != null) {
            val textSize = when {
                keyHeight <= 40.dp && text.length > 3 -> 11.sp
                keyHeight <= 40.dp -> 13.sp
                text.length > 3 -> 14.sp
                else -> 22.sp
            }
            Text(
                text = text,
                fontSize = textSize,
                fontFamily = FontFamily.SansSerif,
                fontWeight = if (text in listOf("=", "+", "−", "×", "÷")) FontWeight.Bold else FontWeight.Medium,
                color = effectiveText
            )
        }
    }
}

@Composable
private fun QuickStripButton(
    label: String,
    onClick: () -> Unit
) {
    val colors = CalculatorTheme.colors
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(colors.surfaceVariant)
            .border(1.dp, colors.border, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            color = colors.sciKeyAccent
        )
    }
}
