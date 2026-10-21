package br.com.roggersilver.xiaomidebloater

import android.os.Process
import java.util.concurrent.TimeUnit

/**
 * Shizuku UserService: this class is instantiated by Shizuku under shell/root,
 * NOT by Android's normal Service framework.
 *
 * Every Binder request is revalidated inside this privileged process.
 * No arbitrary commands, uninstall, root scripts, or non-user-0 operations.
 */
class PrivilegedPackageService : IPackageControl.Stub() {
    override fun backendUid(): Int = Process.myUid()

    override fun setEnabledState(packageName: String, enable: Boolean): String {
        if (backendUid() != 2000 && backendUid() != 0) {
            return "ERRO: processo sem identidade Shizuku shell/root."
        }
        val args = SafePackageCommands.arguments(packageName, enable)
            ?: return "ERRO: pacote não autorizado. Alteração bloqueada."
        val before = run(SafePackageCommands.disabledListArguments())
        if (before.code != 0 || before.timedOut) return "ERRO: não foi possível ler o estado atual."
        val wasDisabled = SafePackageCommands.isPackageDisabled(before.output, packageName)
        if (wasDisabled == !enable) return "OK: aplicativo já estava no estado solicitado."

        val result = run(args)
        if (result.timedOut) return "ERRO: comando excedeu o tempo limite. Confira o estado antes de repetir."
        if (result.code != 0) return "ERRO: o sistema recusou a operação. ${result.output.take(240)}"

        val after = run(SafePackageCommands.disabledListArguments())
        if (after.code != 0 || after.timedOut) {
            return "INDEFINIDO: comando executado, mas não foi possível verificar o resultado."
        }
        val nowDisabled = SafePackageCommands.isPackageDisabled(after.output, packageName)
        return if (nowDisabled == !enable) {
            if (enable) "OK: aplicativo reativado e estado confirmado."
            else "OK: aplicativo desativado e estado confirmado."
        } else {
            "INDEFINIDO: o estado não mudou como esperado. Não repita sem conferir."
        }
    }

    private data class CommandResult(val code: Int, val output: String, val timedOut: Boolean)

    private fun run(arguments: List<String>): CommandResult = try {
        val process = ProcessBuilder(arguments)
            .redirectErrorStream(true)
            .start()
        if (!process.waitFor(12, TimeUnit.SECONDS)) {
            process.destroyForcibly()
            CommandResult(-1, "", true)
        } else {
            // These pm operations produce bounded, short text output.
            val output = process.inputStream.bufferedReader().use { it.readText().take(3000) }
            CommandResult(process.exitValue(), output, false)
        }
    } catch (e: Exception) {
        CommandResult(-1, e.message?.take(240) ?: "falha ao executar", false)
    }

    // Reserved by Shizuku's server to terminate UserService after unbind(remove=true).
    override fun destroy() {
        System.exit(0)
    }
}
