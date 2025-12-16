package ru.calc1.construction.util

import android.content.Context
import android.content.Intent

/**
 * Helper utility for sharing text content via Android's share intent.
 * 
 * Provides a simple, reusable way to share text from Compose screens.
 * Uses Android's standard ACTION_SEND intent to open the system share dialog,
 * allowing users to share calculation results via any installed app (messengers,
 * email, social networks, etc.).
 * 
 * Usage:
 * ```kotlin
 * val context = LocalContext.current
 * ShareHelper.shareText(context, "Calculation results...")
 * ```
 * 
 * Features:
 * - Opens system share chooser dialog
 * - Works with all apps that support text sharing
 * - No app-specific integrations needed
 * - Handles all sharing scenarios (messengers, email, clipboard, etc.)
 * 
 * Architecture:
 * - Uses object singleton pattern (no instance needed)
 * - Stateless utility (pure function)
 * - Wraps Android Intent system for Compose compatibility
 */
object ShareHelper {
	
	/**
	 * Shares text content using Android's ACTION_SEND intent.
	 * 
	 * Creates an intent with ACTION_SEND action and text/plain MIME type,
	 * then opens the system chooser dialog. The user can select any app
	 * that supports text sharing (Telegram, WhatsApp, Email, etc.).
	 * 
	 * Process:
	 * 1. Create Intent with ACTION_SEND action
	 * 2. Add text as EXTRA_TEXT extra
	 * 3. Set MIME type to "text/plain"
	 * 4. Create chooser dialog using Intent.createChooser()
	 * 5. Start activity with chooser
	 * 
	 * @param context Android context (use LocalContext.current in Compose)
	 * @param text Text content to share (calculation results, app link, etc.)
	 */
	fun shareText(context: Context, text: String) {
		// Create send intent with text content
		val sendIntent = Intent().apply {
			action = Intent.ACTION_SEND
			putExtra(Intent.EXTRA_TEXT, text)
			type = "text/plain"
		}
		
		// Create chooser dialog to let user select sharing app
		val shareIntent = Intent.createChooser(sendIntent, null)
		context.startActivity(shareIntent)
	}
}

