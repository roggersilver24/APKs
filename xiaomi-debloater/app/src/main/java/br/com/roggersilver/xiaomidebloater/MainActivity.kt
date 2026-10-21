package br.com.roggersilver.xiaomidebloater

import android.os.Build
import android.os.Bundle
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
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                var installed by remember { mutableStateOf<List<InstalledApp>>(emptyList()) }
                var query by remember { mutableStateOf("") }
                var selected by remember { mutableStateOf<RiskGroup?>(null) }
                var refreshing by remember { mutableStateOf(true) }
                var requestId by remember { mutableIntStateOf(0) }
                var error by remember { mutableStateOf<String?>(null) }
                LaunchedEffect(requestId) {
                    refreshing = true
                    error = null
                    try { installed = withContext(Dispatchers.IO) { PackageScanner.scan(this@MainActivity) } }
                    catch (e: Exception) { error = e.message ?: "Falha na leitura local" }
                    finally { refreshing = false }
                }
                val filtered = installed.filter {
                    (selected == null || it.group == selected) &&
                    (query.isBlank() || it.label.contains(query, true) || it.packageName.contains(query, true))
                }
                Column(Modifier.fillMaxSize().padding(16.dp)) {
                    Text("Xiaomi Debloater", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("${Build.MANUFACTURER} • ${Build.MODEL} • Android ${Build.VERSION.RELEASE}")
                    Spacer(Modifier.height(10.dp))
                    Text("Modo seguro: somente analise. Nenhum app sera removido ou desativado.", color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text("Buscar aplicativo ou pacote") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { selected = null }) { Text("Todos") }
                        TextButton(onClick = { selected = RiskGroup.REVIEW }) { Text("Revisar") }
                        TextButton(onClick = { selected = RiskGroup.PROTECTED }) { Text("Protegidos") }
                    }
                    Text("Exibindo ${filtered.size} de ${installed.size} aplicativos", style = MaterialTheme.typography.labelMedium)
                    Button(onClick = { requestId++ }, enabled = !refreshing) { Text(if (refreshing) "Analisando..." else "Atualizar leitura") }
                    error?.let { Text("Erro: $it", color = MaterialTheme.colorScheme.error) }
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(filtered, key = { it.packageName }) { app ->
                            Card(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(app.label, fontWeight = FontWeight.SemiBold)
                                    Text(app.packageName, style = MaterialTheme.typography.bodySmall)
                                    Text(app.group.title + " • " + if (app.systemApp) "Sistema" else "Usuario", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
