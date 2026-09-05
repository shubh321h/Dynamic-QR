package com.agon.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agon.app.data.AppViewModel
import com.agon.app.data.funnelOf
import com.agon.app.data.last14Days
import com.agon.app.data.perCampaign
import com.agon.app.ui.components.FunnelBar
import com.agon.app.ui.components.MiniBarChart
import com.agon.app.ui.components.SectionCard

@Composable
fun DashboardScreen(vm: AppViewModel) {
    val ui by vm.state.collectAsState()
    val events = ui.events
    val funnel = funnelOf(events)
    val per = perCampaign(events)
    val days = last14Days(events)
    val conv = if (funnel.scans == 0) 0 else (funnel.opens * 100 / funnel.scans)
    val maxF = maxOf(funnel.scans, funnel.starts, funnel.copies, funnel.opens, funnel.confirms, 1)

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Analytics", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Privacy-first: only anonymous tap counts. No names, no locations, no review content stored.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("Scans", "${funnel.scans}", "QR scans", Modifier.weight(1f), MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)
            StatCard("Google taps", "${funnel.opens}", "$conv% convert", Modifier.weight(1f), Color(0xFFF59E0B), Color.White)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("Copies", "${funnel.copies}", "review drafts", Modifier.weight(1f), null, null)
            StatCard("Done taps", "${funnel.confirms}", "self-reported", Modifier.weight(1f), null, null)
        }

        SectionCard(title = "Scans — last 14 days", subtitle = "Anonymous scan counts per day.") {
            if (funnel.scans == 0) EmptyHint("No scans yet — tap “Load demo” below or scan a QR.")
            else MiniBarChart(days)
        }

        SectionCard(title = "Funnel", subtitle = "Scan → Start → Copy → Open Google → Done.") {
            FunnelBar("Scans", funnel.scans, maxF, MaterialTheme.colorScheme.primary)
            FunnelBar("Started", funnel.starts, maxF, Color(0xFF38BDF8))
            FunnelBar("Copied", funnel.copies, maxF, Color(0xFFA78BFA))
            FunnelBar("Opened G", funnel.opens, maxF, Color(0xFFF59E0B))
            FunnelBar("Done", funnel.confirms, maxF, Color(0xFF0EA5A0))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Icon(Icons.Default.TrendingUp, null, tint = Color(0xFF067647), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (funnel.scans == 0) "Share your QR at delivery to get the first scan." else if (conv >= 50) "Strong $conv% scan→Google rate. Keep QRs at eye level!" else "Tip: ask crew to mention the 20-second review at handover — lifts conversion.", style = MaterialTheme.typography.bodySmall)
            }
        }

        SectionCard(title = "Per-QR performance", subtitle = "Which printed QR pulls the most scans?") {
            if (ui.campaigns.isEmpty()) EmptyHint("Create a QR first.")
            else {
                val maxC = (ui.campaigns.maxOfOrNull { per[it.id] ?: 0 } ?: 0).coerceAtLeast(1)
                ui.campaigns.forEach { c ->
                    FunnelBar("${c.name.take(12)}", per[c.id] ?: 0, maxC, if (c.isActive) Color(0xFF0EA5A0) else MaterialTheme.colorScheme.outline)
                }
            }
        }

        SectionCard(title = "What we never collect", subtitle = null) {
            Bullet("No customer names, phone numbers or addresses")
            Bullet("No review text leaves the customer's phone except to Google")
            Bullet("No precise location or device fingerprint")
            Bullet("Counts only — stored on this device, deletable anytime")
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(onClick = { vm.loadDemoData() }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.AutoAwesome, null, Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("Load demo") }
            OutlinedButton(onClick = { vm.clearAnalytics() }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.DeleteSweep, null, Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("Clear") }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun StatCard(title: String, value: String, sub: String, mod: Modifier, bg: Color?, fg: Color?) {
    Card(mod, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = bg ?: MaterialTheme.colorScheme.surface), border = if (bg == null) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null) {
        Column(Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = fg ?: MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = fg ?: MaterialTheme.colorScheme.onSurface)
            Text(sub, style = MaterialTheme.typography.labelSmall, color = (fg ?: MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.85f))
        }
    }
}

@Composable
private fun EmptyHint(t: String) {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh).padding(14.dp), contentAlignment = Alignment.Center) {
        Text(t, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Bullet(t: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.TaskAlt, null, tint = Color(0xFF0EA5A0), modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(t, style = MaterialTheme.typography.bodySmall)
    }
}
