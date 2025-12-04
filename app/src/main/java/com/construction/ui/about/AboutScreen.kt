package com.construction.ui.about

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.content.Intent
import android.net.Uri
import com.construction.R
import com.construction.util.ShareHelper

/**
 * About screen displaying app information and share app functionality.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
	onNavigateUp: () -> Unit
) {
	val context = LocalContext.current
	
	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(stringResource(R.string.about)) },
				navigationIcon = {
					IconButton(onClick = onNavigateUp) {
						Icon(
							imageVector = Icons.Default.ArrowBack,
							contentDescription = "Назад"
						)
					}
				}
			)
		}
	) { paddingValues ->
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(paddingValues)
				.verticalScroll(rememberScrollState())
				.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(24.dp)
		) {
			// App name
			Text(
				text = stringResource(R.string.app_name),
				style = MaterialTheme.typography.headlineMedium,
				fontWeight = FontWeight.Bold
			)
			
			// Description
			Text(
				text = stringResource(R.string.about_description),
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
			
			Divider()
			
			// Share app button
			Button(
				onClick = {
					val rustoreUrl = context.getString(R.string.rustore_url)
					val shareText = context.getString(R.string.share_app_message, rustoreUrl)
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
				Text(stringResource(R.string.share_app))
			}
			
			// Version info
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
						text = "Версия приложения",
						style = MaterialTheme.typography.titleSmall,
						fontWeight = FontWeight.Bold
					)
					Text(
						text = "1.0 (Build 1)",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
					Text(
						text = "Минимальная версия Android: 7.0 (API 24)",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			}
			
			// Contact information
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
						text = "Контактная информация",
						style = MaterialTheme.typography.titleSmall,
						fontWeight = FontWeight.Bold
					)
					Text(
						text = "Email для поддержки:",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
					Text(
						text = stringResource(R.string.contact_email),
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.primary
					)
					Spacer(modifier = Modifier.height(8.dp))
					Text(
						text = "Разработчик: ${stringResource(R.string.app_developer)}",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			}
			
			// Privacy policy
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
						text = "Политика конфиденциальности",
						style = MaterialTheme.typography.titleSmall,
						fontWeight = FontWeight.Bold
					)
					Text(
						text = "Приложение собирает минимальные данные, которые хранятся только локально на вашем устройстве. История расчётов не передаётся на внешние серверы.",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
					Button(
						onClick = {
							val intent = Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.privacy_policy_url)))
							context.startActivity(intent)
						},
						modifier = Modifier.fillMaxWidth(),
						colors = ButtonDefaults.buttonColors(
							containerColor = MaterialTheme.colorScheme.primaryContainer,
							contentColor = MaterialTheme.colorScheme.onPrimaryContainer
						)
					) {
						Text("Открыть политику конфиденциальности")
					}
				}
			}
			
			// Support contact
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
						text = "Поддержка",
						style = MaterialTheme.typography.titleSmall,
						fontWeight = FontWeight.Bold
					)
					Text(
						text = "По вопросам работы приложения обращайтесь:",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
					Button(
						onClick = {
							val email = context.getString(R.string.support_email)
							val intent = Intent(Intent.ACTION_SENDTO).apply {
								data = Uri.parse("mailto:$email")
								putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
								putExtra(Intent.EXTRA_SUBJECT, "Вопрос по приложению Строительные калькуляторы")
							}
							context.startActivity(Intent.createChooser(intent, "Отправить email"))
						},
						modifier = Modifier.fillMaxWidth(),
						colors = ButtonDefaults.buttonColors(
							containerColor = MaterialTheme.colorScheme.primaryContainer,
							contentColor = MaterialTheme.colorScheme.onPrimaryContainer
						)
					) {
						Text("Написать в поддержку")
					}
					Text(
						text = stringResource(R.string.support_email),
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.primary
					)
				}
			}
		}
	}
}

