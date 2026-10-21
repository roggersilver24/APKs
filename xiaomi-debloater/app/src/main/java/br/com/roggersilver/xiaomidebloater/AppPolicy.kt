package br.com.roggersilver.xiaomidebloater

import java.util.Locale

enum class RiskGroup(val title: String) {
    PROTECTED("Protegido"),
    REVIEW("Revisar"),
    UNKNOWN("Desconhecido"),
    USER("Usuário")
}

/**
 * Fail-closed policy: an entry in REVIEW is NEVER automatically disabled.
 * The privileged process validates the allowlist independently on every call.
 */
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
        "com.miui.guardprovider",
        "com.xiaomi.xmsf",
        "com.xiaomi.finddevice"
    )

    private val protectedPrefixes = listOf(
        "android.", "com.android.", "com.google.android."
    )

    // An intentionally tiny initial allowlist, NOT a blanket approval of
    // Xiaomi components on all firmware variants.
    private val reviewedCandidates = setOf(
        "com.miui.analytics",
        "com.miui.msa.global",
        "com.xiaomi.mipicks"
    )

    fun isProtected(pkg: String): Boolean =
        pkg.isBlank() || pkg in protectedExact || protectedPrefixes.any(pkg::startsWith)

    fun canChange(pkg: String): Boolean =
        pkg in reviewedCandidates && !isProtected(pkg)

    fun isXiaomiFamily(manufacturer: String): Boolean =
        manufacturer.lowercase(Locale.ROOT).let {
            it.contains("xiaomi") || it.contains("redmi") || it.contains("poco")
        }

    fun classify(pkg: String, systemApp: Boolean): RiskGroup = when {
        isProtected(pkg) -> RiskGroup.PROTECTED
        !systemApp -> RiskGroup.USER
        canChange(pkg) -> RiskGroup.REVIEW
        else -> RiskGroup.UNKNOWN
    }
}
