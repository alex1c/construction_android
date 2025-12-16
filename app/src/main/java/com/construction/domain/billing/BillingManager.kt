package com.construction.domain.billing

/**
 * Interface for managing in-app purchases and subscriptions via RuStore Billing.
 * 
 * This is a stub interface prepared for future RuStore Billing integration.
 * Implementation will be added when RuStore Billing SDK is integrated.
 * 
 * TODO: Enable Premium gating after RuStore billing launch
 * TODO: Implement RuStore Billing SDK integration
 * TODO: Add purchase flow handling
 * TODO: Add subscription status checking
 * TODO: Add purchase restoration functionality
 * TODO: Update PremiumState.isPremiumUser based on subscription status
 */
interface BillingManager {
	/**
	 * Checks if the user has an active premium subscription.
	 * 
	 * @return true if user has active premium subscription, false otherwise
	 */
	suspend fun hasActiveSubscription(): Boolean
	
	/**
	 * Launches the purchase flow for premium subscription.
	 * 
	 * @param onPurchaseComplete Callback when purchase is completed successfully
	 * @param onPurchaseError Callback when purchase fails or is cancelled
	 */
	suspend fun launchPurchaseFlow(
		onPurchaseComplete: () -> Unit,
		onPurchaseError: (String) -> Unit
	)
	
	/**
	 * Restores previous purchases.
	 * 
	 * @param onRestoreComplete Callback when restore is completed
	 * @param onRestoreError Callback when restore fails
	 */
	suspend fun restorePurchases(
		onRestoreComplete: () -> Unit,
		onRestoreError: (String) -> Unit
	)
	
	/**
	 * Initializes the billing manager.
	 * Should be called during app startup.
	 * 
	 * @param onInitialized Callback when initialization is complete
	 * @param onInitializationError Callback when initialization fails
	 */
	suspend fun initialize(
		onInitialized: () -> Unit,
		onInitializationError: (String) -> Unit
	)
}

