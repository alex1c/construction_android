package com.construction.navigation

/**
 * Navigation routes for the app.
 * Defines all possible navigation destinations.
 */
object NavRoutes {
	const val HOME = "home"
	const val CATEGORY = "category/{categoryId}"
	const val CALCULATOR = "calculator/{calculatorId}"
	
	/**
	 * Builds category route with category ID parameter.
	 */
	fun categoryRoute(categoryId: String) = "category/$categoryId"
	
	/**
	 * Builds calculator route with calculator ID parameter.
	 */
	fun calculatorRoute(calculatorId: String) = "calculator/$calculatorId"
}


