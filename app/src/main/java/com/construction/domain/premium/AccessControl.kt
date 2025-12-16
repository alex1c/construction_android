package com.construction.domain.premium

import com.construction.domain.model.AccessLevel
import com.construction.domain.model.CalculatorCategory

/**
 * Access control utilities for premium monetization.
 * 
 * Provides functions to check if content is accessible based on
 * access level and premium subscription status.
 * 
 * TODO: Enable Premium gating after RuStore billing launch
 */
object AccessControl {
	/**
	 * Checks if a category is accessible to the current user.
	 * 
	 * Access is granted if:
	 * - Category has FREE access level, OR
	 * - User has premium subscription
	 * 
	 * @param category Category to check access for
	 * @return true if category is accessible, false otherwise
	 */
	fun isCategoryAccessible(category: CalculatorCategory): Boolean {
		return category.accessLevel == AccessLevel.FREE || PremiumState.isPremiumUser
	}
	
	/**
	 * Checks if a category is accessible based on access level.
	 * 
	 * @param accessLevel Access level to check
	 * @return true if accessible, false otherwise
	 */
	fun isAccessible(accessLevel: AccessLevel): Boolean {
		return accessLevel == AccessLevel.FREE || PremiumState.isPremiumUser
	}
}

