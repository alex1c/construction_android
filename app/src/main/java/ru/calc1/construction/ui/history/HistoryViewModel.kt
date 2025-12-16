package ru.calc1.construction.ui.history

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

/**
 * ViewModel for history screen.
 * 
 * Manages calculation history using SharedPreferences for lightweight persistence.
 * History entries are stored as JSON strings, avoiding the need for a database.
 * 
 * Architecture:
 * - Uses SharedPreferences for simple key-value storage
 * - JSON serialization/deserialization for complex data structures
 * - StateFlow for reactive UI updates
 * - Coroutines for async operations
 * 
 * Storage Format:
 * - Key: "history_entries"
 * - Value: JSON array of HistoryEntry objects
 * - Each entry contains: id, calculatorId, calculatorName, timestamp, inputValues, results
 * 
 * Limitations:
 * - SharedPreferences is suitable for small datasets (< 1MB)
 * - For large datasets, consider migrating to Room database
 * - JSON parsing is synchronous (consider background thread for large histories)
 * 
 * Future Enhancements:
 * - Migrate to Room database for better performance
 * - Add pagination for large histories
 * - Add search and filtering
 * - Add export functionality
 * 
 * @param context Android context for accessing SharedPreferences
 */
class HistoryViewModel(context: Context) : ViewModel() {
	
	/**
	 * SharedPreferences instance for storing history entries.
	 * Uses private mode to prevent other apps from accessing the data.
	 */
	private val prefs: SharedPreferences = context.getSharedPreferences("history_prefs", Context.MODE_PRIVATE)
	
	/**
	 * Key for storing history entries in SharedPreferences.
	 */
	private val HISTORY_KEY = "history_entries"
	
	/**
	 * StateFlow containing the list of history entries.
	 * Automatically triggers UI recomposition when updated.
	 */
	private val _historyEntries = MutableStateFlow<List<HistoryEntry>>(emptyList())
	val historyEntries: StateFlow<List<HistoryEntry>> = _historyEntries.asStateFlow()
	
	/**
	 * Initializes ViewModel by loading history from SharedPreferences.
	 */
	init {
		loadHistory()
	}
	
	/**
	 * Loads history from SharedPreferences.
	 * 
	 * Process:
	 * 1. Read JSON string from SharedPreferences
	 * 2. Parse JSON array into List<HistoryEntry>
	 * 3. Update StateFlow with loaded entries
	 * 4. Handle parsing errors gracefully (return empty list)
	 * 
	 * Error Handling:
	 * - If JSON is invalid or missing, returns empty list
	 * - Errors are silently handled to prevent crashes
	 */
	private fun loadHistory() {
		viewModelScope.launch {
			val historyJson = prefs.getString(HISTORY_KEY, null)
			if (historyJson != null) {
				try {
					val entries = parseHistoryFromJson(historyJson)
					_historyEntries.value = entries
				} catch (e: Exception) {
					_historyEntries.value = emptyList()
				}
			}
		}
	}
	
	/**
	 * Saves a new history entry.
	 * 
	 * Adds the entry to the beginning of the list (most recent first)
	 * and persists to SharedPreferences.
	 * 
	 * @param entry History entry to add (contains calculator info, inputs, and results)
	 */
	fun addHistoryEntry(entry: HistoryEntry) {
		viewModelScope.launch {
			val currentList = _historyEntries.value.toMutableList()
			currentList.add(0, entry) // Add to beginning (most recent first)
			_historyEntries.value = currentList
			saveHistory(currentList)
		}
	}
	
	/**
	 * Deletes a history entry by ID.
	 * 
	 * Removes the entry from the list and updates SharedPreferences.
	 * 
	 * @param entryId Unique ID of the entry to delete
	 */
	fun deleteHistoryEntry(entryId: String) {
		viewModelScope.launch {
			val currentList = _historyEntries.value.toMutableList()
			currentList.removeAll { it.id == entryId }
			_historyEntries.value = currentList
			saveHistory(currentList)
		}
	}
	
