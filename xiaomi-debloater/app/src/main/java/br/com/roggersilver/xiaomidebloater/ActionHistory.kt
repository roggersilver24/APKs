package br.com.roggersilver.xiaomidebloater

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class ActionRecord(val packageName: String, val enabled: Boolean, val timestamp: Long)

object ActionHistory {
    private const val PREFS = "verified_actions"
    private const val KEY = "history"

    fun read(context: Context): List<ActionRecord> = try {
        val array = JSONArray(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "[]"))
        (0 until array.length()).mapNotNull { index ->
            val item = array.optJSONObject(index) ?: return@mapNotNull null
            val pkg = item.optString("pkg")
            if (pkg.isBlank() || !AppPolicy.canChange(pkg)) null
            else ActionRecord(pkg, item.optBoolean("enabled"), item.optLong("at"))
        }.asReversed()
    } catch (_: Exception) { emptyList() }

    fun record(context: Context, pkg: String, enabled: Boolean) {
        if (!AppPolicy.canChange(pkg)) return
        val entries = read(context).asReversed().takeLast(49)
        val array = JSONArray()
        (entries + ActionRecord(pkg, enabled, System.currentTimeMillis())).forEach {
            array.put(JSONObject().put("pkg", it.packageName).put("enabled", it.enabled).put("at", it.timestamp))
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY, array.toString()).apply()
    }
}
