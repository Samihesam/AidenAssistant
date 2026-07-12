package com.example.personalassistant

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object PersonalizationManager {

    private const val PREFS_NAME = "aiden_personalization"
    private const val KEY_ASSISTANT_NAME = "assistant_name"
    private const val KEY_MEMORIES = "memories"

    data class Memory(val key: String, val value: String, val timestamp: Long)

    fun getAssistantName(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_ASSISTANT_NAME, "ایدن") ?: "ایدن"
    }

    fun setAssistantName(context: Context, newName: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ASSISTANT_NAME, newName).apply()
    }

    fun getAllMemories(context: Context): List<Memory> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_MEMORIES, "[]") ?: "[]"
        val array = JSONArray(raw)
        return (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            Memory(obj.getString("key"), obj.getString("value"), obj.getLong("timestamp"))
        }
    }

    fun saveMemory(context: Context, key: String, value: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = getAllMemories(context).toMutableList()

        current.removeAll { it.key.equals(key, ignoreCase = true) }
        current.add(Memory(key, value, System.currentTimeMillis()))

        val array = JSONArray()
        current.forEach { mem ->
            array.put(JSONObject().apply {
                put("key", mem.key)
                put("value", mem.value)
                put("timestamp", mem.timestamp)
            })
        }
        prefs.edit().putString(KEY_MEMORIES, array.toString()).apply()
    }

    fun findMemory(context: Context, query: String): Memory? {
        return getAllMemories(context).firstOrNull {
            it.key.contains(query, ignoreCase = true) || query.contains(it.key, ignoreCase = true)
        }
    }

    fun deleteMemory(context: Context, key: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = getAllMemories(context).filterNot { it.key.equals(key, ignoreCase = true) }
        val array = JSONArray()
        current.forEach { mem ->
            array.put(JSONObject().apply {
                put("key", mem.key); put("value", mem.value); put("timestamp", mem.timestamp)
            })
        }
        prefs.edit().putString(KEY_MEMORIES, array.toString()).apply()
    }

    fun clearAllMemories(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_MEMORIES).apply()
    }
}

object PersonalizationCommandParser {

    sealed class PersonalizationCommand {
        data class RenameAssistant(val newName: String) : PersonalizationCommand()
        data class RememberFact(val key: String, val value: String) : PersonalizationCommand()
        data class RecallFact(val query: String) : PersonalizationCommand()
        data class ForgetFact(val key: String) : PersonalizationCommand()
        object None : PersonalizationCommand()
    }

    fun parse(text: String): PersonalizationCommand {
        val normalized = text.trim()

        val renameRegex = Regex("اسمت\\s*(رو|را)?\\s*(بذار|عوض کن به)\\s*(.+)")
        renameRegex.find(normalized)?.let {
            val newName = it.groupValues[3].trim()
            if (newName.isNotEmpty()) return PersonalizationCommand.RenameAssistant(newName)
        }

        if (normalized.contains("یادت باشه") || normalized.contains("به خاطر بسپار")) {
            val fact = normalized
                .replace("یادت باشه", "")
                .replace("به خاطر بسپار", "")
                .trim()
            if (fact.isNotEmpty()) {
                val words = fact.split(" ")
                val key = words.take(2).joinToString(" ")
                return PersonalizationCommand.RememberFact(key = key, value = fact)
            }
        }

        if (normalized.contains("فراموش کن") || normalized.contains("یادت نباشه")) {
            val key = normalized.replace("فراموش کن", "").replace("یادت نباشه", "").trim()
            if (key.isNotEmpty()) return PersonalizationCommand.ForgetFact(key)
        }

        if (normalized.contains("؟") || normalized.startsWith("چی") || normalized.contains("کیه") || normalized.contains("چیه")) {
            return PersonalizationCommand.RecallFact(normalized)
        }

        return PersonalizationCommand.None
    }
}
