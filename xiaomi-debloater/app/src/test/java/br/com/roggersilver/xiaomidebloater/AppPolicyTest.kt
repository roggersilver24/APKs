package br.com.roggersilver.xiaomidebloater

import org.junit.Assert.*
import org.junit.Test

class AppPolicyTest {
    @Test fun systemPackagesAreProtected() {
        assertEquals(RiskGroup.PROTECTED, AppPolicy.classify("com.android.systemui", true))
        assertEquals(RiskGroup.PROTECTED, AppPolicy.classify("com.miui.securitycenter", true))
        assertEquals(RiskGroup.PROTECTED, AppPolicy.classify("com.google.android.gms", true))
        assertFalse(AppPolicy.canChange("com.miui.home"))
    }

    @Test fun candidatesAreReviewOnly() {
        assertEquals(RiskGroup.REVIEW, AppPolicy.classify("com.miui.msa.global", true))
        assertEquals(RiskGroup.REVIEW, AppPolicy.classify("com.miui.analytics", true))
    }

    @Test fun unknownSystemPackageNeverBecomesSafeByDefault() {
        assertEquals(RiskGroup.UNKNOWN, AppPolicy.classify("com.xiaomi.unknown.service", true))
        assertFalse(AppPolicy.canChange("com.xiaomi.unknown.service"))
    }

    @Test fun userAppIsNotTreatedAsBloatware() {
        assertEquals(RiskGroup.USER, AppPolicy.classify("org.example.myapp", false))
    }

    @Test fun onlyXiaomiFamilyCanModify() {
        assertTrue(AppPolicy.isXiaomiFamily("Xiaomi"))
        assertTrue(AppPolicy.isXiaomiFamily("POCO"))
        assertFalse(AppPolicy.isXiaomiFamily("Samsung"))
    }

    @Test fun commandsAreFixedAndNoInjection() {
        assertEquals(
            listOf("pm", "disable-user", "--user", "0", "com.miui.msa.global"),
            SafePackageCommands.arguments("com.miui.msa.global", false)
        )
        assertEquals(
            listOf("pm", "enable", "--user", "0", "com.miui.msa.global"),
            SafePackageCommands.arguments("com.miui.msa.global", true)
        )
        assertNull(SafePackageCommands.arguments("com.miui.msa.global;reboot", false))
        assertNull(SafePackageCommands.arguments("com.miui.home", false))
        assertNull(SafePackageCommands.arguments("--help", false))
    }

    @Test fun disabledListIsMatchedExactly() {
        val output = "package:com.miui.msa.global\npackage:com.miui.analytics\n"
        assertTrue(SafePackageCommands.isPackageDisabled(output, "com.miui.msa.global"))
        assertFalse(SafePackageCommands.isPackageDisabled(output, "com.miui.msa"))
    }
}
