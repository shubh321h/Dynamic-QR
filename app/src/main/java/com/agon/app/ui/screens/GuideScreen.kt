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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agon.app.ui.components.SectionCard
import com.agon.app.ui.components.StepRow

@Composable
fun GuideScreen(onGoCustomer: () -> Unit, onGoQr: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Success Flow", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("The full journey — customer control at every step, owner control behind the scenes.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, null, tint = Color(0xFFFBBF24), modifier = Modifier.size(30.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Honest reviews only", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("We never pre-select stars, auto-submit, fake, or bypass Google. The customer always taps Post on Google themselves.", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        SectionCard(title = "Customer journey  (after delivery)", subtitle = "~20 seconds, mobile-first, touch-friendly.") {
            StepRow("1", "Scan the QR", "On the truck sticker, standee or invoice. Opens your branded thank-you page. No app or login needed.")
            StepRow("2", "See your brand", "Logo, thank-you note and the honest prompt: “How was your move?” — bound to your Google Maps page.")
            StepRow("3", "Tap stars honestly", "Stars start EMPTY and are never manipulated. 1–3★ shows a caring “we'll fix it” note.")
            StepRow("4", "Answer 3 optional taps", "Service type, what went well, tone. A draft review is generated — fully editable.")
            StepRow("5", "Continue to Google", "One big CTA copies the text and opens your Maps review page (goo.gl/pS6FZHudLgQkaoXh7).")
            StepRow("6", "Paste & Post on Google", "Customer pastes, edits if they like, and taps Post on Google. Only they can publish.")
            StepRow("7", "Done", "A thank-you confirms. “I posted” logs one anonymous Done count.")
            Spacer(Modifier.height(8.dp))
            Button(onClick = onGoCustomer, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Default.Person, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Try the customer view")
            }
        }

        SectionCard(title = "Owner journey  (you)", subtitle = "PIN-protected, everything on-device.") {
            StepRow("1", "Bind your Maps URL once", "Brand tab → paste goo.gl link → token resolves (pS6FZHudLgQkaoXh7). All QRs inherit it.")
            StepRow("2", "Create dynamic QRs", "One per truck, counter, invoice. Print once — retarget anytime without reprinting.")
            StepRow("3", "Simulate a scan", "QR tab → tap a code → “Simulate scan” previews exactly what customers see.")
            StepRow("4", "Track analytics", "Scans, funnel, per-QR chart. Anonymous counts only — fully private.")
            StepRow("5", "Tune wording", "Edit headline, thank-you, tagline in Brand. Instant on the customer page.")
            Spacer(Modifier.height(8.dp))
            Button(onClick = onGoQr, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Default.QrCode2, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Open my QR codes")
            }
        }

        SectionCard(title = "Security & scale", subtitle = null) {
            SecRow("PIN gate", "Owner tabs locked behind a device-only PIN.")
            SecRow("No secrets in QR", "QRs hold a short campaign code; destinations live in your editable store.")
            SecRow("On-device store", "DataStore persistence; no servers, no SDKs, offline-first.")
            SecRow("Modular code", "data / ui/screens / ui/components / ReviewGen split — add backend later cleanly.")
            SecRow("Compliance", "No automation of Google submission, no rating gating, no incentives-for-stars.")
        }

        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AdminPanelSettings, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(10.dp))
                Text("Default owner PIN is 1234 — change it in Brand → Owner PIN before printing QRs.", style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SecRow(t: String, b: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Box(Modifier.size(10.dp).clip(androidx.compose.foundation.shape.CircleShape).background(MaterialTheme.colorScheme.tertiary).align(Alignment.CenterVertically))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(t, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            Text(b, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
