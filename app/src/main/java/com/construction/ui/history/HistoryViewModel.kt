package com.construction.ui.history

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
 * Uses SharedPreferences to store history entries without database.
 */
class HistoryViewModel(context: Context) : ViewModel() {
	
	private val prefs: SharedPreferences = context.getSharedPreferences("history_prefs", Context.MODE_PRIVATE)
	private val HISTORY_KEY = "history_entries"
	
	private val _historyEntries = MutableStateFlow<List<HistoryEntry>>(emptyList())
	val historyEntries: StateFlow<List<HistoryEntry>> = _historyEntries.asStateFlow()
	
	init {
		loadHistory()
	}
	
	/**
	 * Loads history from SharedPreferences.
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
	 */
	fun addHistoryEntry(entry: HistoryEntry) {
		viewModelScope.launch {
			val currentList = _historyEntries.value.toMutableList()
			currentList.add(0, entry) // Add to beginning
			_historyEntries.value = currentList
			saveHistory(currentList)
		}
	}
	
	/**
	 * Deletes a history entry.
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
	 * Clears all history.
	 */
	fun clearHistory() {
		viewModelScope.launch {
			_historyEntries.value = emptyList()
			prefs.edit().remove(HISTORY_KEY).apply()
		}
	}
	
	/**
	 * Saves history to SharedPreferences as JSON.
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

