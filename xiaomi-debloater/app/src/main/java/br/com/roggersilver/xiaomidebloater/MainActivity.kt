package br.com.roggersilver.xiaomidebloater

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Date

class MainActivity : ComponentActivity() {
    private lateinit var gateway: ShizukuGateway

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gateway = ShizukuGateway(applicationContext)
        gateway.start()
        setContent { MaterialTheme { DebloaterScreen() } }
    }

    override fun onDestroy() {
        gateway.stop()
        super.onDestroy()
    }

    @Composable
    private fun DebloaterScreen() {
        val connection by gateway.snapshot.collectAsState()
        val scope = rememberCoroutineScope()
        var installed by remember { mutableStateOf<List<InstalledApp>>(emptyList()) }
        var search by remember { mutableStateOf("") }
        var filter by remember { mutableStateOf<RiskGroup?>(null) }
        var refreshing by remember { mutableStateOf(true) }
        var refreshId by remember { mutableIntStateOf(0) }
        var pendingApp by remember { mutableStateOf<InstalledApp?>(null) }
        var operationInProgress by remember { mutableStateOf(false) }
        var notice by remember { mutableStateOf("") }
        var history by remember { mutableStateOf(ActionHistory.read(this@MainActivity)) }

        // Restrict all privileged changes to the primary Android user and Xiaomi-family devices.
        val supportedDevice = AppPolicy.isXiaomiFamily(Build.MANUFACTURER)
        val primaryUser = Process.myUid() < 100000
        val canChange = supportedDevice && primaryUser && connection.ready

        LaunchedEffect(refreshId) {
            refreshing = true
            try {
                installed = withContext(Dispatchers.IO) { PackageScanner.scan(this@MainActivity) }
            } catch (e: Exception) {
                notice = "Não foi possível consultar os aplicativos: ${e.message}"
            } finally {
                refreshing = false
            }
        }

        val visible = installed.filter {
            (filter == null || it.group == filter) &&
                (search.isBlank() ||
                    it.label.contains(search, ignoreCase = true) ||
                    it.packageName.contains(search, ignoreCase = true))
        }
        val candidates = installed.count { it.group == RiskGroup.REVIEW }
        val protected = installed.count { it.group == RiskGroup.PROTECTED }

        Column(Modifier.fillMaxSize().padding(14.dp)) {
            Text("Xiaomi Debloater", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("${Build.MANUFACTURER} • ${Build.MODEL} • Android ${Build.VERSION.RELEASE}",
                style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))

            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(12.dp)) {
                    Text("Conexão Shizuku", fontWeight = FontWeight.SemiBold)
                    Text(connection.description, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { gateway.connect() },
                            enabled = !connection.ready && !operationInProgress
                        ) { Text(if (connection.ready) "Conectado" else "Autorizar / Conectar") }
                        TextButton(onClick = {
                            val link = Intent(Intent.ACTION_VIEW, Uri.parse("https://shizuku.rikka.app/download/"))
                            startActivity(link)
                        }) { Text("Instalar Shizuku") }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            if (!supportedDevice) {
                Text("Alterações desativadas: aparelho não reconhecido como Xiaomi, Redmi ou POCO.",
                    color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            } else if (!primaryUser) {
                Text("Modo secundário detectado: mudanças só são permitidas no usuário principal (0).",
                    color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            } else {
                Text("Desativação reversível apenas para pacotes revisados. Sem desinstalação.",
                    color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(8.dp))

            Text("Inventário: ${installed.size} apps • ${candidates} para revisão • ${protected} protegidos",
                style = MaterialTheme.typography.labelMedium)
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("Pesquisar nome ou pacote") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = { filter = null }) { Text("Todos") }
                TextButton(onClick = { filter = RiskGroup.REVIEW }) { Text("Revisar") }
                TextButton(onClick = { filter = RiskGroup.PROTECTED }) { Text("Protegidos") }
                TextButton(onClick = { filter = RiskGroup.UNKNOWN }) { Text("Outros") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Exibindo ${visible.size} aplicativos", style = MaterialTheme.typography.labelSmall)
                TextButton(onClick = { refreshId++ }, enabled = !refreshing && !operationInProgress) {
                    Text(if (refreshing) "Lendo..." else "Atualizar")
                }
            }
            if (notice.isNotBlank()) {
                Text(notice, style = MaterialTheme.typography.bodySmall,
                    color = if (notice.startsWith("OK")) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(4.dp))

            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(visible, key = { it.packageName }) { app ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(app.label, fontWeight = FontWeight.SemiBold)
                            Text(app.packageName, style = MaterialTheme.typography.bodySmall)
                            Text(
                                app.group.title + " • " + if (app.systemApp) "Sistema" else "Usuário"
                                    + if (!app.enabled) " • Desativado" else "",
                                style = MaterialTheme.typography.labelSmall
                            )
                            if (app.group == RiskGroup.REVIEW && app.systemApp && supportedDevice && primaryUser) {
                                Spacer(Modifier.height(4.dp))
                                OutlinedButton(
                                    onClick = { pendingApp = app },
                                    enabled = canChange && !operationInProgress && !refreshing
                                ) {
                                    Text(if (app.enabled) "Desativar com confirmação" else "Restaurar aplicativo")
                                }
                            }
                        }
                    }
                }
                if (history.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text("Últimas operações verificadas", fontWeight = FontWeight.SemiBold)
                        history.take(6).forEach { h ->
                            Text(
                                (if (h.enabled) "Restaurado" else "Desativado") + ": " + h.packageName +
                                    " • " + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                                    .format(Date(h.timestamp)),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        pendingApp?.let { app ->
            val enable = !app.enabled
            AlertDialog(
                onDismissRequest = { if (!operationInProgress) pendingApp = null },
                title = { Text(if (enable) "Restaurar aplicativo?" else "Desativar aplicativo?") },
                text = {
                    Text(
                        "${app.label}\n${app.packageName}\n\n" +
                            if (enable)
                                "O sistema tentará reativar este pacote para o usuário principal. Pode falhar dependendo da ROM."
                            else
                                "Mesmo apps de anúncios podem afetar recursos da MIUI/HyperOS. O app será desativado (não excluído)."
                    )
                },
                confirmButton = {
                    Button(
                        enabled = !operationInProgress && canChange && AppPolicy.canChange(app.packageName),
                        onClick = {
                            operationInProgress = true
                            scope.launch {
                                val result = gateway.changePackage(app.packageName, enable)
                                notice = result
                                if (result.startsWith("OK:")) {
                                    ActionHistory.record(this@MainActivity, app.packageName, enable)
                                    history = ActionHistory.read(this@MainActivity)
                                }
                                pendingApp = null
                                operationInProgress = false
                                refreshId++
                            }
                        }
                    ) { Text(if (operationInProgress) "Executando..." else "Confirmar") }
                },
                dismissButton = {
                    TextButton(onClick = { pendingApp = null }, enabled = !operationInProgress) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
