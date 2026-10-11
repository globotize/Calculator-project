package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.MathEvaluator
import com.example.model.UnitConverterData
import com.example.ui.theme.CalculatorTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterDialog(
    initialValue: String = "1",
    onDismiss: () -> Unit,
    onInsertResult: (String) -> Unit
) {
    val colors = CalculatorTheme.colors
    val categories = UnitConverterData.CATEGORIES
    var selectedCatIndex by remember { mutableIntStateOf(0) }
    val currentCategory = categories[selectedCatIndex]

    var fromUnitIndex by remember(selectedCatIndex) { mutableIntStateOf(0) }
    var toUnitIndex by remember(selectedCatIndex) {
        mutableIntStateOf(if (currentCategory.units.size > 1) 1 else 0)
    }

    var fromValueStr by remember { mutableStateOf(initialValue.ifEmpty { "1" }) }

    val fromUnit = currentCategory.units.getOrElse(fromUnitIndex) { currentCategory.units.first() }
    val toUnit = currentCategory.units.getOrElse(toUnitIndex) { currentCategory.units.first() }

    val convertedValueStr = remember(fromValueStr, fromUnit, toUnit) {
        val num = fromValueStr.toDoubleOrNull()
        if (num != null) {
            val res = UnitConverterData.convert(num, fromUnit, toUnit)
            MathEvaluator.formatNumber(res)
        } else {
            "--"
        }
    }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, colors.border, RoundedCornerShape(24.dp)),
            color = colors.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Unit Converter",
                            tint = colors.sciKeyAccent
                        )
                        Text(
                            text = "Unit Converter",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.textSecondary
                        )
                    }
                }

                // Category chips row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories.indices.toList()) { idx ->
                        val cat = categories[idx]
                        val isSelected = idx == selectedCatIndex
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) colors.sciKeyAccent else colors.surfaceVariant)
                                .clickable { selectedCatIndex = idx }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) colors.background else colors.textSecondary
                            )
                        }
                    }
                }

                // From Unit Row
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "From", fontSize = 12.sp, color = colors.textSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = fromValueStr,
                            onValueChange = { fromValueStr = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        UnitDropdownSelector(
                            units = currentCategory.units.map { it.symbol },
                            selectedIndex = fromUnitIndex,
                            onSelectIndex = { fromUnitIndex = it },
                            modifier = Modifier.weight(0.9f)
                        )
                    }
                }

                // Swap Button
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            val temp = fromUnitIndex
                            fromUnitIndex = toUnitIndex
                            toUnitIndex = temp
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Swap Units",
                            tint = colors.sciKeyAccent
                        )
                    }
                }

                // To Unit Row
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "To", fontSize = 12.sp, color = colors.textSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, colors.border, RoundedCornerShape(12.dp)),
                            color = colors.surfaceVariant
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 14.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = convertedValueStr,
                                    fontSize = 18.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            }
                        }

                        UnitDropdownSelector(
                            units = currentCategory.units.map { it.symbol },
                            selectedIndex = toUnitIndex,
                            onSelectIndex = { toUnitIndex = it },
                            modifier = Modifier.weight(0.9f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Insert into calculator button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            val cleanVal = convertedValueStr.replace(",", "")
                            if (cleanVal != "--") {
                                onInsertResult(cleanVal)
                                onDismiss()
                            }
                        }
                        .testTag("insert_converted_unit_button"),
                    color = colors.sciKeyAccent
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Insert Result into Calculator",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.background
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitDropdownSelector(
    units: List<String>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = CalculatorTheme.colors
    var expanded by remember { mutableStateOf(false) }
    val currentLabel = units.getOrElse(selectedIndex) { "" }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = currentLabel,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = colors.surfaceVariant
        ) {
            units.forEachIndexed { index, symbol ->
                DropdownMenuItem(
                    text = { Text(symbol, color = colors.textPrimary) },
                    onClick = {
                        onSelectIndex(index)
                        expanded = false
                    }
                )
            }
        }
    }
}
