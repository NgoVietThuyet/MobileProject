package com.example.test.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.test.data.LocalCategoryDataSource
import com.example.test.ui.components.AppHeader
import com.example.test.vm.AddBudgetViewModel
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetScreen(
    onBack: () -> Unit,
    onCancel: () -> Unit,
    onCreate: () -> Unit,
    viewModel: AddBudgetViewModel = hiltViewModel()
) {
    val ui by viewModel.ui.collectAsState()

    var categoryId by remember { mutableStateOf<String?>(null) }
    var budgetName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    val ym = YearMonth.now()
    val startDate = ym.atDay(1).format(DateTimeFormatter.ISO_DATE)
    val endDate = ym.atEndOfMonth().format(DateTimeFormatter.ISO_DATE)

    Scaffold(
        topBar = {
            AppHeader(
                title = "Thêm ngân sách",
                showBack = true,
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {

            Text("Tên ngân sách")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = budgetName,
                onValueChange = { budgetName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Tên ngân sách") },
                placeholder = { Text("Nhập ngân sách...") },
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            Text("Số tiền cho ngân sách")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = { newValue ->
                    amount = newValue.filter { it.isDigit() }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Số tiền (VND)") },
                placeholder = { Text("Nhập số tiền...") },
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            Text("Chọn biểu tượng")
            Spacer(Modifier.height(8.dp))

            CategoryPickerGrid(
                selected = categoryId,
                onSelect = { id, name ->
                    categoryId = id
                    budgetName = name
                },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.height(16.dp))

            if (ui.error != null) {
                Text(ui.error ?: "", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    enabled = !ui.isSubmitting
                ) { Text("Huỷ") }

                Button(
                    onClick = {
                        val cat = categoryId ?: return@Button
                        viewModel.submit(
                            categoryId = cat,
                            amountVnd = amount.trim(),
                            startDate = startDate,
                            endDate = endDate
                        )
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !ui.isSubmitting && amount.isNotBlank() && categoryId != null
                ) {
                    Text(if (ui.isSubmitting) "Đang tạo..." else "Tạo ngân sách")
                }
            }

            if (ui.success) onCreate()
        }
    }
}

@Composable
private fun CategoryPickerGrid(
    selected: String?,
    onSelect: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = LocalCategoryDataSource.expenseOnly()

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
    ) {
        items(categories) { cat ->
            FilterChip(
                selected = selected == cat.categoryId,
                onClick = { onSelect(cat.categoryId, cat.name) },
                label = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat.icon,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
        }
    }
}