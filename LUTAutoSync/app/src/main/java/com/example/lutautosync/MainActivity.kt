package com.example.lutautosync

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.lutautosync.data.*
import com.example.lutautosync.service.SyncForegroundService
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val picker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let(::importLut) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContainer.init(this)
        setContent {
            MaterialTheme {
                Home(
                    onImport = { picker.launch(arrayOf("*/*")) },
                    onSetDefault = { lut -> lifecycleScope.launch { AppContainer.db.lutDao().clearDefault(); AppContainer.db.lutDao().update(lut.copy(isDefault = true)) } },
                    onToggle = { enabled -> if (enabled) { requestBatteryExemption(); startForegroundService(Intent(this, SyncForegroundService::class.java)) } else stopService(Intent(this, SyncForegroundService::class.java)) }
                )
            }
        }
    }

    private fun requestBatteryExemption() {
        val power = getSystemService(PowerManager::class.java)
        if (power?.isIgnoringBatteryOptimizations(packageName) == false) startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:$packageName")))
    }

    private fun importLut(uri: Uri) {
        contentResolver.openInputStream(uri)?.use { input ->
            val file = java.io.File(filesDir, "lut_${System.currentTimeMillis()}.cube")
            file.outputStream().use(input::copyTo)
            lifecycleScope.launch { AppContainer.db.lutDao().insert(LutEntity(name = file.name, path = file.path, format = "cube")) }
        }
    }
}

@Composable
private fun Home(onImport: () -> Unit, onSetDefault: (LutEntity) -> Unit, onToggle: (Boolean) -> Unit) {
    val luts by AppContainer.db.lutDao().observeAll().collectAsStateWithLifecycle(emptyList())
    val count by AppContainer.db.processedDao().count().collectAsStateWithLifecycle(0)
    var enabled by rememberSaveable { mutableStateOf(false) }
    Scaffold(topBar = { TopAppBar(title = { Text("LUT Auto Sync") }) }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Text("Background monitoring", style = MaterialTheme.typography.titleMedium); Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { Switch(enabled, onCheckedChange = { enabled = it; onToggle(it) }); Text(if (enabled) "Running" else "Stopped") } }
            item { Text("Default LUT: ${luts.firstOrNull { it.isDefault }?.name ?: "Not set"}") }
            item { Text("Processed photos: $count") }
            item { Button(onClick = onImport) { Text("Import LUT") } }
            item { Text("LUT library", style = MaterialTheme.typography.titleMedium) }
            items(luts) { lut -> ListItem(headlineContent = { Text(lut.name) }, supportingContent = { Text(lut.format.uppercase()) }, trailingContent = { TextButton(onClick = { onSetDefault(lut) }) { Text(if (lut.isDefault) "Default" else "Set default") } }) }
        }
    }
}
