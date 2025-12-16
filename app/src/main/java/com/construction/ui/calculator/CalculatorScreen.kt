package com.construction.ui.calculator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.construction.R
import com.construction.domain.model.InputFieldDefinition
import com.construction.domain.premium.AccessControl
import com.construction.domain.repository.CalculatorRepository
import com.construction.ui.calculator.buildShareText
import com.construction.ui.premium.PremiumStub
import com.construction.ui.util.CalculatorIcons
import com.construction.util.ShareHelper

/**
 * Screen displaying calculator with input fields and calculation results.
 * Uses ViewModel to manage state and perform calculations.
 *
 * @param calculatorId ID of the calculator to display
 * @param onNavigateUp Callback to navigate back
 * @param viewModel ViewModel instance (created via factory)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
	calculatorId: String,
	onNavigateUp: () -> Unit
) {
	val viewModel: CalculatorViewModel = viewModel(
		factory = CalculatorViewModelFactory(calculatorId)
	)
	val calculator by viewModel.calculator.collectAsState()
	val inputValues by viewModel.inputValues.collectAsState()
	val results by viewModel.results.collectAsState()
	val error by viewModel.error.collectAsState()
	val context = LocalContext.current
	
	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(calculator?.name ?: "Калькулятор") },
				navigationIcon = {
					IconButton(onClick = onNavigateUp) {
						Icon(
							imageVector = Icons.Default.ArrowBack,
							contentDescription = "Назад"
						)
					}
				},
				actions = {
					// Share button - only show when there are results
					if (results.isNotEmpty() && calculator != null) {
						IconButton(
							onClick = {
								val shareText = buildShareText(
									calculator = calculator!!,
									inputValues = inputValues,
									results = results,
									context = context
								)
								ShareHelper.shareText(context, shareText)
							},
							colors = IconButtonDefaults.iconButtonColors(
								contentColor = Color(0xFF39FF14) // Ярко кислотно-зеленый
							)
						) {
							Icon(
								imageVector = Icons.Default.Share,
								contentDescription = stringResource(R.string.share_calculation)
							)
						}
					}
				}
			)
		}
	) { paddingValues ->
		val currentCalculator = calculator
		if (currentCalculator == null) {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.padding(paddingValues),
				contentAlignment = Alignment.Center
			) {
				CircularProgressIndicator()
			}
		} else {
			// Check access to calculator's category
			// TODO: Enable Premium gating after RuStore billing launch
			val category = remember(currentCalculator.categoryId) {
				CalculatorRepository.getCategories().firstOrNull { it.id == currentCalculator.categoryId }
			}
			val hasAccess = category?.let { AccessControl.isCategoryAccessible(it) } ?: true
			
			// Show PremiumStub if access is denied
			// Currently, hasAccess is always true since isPremiumUser = true
			if (!hasAccess) {
				PremiumStub(
					contentName = currentCalculator.name,
					onNavigateUp = onNavigateUp
				)
				return@Scaffold
			}
			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(paddingValues)
					.verticalScroll(rememberScrollState())
					.padding(16.dp),
				verticalArrangement = Arrangement.spacedBy(16.dp)
			) {
				// Calculator icon and name header
				Row(
					modifier = Modifier.fillMaxWidth(),
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(12.dp)
				) {
					Icon(
						imageVector = CalculatorIcons.getIcon(currentCalculator.id),
						contentDescription = currentCalculator.name,
						modifier = Modifier.size(48.dp),
						tint = Color(CalculatorIcons.getCategoryColor(currentCalculator.categoryId))
					)
					Column(modifier = Modifier.weight(1f)) {
						Text(
							text = currentCalculator.name,
							style = MaterialTheme.typography.headlineSmall,
							fontWeight = FontWeight.Bold
						)
						Text(
							text = currentCalculator.shortDescription,
							style = MaterialTheme.typography.bodyLarge,
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
					}
				}
				
				// Help text (if available)
				currentCalculator.helpText?.let { helpText ->
					Card(
						modifier = Modifier.fillMaxWidth(),
						colors = CardDefaults.cardColors(
							containerColor = MaterialTheme.colorScheme.primaryContainer
						)
					) {
						Text(
							text = helpText,
							style = MaterialTheme.typography.bodyMedium,
							modifier = Modifier.padding(12.dp)
						)
					}
				}
				
				Divider()
				
				// Input fields section
				Text(
					text = "Параметры",
					style = MaterialTheme.typography.titleLarge,
					fontWeight = FontWeight.Bold
				)
				
				currentCalculator.inputFields.forEach { field ->
					InputField(
						field = field,
						value = inputValues[field.id] ?: "",
						onValueChange = { viewModel.updateInput(field.id, it) }
					)
				}
				
				// Calculate button
				Button(
					onClick = { viewModel.calculate() },
					modifier = Modifier.fillMaxWidth(),
					enabled = currentCalculator.inputFields.isNotEmpty()
				) {
					Text("Рассчитать")
				}
				
				// Error message
				error?.let { errorMessage ->
					Card(
						modifier = Modifier.fillMaxWidth(),
						colors = CardDefaults.cardColors(
							containerColor = MaterialTheme.colorScheme.errorContainer
						)
					) {
						Text(
							text = errorMessage,
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.onErrorContainer,
							modifier = Modifier.padding(12.dp)
						)
					}
				}
				
				// Results section
				if (results.isNotEmpty()) {
					Divider()
					
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.SpaceBetween,
						verticalAlignment = Alignment.CenterVertically
					) {
						Text(
							text = "Результаты",
							style = MaterialTheme.typography.titleLarge,
							fontWeight = FontWeight.Bold
						)
					}
					
					currentCalculator.resultFields.forEach { field ->
						// Special handling for calculation_details field
						// TODO: Enable after Premium feature launch
						// Detailed calculation descriptions are hidden behind premium flag
						if (field.id == "calculation_details") {
							// Only show detailed descriptions when premium is enabled
							// Currently always enabled for testing (will be controlled by RuStore Billing in future)
							val premiumEnabled = true
							if (premiumEnabled) {
								val calculationDetails by viewModel.calculationDetails.collectAsState()
								calculationDetails?.let { details ->
									Card(
										modifier = Modifier.fillMaxWidth(),
										colors = CardDefaults.cardColors(
											containerColor = MaterialTheme.colorScheme.surfaceVariant
										)
									) {
										Column(
											modifier = Modifier
												.fillMaxWidth()
												.padding(16.dp),
											verticalArrangement = Arrangement.spacedBy(8.dp)
										) {
											Text(
												text = field.label,
												style = MaterialTheme.typography.titleMedium,
												fontWeight = FontWeight.Bold
											)
											Text(
												text = details,
												style = MaterialTheme.typography.bodySmall,
												color = MaterialTheme.colorScheme.onSurfaceVariant
											)
										}
									}
								}
							}
							// When premium is disabled, calculation_details field is hidden
							// All data is still calculated and stored, just not displayed
						} else {
							val resultValue = results[field.id]
							if (resultValue != null) {
								ResultCard(
									label = field.label,
									value = resultValue,
									unit = field.unit
								)
							}
						}
					}
					
					// Share button below results
					Button(
						onClick = {
							val shareText = buildShareText(
								calculator = currentCalculator,
								inputValues = inputValues,
								results = results,
								context = context
							)
							ShareHelper.shareText(context, shareText)
						},
						modifier = Modifier.fillMaxWidth(),
						colors = ButtonDefaults.buttonColors(
							containerColor = Color(0xFF39FF14), // Ярко кислотно-зеленый
							contentColor = Color.Black
						)
					) {
						Icon(
							imageVector = Icons.Default.Share,
							contentDescription = null,
							modifier = Modifier.size(18.dp)
						)
						Spacer(modifier = Modifier.width(8.dp))
						Text(stringResource(R.string.share_calculation))
					}
				}
				
				// Usage examples section
				if (currentCalculator.usageExamples.isNotEmpty()) {
					Divider()
					
					Text(
						text = stringResource(R.string.calculation_examples),
						style = MaterialTheme.typography.titleLarge,
						fontWeight = FontWeight.Bold
					)
					
					currentCalculator.usageExamples.forEach { example ->
						UsageExampleCard(example = example)
					}
				}
			}
		}
	}
}

/**
 * Card displaying a usage example.
 */
