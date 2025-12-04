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
 * Defines all navigation routes and their destinations.
 *
 * @param navController Navigation controller for managing navigation
 */
@Composable
fun NavGraph(
	navController: NavHostController = rememberNavController()
) {
	NavHost(
		navController = navController,
		startDestination = NavRoutes.HOME
	) {
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
		
		composable(
			route = NavRoutes.CATEGORY,
			arguments = listOf(
				navArgument("categoryId") { type = NavType.StringType }
			)
		) { backStackEntry ->
			val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
			CategoryScreen(
				categoryId = categoryId,
				onCalculatorClick = { calculatorId ->
					navController.navigate(NavRoutes.calculatorRoute(calculatorId))
				},
				onNavigateUp = { navController.popBackStack() }
			)
		}
		
		composable(
			route = NavRoutes.CALCULATOR,
			arguments = listOf(
				navArgument("calculatorId") { type = NavType.StringType }
			)
		) { backStackEntry ->
			val calculatorId = backStackEntry.arguments?.getString("calculatorId") ?: ""
			CalculatorScreen(
				calculatorId = calculatorId,
				onNavigateUp = { navController.popBackStack() }
			)
		}
	}
}

