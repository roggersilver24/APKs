package br.com.roggersilver.xiaomidebloater

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build

data class InstalledApp(
    val packageName: String,
    val label: String,
    val group: RiskGroup,
    val systemApp: Boolean,
    val enabled: Boolean
)

object PackageScanner {
    fun scan(context: Context): List<InstalledApp> {
        val pm = context.packageManager
        val apps = if (Build.VERSION.SDK_INT >= 33) {
            pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledApplications(0)
        }
        return apps.map { info ->
            val system = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                (info.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            InstalledApp(
                packageName = info.packageName,
                label = info.loadLabel(pm).toString().ifBlank { info.packageName },
                group = AppPolicy.classify(info.packageName, system),
                systemApp = system,
                enabled = info.enabled
            )
        }.sortedWith(compareBy<InstalledApp>({ it.group.ordinal }, { it.label.lowercase() }, { it.packageName }))
    }
}
