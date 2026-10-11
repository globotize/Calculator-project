package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AngleMode
import com.example.model.CalculatorState
import com.example.ui.theme.CalculatorTheme

@Composable
fun CalculatorDisplay(
    state: CalculatorState,
    onToggleAngleMode: () -> Unit,
    onMemoryRecall: () -> Unit,
    onInsertText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = CalculatorTheme.colors
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val exprScrollState = rememberScrollState()
    val resScrollState = rememberScrollState()

    // Always scroll to end of expression when typing so latest input is never cut off
    LaunchedEffect(state.expression) {
        exprScrollState.scrollTo(exprScrollState.maxValue)
    }

    LaunchedEffect(state.result) {
        resScrollState.scrollTo(resScrollState.maxValue)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
        color = colors.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Status Bar: Angle Mode, 2nd, HYP, Memory badges, Copy action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Angle Mode Pill (DEG / RAD)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceVariant)
                        .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                        .clickable { onToggleAngleMode() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("angle_mode_pill"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (state.angleMode == AngleMode.DEG) "DEG" else "RAD",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.sciKeyAccent
                    )
                    Text(
                        text = if (state.angleMode == AngleMode.DEG) "RAD" else "DEG",
                        fontSize = 11.sp,
                        color = colors.textSecondary.copy(alpha = 0.5f)
                    )
                }

                // Mode indicators (2nd, HYP, MEM, Copy)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (state.isSecondMode) {
                        StatusIndicatorBadge(label = "2ND", active = true)
                    }
                    if (state.isHypMode) {
                        StatusIndicatorBadge(label = "HYP", active = true)
                    }
                    if (state.memoryValue != null) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.surfaceVariant)
                                .clickable { onMemoryRecall() }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Memory,
                                contentDescription = "Memory Active",
                                tint = colors.operatorText,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "M",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.operatorText
                            )
                        }
                    }

                    // Copy action button
                    val hasContentToCopy = state.result.isNotEmpty() || state.expression.isNotEmpty()
                    if (hasContentToCopy) {
                        IconButton(
                            onClick = {
                                val textToCopy = if (state.result.isNotEmpty()) {
                                    state.result.replace(",", "")
                                } else {
                                    state.expression
                                }
                                clipboardManager.setText(AnnotatedString(textToCopy))
                                Toast.makeText(context, "Copied: $textToCopy", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(colors.surfaceVariant)
                                .testTag("copy_result_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy result",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Display Content Area (Expression + Live Preview / Result)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.End
            ) {
                // When result is evaluated, expression moves to smaller secondary row at top
                val isResultMode = state.result.isNotEmpty()

                // Expression text with guaranteed right alignment and smooth scroll
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(exprScrollState),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val exprFontSize = when {
                        isResultMode -> 18.sp
                        state.expression.length > 25 -> 22.sp
                        state.expression.length > 18 -> 26.sp
                        state.expression.length > 12 -> 30.sp
                        else -> 36.sp
                    }

                    val exprColor = when {
                        isResultMode -> colors.textSecondary
                        state.expression.isEmpty() -> colors.textSecondary.copy(alpha = 0.4f)
                        else -> colors.textPrimary
                    }

                    Text(
                        text = state.expression.ifEmpty { "0" },
                        fontSize = exprFontSize,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isResultMode) FontWeight.Normal else FontWeight.Medium,
                        color = exprColor,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.testTag("expression_display")
                    )
                }

                // Error message
                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage,
                        fontSize = 15.sp,
                        color = colors.clearText,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("error_display")
                    )
                }

                // Live Evaluated Preview while typing (e.g. "= 42")
                if (state.livePreview.isNotEmpty() && !isResultMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "= ${state.livePreview}",
                            fontSize = 22.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.sciKeyAccent.copy(alpha = 0.9f),
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            modifier = Modifier.testTag("live_preview_display")
                        )
                    }
                }

                // Primary Evaluated Result (when '=' is pressed)
                if (isResultMode) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(resScrollState),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val resFontSize = when {
                            state.result.length > 18 -> 26.sp
                            state.result.length > 12 -> 32.sp
                            else -> 40.sp
                        }

                        Text(
                            text = state.result,
                            fontSize = resFontSize,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.testTag("result_display")
                        )
                    }

                    // Alternate representation chips (Fractions, Hex, Bin)
                    state.lastEvaluatedResult?.let { res ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            res.fractionApprox?.let { frac ->
                                ResultChip(
                                    label = "≈ $frac",
                                    onClick = {
                                        onInsertText(frac)
                                        Toast.makeText(context, "Inserted fraction $frac", Toast.LENGTH_SHORT).show()
                                    }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            res.hex?.let { hexStr ->
                                ResultChip(
                                    label = hexStr,
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(hexStr))
                                        Toast.makeText(context, "Copied $hexStr", Toast.LENGTH_SHORT).show()
                                    }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            res.bin?.let { binStr ->
                                ResultChip(
                                    label = binStr,
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(binStr))
                                        Toast.makeText(context, "Copied $binStr", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusIndicatorBadge(label: String, active: Boolean) {
    val colors = CalculatorTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) colors.sciKeyAccent.copy(alpha = 0.2f) else colors.surfaceVariant)
            .border(
                1.dp,
                if (active) colors.sciKeyAccent else colors.border,
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (active) colors.sciKeyAccent else colors.textSecondary
        )
    }
}

@Composable
private fun ResultChip(
    label: String,
    onClick: () -> Unit
) {
    val colors = CalculatorTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surfaceVariant)
            .border(1.dp, colors.border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            color = colors.sciKeyAccent
        )
    }
}
