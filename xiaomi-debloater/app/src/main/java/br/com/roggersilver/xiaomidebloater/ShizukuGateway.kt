package br.com.roggersilver.xiaomidebloater

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku

data class ShizukuSnapshot(
    val description: String,
    val ready: Boolean = false,
    val active: Boolean = false
)

class ShizukuGateway(context: Context) {
    companion object {
        private const val REQUEST_CODE = 710
    }
    private val args = Shizuku.UserServiceArgs(
        ComponentName(context.packageName, PrivilegedPackageService::class.java.name)
    )
        .daemon(false)
        .processNameSuffix("safe-pm")
        .tag("xiaomi-safe-pm")
        .version(2)

    private val mutableSnapshot = MutableStateFlow(ShizukuSnapshot("Verificando Shizuku..."))
    val snapshot: StateFlow<ShizukuSnapshot> = mutableSnapshot

    @Volatile private var service: IPackageControl? = null
    private var requestedBind = false
    private var started = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            val candidate = IPackageControl.Stub.asInterface(binder)
            try {
                val uid = candidate.backendUid()
                if (uid != 2000 && uid != 0) {
                    service = null
                    mutableSnapshot.value = ShizukuSnapshot("Identidade inválida do serviço. Operações bloqueadas.")
                    return
                }
                service = candidate
                mutableSnapshot.value = ShizukuSnapshot(
                    "Conectado com autorização. Processo protegido (UID $uid).",
                    ready = true, active = true
                )
            } catch (e: Exception) {
                service = null
                mutableSnapshot.value = ShizukuSnapshot("Não foi possível validar o serviço.")
            }
        }
        override fun onServiceDisconnected(name: ComponentName) {
            service = null
            requestedBind = false
            updateStatus()
        }
    }

    private val onBinder = Shizuku.OnBinderReceivedListener {
        requestedBind = false
        updateStatus()
    }
    private val onBinderDead = Shizuku.OnBinderDeadListener {
        service = null
        requestedBind = false
        updateStatus()
    }
    private val onPermission = Shizuku.OnRequestPermissionResultListener { requestCode, result ->
        if (requestCode == REQUEST_CODE) {
            if (result == PackageManager.PERMISSION_GRANTED) bind()
            else mutableSnapshot.value = ShizukuSnapshot("Autorização negada. Você pode solicitar novamente no Shizuku.", active = true)
        }
    }

    fun start() {
        if (started) return
        started = true
        Shizuku.addBinderReceivedListenerSticky(onBinder)
        Shizuku.addBinderDeadListener(onBinderDead)
        Shizuku.addRequestPermissionResultListener(onPermission)
        updateStatus()
    }

    fun connect() {
        if (!Shizuku.pingBinder()) {
            updateStatus()
            return
        }
        try {
            if (Shizuku.isPreV11()) {
                mutableSnapshot.value = ShizukuSnapshot("Versão antiga do Shizuku. Atualize para a versão 13.", active = true)
                return
            }
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                if (Shizuku.shouldShowRequestPermissionRationale()) {
                    mutableSnapshot.value = ShizukuSnapshot("Autorize o aplicativo diretamente no gerenciador Shizuku.", active = true)
                } else {
                    Shizuku.requestPermission(REQUEST_CODE)
                    mutableSnapshot.value = ShizukuSnapshot("Aguardando autorização do Shizuku...", active = true)
                }
            } else bind()
        } catch (e: Exception) {
            mutableSnapshot.value = ShizukuSnapshot("Falha ao solicitar permissão: ${e.message?.take(100)}", active = true)
        }
    }

    private fun bind() {
        try {
            if (!Shizuku.pingBinder() || Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                updateStatus()
                return
            }
            if (service?.asBinder()?.pingBinder() == true) {
                updateStatus()
                return
            }
            if (requestedBind) return
            requestedBind = true
            mutableSnapshot.value = ShizukuSnapshot("Abrindo serviço autorizado...", active = true)
            Shizuku.bindUserService(args, connection)
        } catch (e: Exception) {
            requestedBind = false
            mutableSnapshot.value = ShizukuSnapshot("Não foi possível conectar: ${e.message?.take(100)}", active = true)
        }
    }

    fun updateStatus() {
        try {
            val current = service
            mutableSnapshot.value = when {
                !Shizuku.pingBinder() ->
                    ShizukuSnapshot("Shizuku inativo. Instale, abra e inicie o serviço.")
                Shizuku.isPreV11() ->
                    ShizukuSnapshot("Shizuku antigo não suportado. Atualize para v13.", active = true)
                Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED ->
                    ShizukuSnapshot("Shizuku iniciado, mas ainda sem autorização para este aplicativo.", active = true)
                current?.asBinder()?.pingBinder() == true ->
                    ShizukuSnapshot("Shizuku autorizado e serviço conectado.", ready = true, active = true)
                else ->
                    ShizukuSnapshot("Shizuku autorizado. Toque em Conectar para iniciar o serviço.", active = true)
            }
        } catch (e: Exception) {
            mutableSnapshot.value = ShizukuSnapshot("Shizuku indisponível. Verifique o serviço.")
        }
    }

    suspend fun changePackage(packageName: String, enable: Boolean): String =
        withContext(Dispatchers.IO) {
            if (!AppPolicy.canChange(packageName)) {
                return@withContext "ERRO: o aplicativo não está na lista de revisão."
            }
            if (!Shizuku.pingBinder() ||
                Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED
            ) {
                return@withContext "ERRO: Shizuku sem autorização."
            }
            val current = service
            if (current == null || !current.asBinder().pingBinder()) {
                return@withContext "ERRO: serviço Shizuku desconectado."
            }
            try {
                current.setEnabledState(packageName, enable)
            } catch (e: Exception) {
                "ERRO: falha de comunicação com Shizuku: ${e.message?.take(120)}"
            }
        }

    fun stop() {
        if (!started) return
        started = false
        Shizuku.removeBinderReceivedListener(onBinder)
        Shizuku.removeBinderDeadListener(onBinderDead)
        Shizuku.removeRequestPermissionResultListener(onPermission)
        if (requestedBind && Shizuku.pingBinder()) {
            runCatching { Shizuku.unbindUserService(args, connection, true) }
        }
        service = null
        requestedBind = false
    }
}