	/**
	 * Clears all history entries.
	 * 
	 * Removes all entries from memory and SharedPreferences.
	 */
	fun clearHistory() {
		viewModelScope.launch {
			_historyEntries.value = emptyList()
			prefs.edit().remove(HISTORY_KEY).apply()
		}
	}
	
	/**
	 * Saves history to SharedPreferences as JSON.
	 * 
	 * Converts List<HistoryEntry> to JSON array and stores as string.
	 * Each entry is serialized with all its properties (id, calculatorId,
	 * calculatorName, timestamp, inputValues, results).
	 * 
	 * JSON Structure:
	 * ```json
	 * [
	 *   {
	 *     "id": "entry_id",
	 *     "calculatorId": "wallpaper",
	 *     "calculatorName": "Калькулятор обоев",
	 *     "timestamp": 1234567890,
	 *     "inputValues": {"room_length": "4", "room_width": "3", ...},
	 *     "results": {"rolls_count": 8.0}
	 *   },
	 *   ...
	 * ]
	 * ```
	 * 
	 * Error Handling:
	 * - Silently ignores save errors to prevent crashes
	 * - Data loss is acceptable for history feature
	 * 
	 * @param entries List of history entries to save
	 */
	private fun saveHistory(entries: List<HistoryEntry>) {
		try {
			val jsonArray = JSONArray()
			entries.forEach { entry ->
				val jsonObject = JSONObject().apply {
					put("id", entry.id)
					put("calculatorId", entry.calculatorId)
					put("calculatorName", entry.calculatorName)
					put("timestamp", entry.timestamp)
					
					// Save input values
					val inputsJson = JSONObject()
					entry.inputValues.forEach { (key, value) ->
						inputsJson.put(key, value)
					}
					put("inputValues", inputsJson)
					
					// Save results
					val resultsJson = JSONObject()
					entry.results.forEach { (key, value) ->
						resultsJson.put(key, value)
					}
					put("results", resultsJson)
				}
				jsonArray.put(jsonObject)
			}
			
			prefs.edit().putString(HISTORY_KEY, jsonArray.toString()).apply()
		} catch (e: Exception) {
			// Ignore save errors
		}
	}
	
	/**
	 * Parses history from JSON string.
	 * 
	 * Converts JSON array string back to List<HistoryEntry>.
	 * Handles nested JSON objects for inputValues and results maps.
	 * 
	 * Process:
	 * 1. Parse JSON string into JSONArray
	 * 2. Iterate through array elements
	 * 3. Extract each property (id, calculatorId, etc.)
	 * 4. Parse nested JSON objects for inputValues and results
	 * 5. Create HistoryEntry objects
	 * 
	 * @param jsonString JSON array string from SharedPreferences
	 * @return List of parsed HistoryEntry objects
	 */
	private fun parseHistoryFromJson(jsonString: String): List<HistoryEntry> {
		val entries = mutableListOf<HistoryEntry>()
		val jsonArray = JSONArray(jsonString)
		
		for (i in 0 until jsonArray.length()) {
			val jsonObject = jsonArray.getJSONObject(i)
			
			// Parse input values
			val inputsJson = jsonObject.getJSONObject("inputValues")
			val inputValues = mutableMapOf<String, String>()
			inputsJson.keys().forEach { key ->
				inputValues[key] = inputsJson.getString(key)
			}
			
			// Parse results
			val resultsJson = jsonObject.getJSONObject("results")
			val results = mutableMapOf<String, Double>()
			resultsJson.keys().forEach { key ->
				results[key] = resultsJson.getDouble(key)
			}
			
			entries.add(
				HistoryEntry(
					id = jsonObject.getString("id"),
					calculatorId = jsonObject.getString("calculatorId"),
					calculatorName = jsonObject.getString("calculatorName"),
					inputValues = inputValues,
					results = results,
					timestamp = jsonObject.getLong("timestamp")
				)
			)
		}
		
		return entries
	}
}

