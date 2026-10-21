package br.com.roggersilver.xiaomidebloater

enum class RiskGroup(val title: String) {
    PROTECTED("Protegido"),
    REVIEW("Revisar"),
    UNKNOWN("Desconhecido"),
    USER("Usuario")
}

/** Conservative classifier. No group gives permission to remove a package. */
object AppPolicy {
    private val protectedExact = setOf(
        "android",
        "com.android.systemui",
        "com.android.settings",
        "com.android.phone",
        "com.android.providers.settings",
        "com.android.packageinstaller",
        "com.google.android.packageinstaller",
        "com.google.android.permissioncontroller",
        "com.google.android.gms",
        "com.google.android.gsf",
        "com.miui.home",
        "com.miui.securitycenter",
        "com.xiaomi.xmsf",
        "com.xiaomi.finddevice"
    )
    private val protectedPrefixes = listOf("android.", "com.android.", "com.google.android.", "com.android.providers.")
    private val reviewCandidates = setOf(
        "com.miui.analytics",
        "com.miui.msa.global",
        "com.xiaomi.mipicks"
    )
    fun classify(pkg: String, systemApp: Boolean): RiskGroup = when {
        pkg.isBlank() -> RiskGroup.PROTECTED
        pkg in protectedExact || protectedPrefixes.any(pkg::startsWith) -> RiskGroup.PROTECTED
        !systemApp -> RiskGroup.USER
        pkg in reviewCandidates -> RiskGroup.REVIEW
        else -> RiskGroup.UNKNOWN
    }
}
