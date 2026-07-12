package com.example.personalassistant

import android.content.Context

object AppLauncher {

    data class InstalledApp(val label: String, val packageName: String)

    fun getInstalledApps(context: Context): List<InstalledApp> {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(android.content.pm.PackageManager.GET_META_DATA)
        return apps
            .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
            .map { InstalledApp(pm.getApplicationLabel(it).toString(), it.packageName) }
            .sortedBy { it.label }
    }

    fun openAppByName(context: Context, spokenName: String): Boolean {
        val apps = getInstalledApps(context)
        val match = apps.firstOrNull {
            it.label.contains(spokenName, ignoreCase = true) ||
            spokenName.contains(it.label, ignoreCase = true)
        } ?: return false

        val intent = context.packageManager.getLaunchIntentForPackage(match.packageName) ?: return false
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        return true
    }

    fun openAppByPackage(context: Context, packageName: String): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        return true
    }
}
