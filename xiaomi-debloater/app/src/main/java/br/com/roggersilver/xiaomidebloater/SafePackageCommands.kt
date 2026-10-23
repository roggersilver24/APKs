package br.com.roggersilver.xiaomidebloater

/**
 * No free-form terminal: there is no user-controlled shell, and args are
 * passed directly to ProcessBuilder (never via 'sh -c').
 */
object SafePackageCommands {
    private val validPackage = Regex("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+")

    fun arguments(pkg: String, enable: Boolean): List<String>? {
        if (!validPackage.matches(pkg) || !AppPolicy.canChange(pkg)) return null
        return listOf("pm", if (enable) "enable" else "disable-user", "--user", "0", pkg)
    }

    fun disabledListArguments(): List<String> =
        listOf("pm", "list", "packages", "-d", "--user", "0")

    fun isPackageDisabled(output: String, pkg: String): Boolean =
        output.lineSequence().any { it.trim() == "package:$pkg" }
}
