package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CalculatorDisplay
import com.example.ui.components.CalculatorKeypad
import com.example.ui.components.ConstantsDialog
import com.example.ui.components.HistoryBottomSheet
import com.example.ui.components.ThemeSelectorDialog
import com.example.ui.components.UnitConverterDialog
import com.example.ui.theme.CalculatorTheme
import com.example.viewmodel.CalculatorViewModel

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val historyList by viewModel.historyList.collectAsState()
    val colors = CalculatorTheme.colors

    var showHistorySheet by remember { mutableStateOf(false) }
    var showConstantsDialog by remember { mutableStateOf(false) }
    var showConverterDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    val historySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        contentWindowInsets = WindowInsets.statusBars
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Bar with Tools & History
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // App Title & Tag
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(colors.sciKeyAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ƒ(x)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.sciKeyAccent
                            )
                        }
                        Text(
                            text = "SciCalc Pro",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }

                    // Action Icons: History, Constants, Unit Converter, Theme
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = { showHistorySheet = true },
                            modifier = Modifier.testTag("action_history")
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "History Tape",
                                tint = colors.sciKeyAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        IconButton(
                            onClick = { showConstantsDialog = true },
                            modifier = Modifier.testTag("action_constants")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Science,
                                contentDescription = "Constants",
                                tint = colors.sciKeyAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        IconButton(
                            onClick = { showConverterDialog = true },
                            modifier = Modifier.testTag("action_converter")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Unit Converter",
                                tint = colors.sciKeyAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        IconButton(
                            onClick = { showThemeDialog = true },
                            modifier = Modifier.testTag("action_theme")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Appearance & Theme",
                                tint = colors.sciKeyAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Calculator Display Component
                CalculatorDisplay(
                    state = state,
                    onToggleAngleMode = { viewModel.onToggleAngleMode() },
                    onMemoryRecall = { viewModel.onMemoryRecall() },
                    onInsertText = { viewModel.onAppendToken(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Keypad Component
                CalculatorKeypad(
                    state = state,
                    onAppendToken = { viewModel.onAppendToken(it) },
                    onAppendFunction = { viewModel.onAppendFunction(it) },
                    onBackspace = { viewModel.onBackspace() },
                    onClear = { viewModel.onClear() },
                    onAllClear = { viewModel.onAllClear() },
                    onEvaluate = { viewModel.onEvaluate() },
                    onToggleSign = { viewModel.onToggleSign() },
                    onToggleSecondMode = { viewModel.onToggleSecondMode() },
                    onToggleHypMode = { viewModel.onToggleHypMode() },
                    onToggleScientificExpanded = { viewModel.onToggleScientificExpanded() },
                    onMemoryStore = { viewModel.onMemoryStore() },
                    onMemoryAdd = { viewModel.onMemoryAdd() },
                    onMemorySubtract = { viewModel.onMemorySubtract() },
                    onMemoryRecall = { viewModel.onMemoryRecall() },
                    onMemoryClear = { viewModel.onMemoryClear() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // History Sheet
    if (showHistorySheet) {
        HistoryBottomSheet(
            sheetState = historySheetState,
            historyList = historyList,
            onDismiss = { showHistorySheet = false },
            onRestoreItem = { viewModel.onRestoreHistoryItem(it) },
            onUseResult = { viewModel.onUseHistoryResult(it) },
            onToggleStar = { viewModel.onToggleStarHistory(it) },
            onDeleteItem = { viewModel.onDeleteHistoryItem(it) },
            onClearAllHistory = { viewModel.onClearAllHistory() }
        )
    }

    // Constants Dialog
    if (showConstantsDialog) {
        ConstantsDialog(
            onDismiss = { showConstantsDialog = false },
            onSelectConstant = { viewModel.onInsertConstant(it) }
        )
    }

    // Unit Converter Dialog
    if (showConverterDialog) {
        UnitConverterDialog(
            initialValue = state.result.ifEmpty { "1" },
            onDismiss = { showConverterDialog = false },
            onInsertResult = { viewModel.onAppendToken(it) }
        )
    }

    // Theme Selector Dialog
    if (showThemeDialog) {
        ThemeSelectorDialog(
            currentTheme = state.activeTheme,
            hapticEnabled = state.hapticFeedbackEnabled,
            onSelectTheme = { viewModel.onSetTheme(it) },
            onToggleHaptic = { viewModel.onToggleHaptic() },
            onDismiss = { showThemeDialog = false }
        )
    }
}
