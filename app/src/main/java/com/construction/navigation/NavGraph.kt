package com.construction.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.construction.ui.calculator.CalculatorScreen
import com.construction.ui.category.CategoryScreen
import com.construction.ui.home.HomeScreen

/**
 * Navigation graph for the app.
 * 
 * Defines all navigation routes and their destinations using Jetpack Navigation Component.
 * The app uses a type-safe navigation approach with string-based routes.
 * 
 * Navigation Structure:
 * - HomeScreen: Entry point, shows categories and popular calculators
 * - CategoryScreen: Shows all calculators in a specific category
 * - CalculatorScreen: Displays individual calculator with input fields and results
 * 
 * Navigation Flow:
 * HomeScreen -> CategoryScreen -> CalculatorScreen
 * HomeScreen -> CalculatorScreen (direct access to popular calculators)
 * 
 * @param navController Navigation controller for managing navigation stack.
 *                      If not provided, creates a new one using rememberNavController().
 */
@Composable
fun NavGraph(
	navController: NavHostController = rememberNavController()
) {
	NavHost(
		navController = navController,
		startDestination = NavRoutes.HOME
	) {
		/**
		 * Home Screen - Main entry point
		 * Displays:
		 * - Search functionality
		 * - Popular calculators section
		 * - Categories section
		 * 
		 * Navigation actions:
		 * - onCategoryClick: Navigate to CategoryScreen with categoryId
		 * - onCalculatorClick: Navigate directly to CalculatorScreen with calculatorId
		 */
		composable(NavRoutes.HOME) {
			HomeScreen(
				onCategoryClick = { categoryId ->
					navController.navigate(NavRoutes.categoryRoute(categoryId))
				},
				onCalculatorClick = { calculatorId ->
					navController.navigate(NavRoutes.calculatorRoute(calculatorId))
				}
			)
		}
		
		/**
		 * Category Screen - Shows calculators in a specific category
		 * 
		 * Route parameters:
		 * - categoryId: String - ID of the category to display
		 * 
		 * Navigation actions:
		 * - onCalculatorClick: Navigate to CalculatorScreen
		 * - onNavigateUp: Pop back to previous screen (HomeScreen)
		 */
		composable(
			route = NavRoutes.CATEGORY,
			arguments = listOf(
				navArgument("categoryId") { type = NavType.StringType }
			)
		) { backStackEntry ->
			// Extract categoryId from navigation arguments
			val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
			CategoryScreen(
				categoryId = categoryId,
				onCalculatorClick = { calculatorId ->
					navController.navigate(NavRoutes.calculatorRoute(calculatorId))
				},
				onNavigateUp = { navController.popBackStack() }
			)
		}
		
		/**
		 * Calculator Screen - Individual calculator with input fields and results
		 * 
		 * Route parameters:
		 * - calculatorId: String - ID of the calculator to display
		 * 
		 * Features:
		 * - Input fields with validation
		 * - Real-time calculation
		 * - Results display
		 * - Share functionality
		 * - Detailed calculation descriptions (premium feature)
		 * 
		 * Navigation actions:
		 * - onNavigateUp: Pop back to previous screen
		 */
		composable(
			route = NavRoutes.CALCULATOR,
			arguments = listOf(
				navArgument("calculatorId") { type = NavType.StringType }
			)
		) { backStackEntry ->
			// Extract calculatorId from navigation arguments
			val calculatorId = backStackEntry.arguments?.getString("calculatorId") ?: ""
			CalculatorScreen(
				calculatorId = calculatorId,
				onNavigateUp = { navController.popBackStack() }
			)
		}
	}
}

