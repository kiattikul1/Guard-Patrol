package com.example.guard_patrol.Data.Preference

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WorkspacePref (context: Context) {
    private val prefName = "WorkspaceSelect"
    private var preferences: SharedPreferences
    private var values = ArrayList<String>()

    init {
        preferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
    }

    fun getPreferences(): ArrayList<String> {
        val gson = Gson()
        val json = preferences.getString(prefName, "")
        val type = object : TypeToken<ArrayList<String>>() {}.type

        return if (json.isNullOrEmpty()) {
            ArrayList()
        } else {
            gson.fromJson(json, type)
        }
    }

    fun setPreferences(id: String, workspace: String) {
        val editor = preferences.edit()
        val gson = Gson()

        // Load existing data
        val json = preferences.getString(prefName, "")
        val type = object : TypeToken<ArrayList<String>>() {}.type

        values = if (json.isNullOrEmpty()) {
            ArrayList()
        } else {
            gson.fromJson(json, type)
        }

        // Update or add new values
        if (values.size >= 2) {
            // Update existing values
            values[0] = id
            values[1] = workspace
        } else {
            // Add new values
            values.add(id)
            values.add(workspace)
        }

        // Save the updated values
        val updatedJson = gson.toJson(values)
        editor.putString(prefName, updatedJson).apply()
    }

    fun clearData() {
        preferences.edit().clear().apply()
    }

    //    fun getPreferences(): String{
//        return preferences.getString(prefName, "") ?: ""
//    }
//
//    fun setPreferences(value:String){
//        preferences.edit().putString(prefName, value).apply()
//    }

}