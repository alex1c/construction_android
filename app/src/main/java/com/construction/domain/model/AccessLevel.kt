package com.construction.domain.model

/**
 * Access level for categories and calculators.
 * 
 * Used to control premium monetization:
 * - FREE: Always accessible to all users
 * - PREMIUM: Requires premium subscription
 * 
 * TODO: Enable Premium gating after RuStore billing launch
 */
enum class AccessLevel {
	/**
	 * Free content accessible to all users.
	 */
	FREE,
	
	/**
	 * Premium content requiring subscription.
	 */
	PREMIUM
}

