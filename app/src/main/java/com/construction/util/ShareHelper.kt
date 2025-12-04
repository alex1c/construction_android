package com.construction.util

import android.content.Context
import android.content.Intent

/**
 * Helper utility for sharing text content via Android's share intent.
 * Provides a simple way to share text from Compose screens.
 */
object ShareHelper {
	
	/**
	 * Shares text content using Android's ACTION_SEND intent.
	 * Opens a chooser dialog allowing the user to select an app to share with.
	 *
	 * @param context Android context (use LocalContext.current in Compose)
	 * @param text Text content to share
	 */
	fun shareText(context: Context, text: String) {
		val sendIntent = Intent().apply {
			action = Intent.ACTION_SEND
			putExtra(Intent.EXTRA_TEXT, text)
			type = "text/plain"
		}
		
		val shareIntent = Intent.createChooser(sendIntent, null)
		context.startActivity(shareIntent)
	}
}

