package br.com.roggersilver.xiaomidebloater

import org.junit.Assert.assertEquals
import org.junit.Test

class AppPolicyTest {
    @Test fun systemPackagesAreProtected() {
        assertEquals(RiskGroup.PROTECTED, AppPolicy.classify("com.android.systemui", true))
        assertEquals(RiskGroup.PROTECTED, AppPolicy.classify("com.miui.securitycenter", true))
        assertEquals(RiskGroup.PROTECTED, AppPolicy.classify("com.google.android.gms", true))
    }
    @Test fun candidatesAreReviewOnly() {
        assertEquals(RiskGroup.REVIEW, AppPolicy.classify("com.miui.msa.global", true))
        assertEquals(RiskGroup.REVIEW, AppPolicy.classify("com.miui.analytics", true))
    }
    @Test fun unknownSystemPackageNeverBecomesSafeByDefault() {
        assertEquals(RiskGroup.UNKNOWN, AppPolicy.classify("com.xiaomi.unknown.service", true))
    }
    @Test fun userAppIsNotTreatedAsBloatware() {
        assertEquals(RiskGroup.USER, AppPolicy.classify("org.example.myapp", false))
    }
}