@Composable
private fun UsageExampleCard(example: com.construction.domain.model.UsageExample) {
	Card(
		modifier = Modifier.fillMaxWidth(),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceVariant
		)
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			Text(
				text = example.title,
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.Bold
			)
			
			Text(
				text = example.description,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
			
			Text(
				text = stringResource(R.string.input_data),
				style = MaterialTheme.typography.labelMedium,
				fontWeight = FontWeight.SemiBold,
				color = MaterialTheme.colorScheme.primary
			)
			
			Text(
				text = example.inputSummary,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
			
			Text(
				text = stringResource(R.string.result),
				style = MaterialTheme.typography.labelMedium,
				fontWeight = FontWeight.SemiBold,
				color = MaterialTheme.colorScheme.primary
			)
			
			Text(
				text = example.resultSummary,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}

/**
 * Input field composable with label, text field, and unit.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InputField(
	field: InputFieldDefinition,
	value: String,
	onValueChange: (String) -> Unit
) {
	Column(
		modifier = Modifier.fillMaxWidth(),
		verticalArrangement = Arrangement.spacedBy(4.dp)
	) {
		Text(
			text = field.label,
			style = MaterialTheme.typography.titleSmall,
			fontWeight = FontWeight.Medium
		)
		
		// Use dropdown if options are provided
		if (field.options != null && field.options.isNotEmpty()) {
			var expanded by remember { mutableStateOf(false) }
			val currentValue = value.toDoubleOrNull() ?: field.defaultValue ?: field.options.first().first
			val selectedOption = field.options.find { it.first == currentValue } ?: field.options.first()
			
			ExposedDropdownMenuBox(
				expanded = expanded,
				onExpandedChange = { expanded = !expanded },
				modifier = Modifier.fillMaxWidth()
			) {
				OutlinedTextField(
					value = selectedOption.second,
					onValueChange = { },
					readOnly = true,
					modifier = Modifier
						.fillMaxWidth()
						.menuAnchor(),
					trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
					placeholder = { Text(field.hint ?: "") }
				)
				ExposedDropdownMenu(
					expanded = expanded,
					onDismissRequest = { expanded = false }
				) {
					field.options.forEach { option ->
						DropdownMenuItem(
							text = { Text(option.second) },
							onClick = {
								onValueChange(option.first.toString())
								expanded = false
							}
						)
					}
				}
			}
		} else {
			// Regular text field
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				OutlinedTextField(
					value = value,
					onValueChange = onValueChange,
					modifier = Modifier.weight(1f),
					placeholder = { Text(field.hint ?: "") },
					singleLine = true,
					isError = value.isNotBlank() && value.toDoubleOrNull() == null
				)
				
				if (field.unit != null) {
					Text(
						text = field.unit,
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						modifier = Modifier.padding(end = 8.dp)
					)
				}
			}
		}
		
		if (field.hint != null) {
			Text(
				text = field.hint,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}

/**
 * Result card displaying a calculated value.
 */
@Composable
private fun ResultCard(
	label: String,
	value: Double,
	unit: String?
) {
	Card(
		modifier = Modifier.fillMaxWidth(),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.secondaryContainer
		)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				text = label,
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.SemiBold
			)
			
			Row(
				horizontalArrangement = Arrangement.spacedBy(4.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = String.format("%.2f", value),
					style = MaterialTheme.typography.titleLarge,
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.primary
				)
				
				if (unit != null) {
					Text(
						text = unit,
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			}
		}
	}
}

