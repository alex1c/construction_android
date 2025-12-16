package ru.calc1.construction.domain.premium

/**
 * Global premium state management.
 * 
 * This object tracks whether the current user has premium access.
 * Currently set to true to allow all content access during v1.x.
 * 
 * TODO: Enable Premium gating after RuStore billing launch
 * TODO: Integrate with RuStore Billing to check actual subscription status
 * TODO: Update isPremiumUser based on BillingManager subscription state
 */
object PremiumState {
	/**
	 * Indicates whether the current user has premium access.
	 * 
	 * Currently set to true to ensure all content is accessible.
	 * This will be updated to check actual subscription status
	 * when RuStore Billing integration is enabled.
	 */
	var isPremiumUser: Boolean = true // TEMPORARILY true - all content accessible
}

