package com.agon.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.agon.app.data.AppViewModel
import com.agon.app.data.Campaign
import com.agon.app.data.Defaults
import com.agon.app.data.EventTypes
import com.agon.app.data.perCampaign
import com.agon.app.data.rememberQrImage
import com.agon.app.data.renderQrBitmap
import com.agon.app.ui.components.SectionCard
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScreen(vm: AppViewModel, onPreviewCustomer: (String) -> Unit) {
    val ctx = LocalContext.current
    val ui by vm.state.collectAsState()
    val scans = perCampaign(ui.events)
    var showAdd by remember { mutableStateOf(false) }
    var selected: Campaign? by remember { mutableStateOf(null) }
    var confirmDelete: Campaign? by remember { mutableStateOf(null) }

    Scaffold(floatingActionButton = {
        ExtendedFloatingActionButton(onClick = { showAdd = true }, icon = { Icon(Icons.Default.Add, null) }, text = { Text("New QR") })
    }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text("Dynamic QR Codes", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("One printed QR forever — change where it points anytime. Scanning logs an anonymous count only.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
            }
            items(ui.campaigns, key = { it.id }) { c ->
                val count = scans[c.id] ?: 0
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth().clickable { selected = c; vm.logEvent(EventTypes.SCAN, c.id) }
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        val img = rememberQrImage(Defaults.payloadFor(c.id), 256)
                        if (img != null) Image(img, "QR", Modifier.size(76.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Fit)
                        else Box(Modifier.size(76.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh), contentAlignment = Alignment.Center) { Icon(Icons.Default.QrCode2, null) }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(c.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                StatusPill(c.isActive)
                            }
                            Text("${c.id} • ${c.label}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("→ ${c.dynamicDestination.take(44)}${if (c.dynamicDestination.length > 44) "…" else ""}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, maxLines = 1)
                            Text("$count scans", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(70.dp)) }
        }
    }

    if (showAdd) AddCampaignDialog(onDismiss = { showAdd = false }, onSave = { n, l, d -> vm.addCampaign(n, l, d); showAdd = false })

    selected?.let { c ->
        ModalBottomSheet(onDismissRequest = { selected = null }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
            QrDetail(ctx, vm, c, ui.businessName, ui.tagline, scans[c.id] ?: 0,
                onPreview = { onPreviewCustomer(c.id) },
                onDelete = { confirmDelete = c; selected = null })
        }
    }
    confirmDelete?.let { c ->
        AlertDialog(onDismissRequest = { confirmDelete = null }, title = { Text("Delete ${c.name}?") },
            text = { Text("Printed QRs with this code will stop resolving. This cannot be undone.") },
            confirmButton = { Button(onClick = { vm.deleteCampaign(c.id); confirmDelete = null }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { confirmDelete = null }) { Text("Keep") } })
    }
}

@Composable
private fun StatusPill(active: Boolean) {
    val bg = if (active) Color(0xFFDEF7EC) else MaterialTheme.colorScheme.surfaceContainerHigh
    val fg = if (active) Color(0xFF03543F) else MaterialTheme.colorScheme.onSurfaceVariant
    Box(Modifier.clip(RoundedCornerShape(50)).background(bg).padding(horizontal = 8.dp, vertical = 3.dp)) {
        Text(if (active) "● LIVE" else "○ PAUSED", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = fg)
    }
}

@Composable
private fun AddCampaignDialog(onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var n by remember { mutableStateOf("") }
    var l by remember { mutableStateOf("") }
    var d by remember { mutableStateOf(Defaults.MAPS_URL) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("New dynamic QR") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(n, { n = it }, label = { Text("Name — e.g. Diwali Offer QR") }, singleLine = true, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(l, { l = it }, label = { Text("Placement — e.g. Truck #12") }, singleLine = true, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(d, { d = it }, label = { Text("Destination URL") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(), minLines = 2)
            }
        },
        confirmButton = { Button(onClick = { onSave(n, l, d) }) { Text("Create") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

@Composable
private fun QrDetail(ctx: Context, vm: AppViewModel, c: Campaign, biz: String, tagline: String, scans: Int, onPreview: () -> Unit, onDelete: () -> Unit) {
    val scope = rememberCoroutineScope()
    var dest by remember(c.id) { mutableStateOf(c.dynamicDestination) }
    var active by remember(c.id) { mutableStateOf(c.isActive) }
    var savedTick by remember { mutableStateOf(false) }
    val big = rememberQrImage(Defaults.payloadFor(c.id), 768)
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(c.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("${c.id} • $scans scans • ${c.label}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error) }
        }
        // print-ready card (this is what owner prints)
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(4.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF0F2A44)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.QrCode2, null, tint = Color(0xFFFBBF24))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(biz.uppercase(), color = Color(0xFF0F2A44), fontWeight = FontWeight.Black)
                        Text(tagline, color = Color(0xFF475467), style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(Modifier.height(12.dp))
                if (big != null) Image(big, "QR large", Modifier.size(230.dp))
                Spacer(Modifier.height(10.dp))
                Text("Scan • Review • Done in 20 seconds", color = Color(0xFF0F2A44), fontWeight = FontWeight.Bold)
                Text("Your honest Google review moves us forward", color = Color(0xFF475467), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                Spacer(Modifier.height(6.dp))
                Box(Modifier.clip(RoundedCornerShape(50)).background(Color(0xFFF59E0B)).padding(horizontal = 14.dp, vertical = 6.dp)) {
                    Text(c.id, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(onClick = {
                scope.launch {
                    try {
                        val bmp = renderQrBitmap(Defaults.payloadFor(c.id), 1024)
                        shareBitmap(ctx, bmp, "${c.id}_QR.png")
                    } catch (_: Exception) { Toast.makeText(ctx, "Share failed", Toast.LENGTH_SHORT).show() }
                }
            }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Share, null, Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("Share PNG") }
            FilledTonalButton(onClick = {
                val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("qr", Defaults.payloadFor(c.id)))
                Toast.makeText(ctx, "QR link copied", Toast.LENGTH_SHORT).show()
            }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.ContentCopy, null, Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("Copy link") }
        }
        SectionCard(title = "Dynamic destination", subtitle = "Change where this printed QR points — no reprint needed. Currently:") {
            OutlinedTextField(dest, { dest = it; savedTick = false }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), minLines = 2, label = { Text("Destination URL") })
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Campaign active", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Switch(active, { active = it })
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    vm.updateCampaign(c.copy(dynamicDestination = dest.ifBlank { Defaults.MAPS_URL }, isActive = active))
                    savedTick = true
                    Toast.makeText(ctx, "Saved — printed QR now points here", Toast.LENGTH_LONG).show()
                }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.CheckCircle, null, Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("Save") }
                OutlinedButton(onClick = {
                    try { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(dest)).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }) }
                    catch (_: Exception) { Toast.makeText(ctx, "Bad URL", Toast.LENGTH_SHORT).show() }
                }) { Icon(Icons.Default.OpenInNew, null, Modifier.size(16.dp)) }
            }
            if (savedTick) Text("✓ Live — same QR, new destination.", color = Color(0xFF067647), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        }
        Button(onClick = onPreview, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(14.dp)) {
            Icon(Icons.Default.OpenInNew, null); Spacer(Modifier.width(8.dp)); Text("Simulate scan → customer view")
        }
        OutlinedButton(onClick = { Toast.makeText(ctx, "Use Share PNG, then print — works with any printer", Toast.LENGTH_LONG).show() }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Print, null, Modifier.size(16.dp)); Spacer(Modifier.width(8.dp)); Text("How to print")
        }
        Spacer(Modifier.height(30.dp))
    }
}

private fun shareBitmap(ctx: Context, bmp: Bitmap, name: String) {
    val dir = File(ctx.cacheDir, "qr").apply { mkdirs() }
    val f = File(dir, name)
    FileOutputStream(f).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    val uri = FileProvider.getUriForFile(ctx, ctx.packageName + ".provider", f)
    ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
        type = "image/png"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }, "Share QR").apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
}
