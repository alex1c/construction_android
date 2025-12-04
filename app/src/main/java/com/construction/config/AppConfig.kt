package com.construction.config

/**
 * Global application configuration flags.
 * 
 * This object provides centralized configuration for feature flags and premium features.
 * Used to control feature visibility without code changes, enabling easy A/B testing
 * and gradual feature rollouts.
 * 
 * Architecture:
 * - Uses object singleton pattern (no instance needed)
 * - Flags can be changed at runtime
 * - Future: Flags can be controlled remotely or via billing system
 * 
 * Current Flags:
 * - premiumEnabled: Controls visibility of detailed calculation descriptions
 * 
 * Future Flags (planned):
 * - showAds: Control advertisement visibility
 * - enableAnalytics: Control analytics collection
 * - enableCrashReporting: Control crash reporting
 * 
 * Integration with Billing:
 * In future updates, these flags will be controlled by RuStore Billing:
 * - premiumEnabled = billingManager.isPremiumUser()
 * - showAds = !billingManager.isPremiumUser()
 */
object AppConfig {
	/**
	 * Premium feature flag.
	 * 
	 * Controls visibility of premium features, specifically detailed calculation
	 * descriptions. When false, only basic results are shown. When true, users
	 * see step-by-step calculation explanations with formulas.
	 * 
	 * Current State (Version 1.0):
	 * - Always false (premium features hidden)
	 * - All detailed descriptions are calculated but not displayed
	 * - Ready for activation when premium billing is integrated
	 * 
	 * Future Integration:
	 * This flag will be controlled by RuStore Billing:
	 * ```kotlin
	 * AppConfig.premiumEnabled = billingManager.isPremiumUser()
	 * ```
	 * 
	 * Premium Features (when enabled):
	 * - Detailed calculation descriptions with step-by-step formulas
	 * - Extended history (unlimited entries)
	 * - Export to PDF
	 * - Advanced calculation options
	 * 
	 * TODO: Enable after Premium feature launch
	 * This flag will be controlled by RuStore Billing in future updates.
	 * 
	 * NOTE: Currently set to true for testing purposes before RuStore Billing integration.
	 * Will be controlled by billingManager.isPremiumUser() after integration.
	 */
	var premiumEnabled: Boolean = true
}

