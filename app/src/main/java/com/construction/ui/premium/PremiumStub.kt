package com.construction.ui.premium

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Placeholder screen shown when user tries to access premium content
 * without a premium subscription.
 * 
 * This composable is prepared for future use but should NEVER be shown
 * in version 1.x since PremiumState.isPremiumUser is temporarily set to true.
 * 
 * TODO: Enable Premium gating after RuStore billing launch
 * TODO: Add purchase button that calls BillingManager.launchPurchaseFlow()
 * TODO: Add subscription status check and restore purchases functionality
 * 
 * @param contentName Name of the premium content being accessed (for display)
 * @param onNavigateUp Optional callback to navigate back
 */
@Composable
fun PremiumStub(
	contentName: String = "Премиум контент",
	onNavigateUp: (() -> Unit)? = null
) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(24.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		// Premium icon placeholder
		Text(
			text = "⭐",
			style = MaterialTheme.typography.displayLarge,
			modifier = Modifier.padding(bottom = 16.dp)
		)
		
		Text(
			text = "Премиум функция",
			style = MaterialTheme.typography.headlineMedium,
			fontWeight = FontWeight.Bold,
			textAlign = TextAlign.Center,
			modifier = Modifier.padding(bottom = 8.dp)
		)
		
		Text(
			text = "$contentName доступен только для премиум пользователей",
			style = MaterialTheme.typography.bodyLarge,
			textAlign = TextAlign.Center,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			modifier = Modifier.padding(bottom = 24.dp)
		)
		
		// Purchase button placeholder
		// TODO: Implement purchase button after RuStore billing integration
		// Button(
		//     onClick = { /* Launch purchase flow */ },
		//     modifier = Modifier.fillMaxWidth()
		// ) {
		//     Text("Получить премиум")
		// }
		
		if (onNavigateUp != null) {
			Spacer(modifier = Modifier.height(16.dp))
			TextButton(onClick = onNavigateUp) {
				Text("Назад")
			}
		}
	}
}

