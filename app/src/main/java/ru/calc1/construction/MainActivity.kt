package ru.calc1.construction

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import ru.calc1.construction.navigation.NavGraph
import ru.calc1.construction.ui.theme.ConstructionTheme

/**
 * Main activity for the construction calculators app.
 * 
 * This is the entry point of the application. It sets up the Compose UI and initializes
 * the navigation graph. The app uses a single-activity architecture with Jetpack Compose
 * for all UI rendering.
 * 
 * Architecture:
 * - Single Activity pattern (all screens are composables)
 * - Jetpack Compose for UI
 * - Navigation Component for screen navigation
 * - Material Design 3 for theming
 * 
 * Flow:
 * 1. Activity is created
 * 2. ConstructionTheme is applied (Material Design 3)
 * 3. NavGraph is initialized with HomeScreen as start destination
 * 4. User navigates through screens using Navigation Component
 */
class MainActivity : ComponentActivity() {
	/**
	 * Called when the activity is first created.
	 * Sets up the Compose UI with theme and navigation.
	 * 
	 * @param savedInstanceState Previously saved state (not used in this app)
	 */
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		// Set Compose content as the root view
		setContent {
			// Apply custom theme (Material Design 3)
			ConstructionTheme {
				// Surface provides background color from theme
				Surface(
					modifier = Modifier.fillMaxSize(),
					color = MaterialTheme.colorScheme.background
				) {
					// Initialize navigation graph
					// NavGraph handles all screen navigation and routing
					NavGraph()
				}
			}
		}
	}
}


