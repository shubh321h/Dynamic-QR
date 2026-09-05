package com.agon.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agon.app.data.AppViewModel
import com.agon.app.data.Defaults
import com.agon.app.ui.components.BrandHeader
import com.agon.app.ui.components.SectionCard

@Composable
fun BrandScreen(vm: AppViewModel) {
    val ctx = LocalContext.current
    val ui by vm.state.collectAsState()
    var name by remember(ui.businessName) { mutableStateOf(ui.businessName) }
    var tagline by remember(ui.tagline) { mutableStateOf(ui.tagline) }
    var maps by remember(ui.mapsUrl) { mutableStateOf(ui.mapsUrl) }
    var headline by remember(ui.headline) { mutableStateOf(ui.headline) }
    var thank by remember(ui.thankYou) { mutableStateOf(ui.thankYou) }
    var pin by remember(ui.pin) { mutableStateOf(ui.pin) }
    var saved by remember { mutableStateOf(false) }

    val token = Defaults.extractMapsToken(maps)
    val mapsOk = maps.contains("maps.app.goo.gl") || maps.contains("google") || maps.contains("maps")

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Brand & Review Page", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("What customers see the second they scan. Changes apply instantly — QRs stay the same.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        SectionCard(title = "Live preview", subtitle = "Exactly how your header looks to customers.") {
            BrandHeader(name.ifBlank { Defaults.BUSINESS_NAME }, tagline, maps.ifBlank { Defaults.MAPS_URL })
        }

        SectionCard(title = "Google Business Profile link", subtitle = "Permanently binds every campaign to your Maps review page. Paste your short link — we resolve its ID token below.") {
            OutlinedTextField(maps, { maps = it; saved = false }, Modifier.fillMaxWidth(), label = { Text("Google Maps review URL") }, shape = RoundedCornerShape(12.dp), minLines = 2)
            Spacer(Modifier.height(8.dp))
            Card(colors = CardDefaults.cardColors(containerColor = if (mapsOk) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)), shape = RoundedCornerShape(12.dp)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (mapsOk) Icons.Default.CheckCircle else Icons.Default.Link, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(if (mapsOk) "Resolved profile token: $token" else "That doesn't look like a Google Maps link", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        Text("Default bound profile: ${Defaults.extractMapsToken(Defaults.MAPS_URL)} • goo.gl/pS6FZHudLgQkaoXh7", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { maps = Defaults.MAPS_URL; saved = false }, modifier = Modifier.weight(1f)) { Text("Restore supplied URL") }
            }
        }

        SectionCard(title = "Page wording", subtitle = "Friendly, honest prompts convert best.") {
            OutlinedTextField(name, { name = it; saved = false }, Modifier.fillMaxWidth(), label = { Text("Business name") }, singleLine = true, shape = RoundedCornerShape(12.dp))
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(tagline, { tagline = it; saved = false }, Modifier.fillMaxWidth(), label = { Text("Tagline") }, singleLine = true, shape = RoundedCornerShape(12.dp))
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(headline, { headline = it; saved = false }, Modifier.fillMaxWidth(), label = { Text("Headline question") }, shape = RoundedCornerShape(12.dp))
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(thank, { thank = it; saved = false }, Modifier.fillMaxWidth(), label = { Text("Thank-you message") }, minLines = 3, shape = RoundedCornerShape(12.dp))
        }

        SectionCard(title = "Owner PIN", subtitle = "Protects QRs, analytics & brand. Stored only on this device.") {
            OutlinedTextField(pin, { if (it.length <= 6 && it.all { c -> c.isDigit() }) { pin = it; saved = false } }, Modifier.fillMaxWidth(), label = { Text("4–6 digit PIN") }, singleLine = true, shape = RoundedCornerShape(12.dp))
        }

        Button(onClick = {
            vm.saveBusiness(name, tagline, maps, headline, thank)
            if (pin.length >= 4) vm.savePin(pin)
            saved = true
            Toast.makeText(ctx, "Brand saved — customer page updated", Toast.LENGTH_LONG).show()
        }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(14.dp)) {
            Icon(Icons.Default.Save, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Save all changes", fontWeight = FontWeight.Bold)
        }
        if (saved) Text("✓ Live on the customer review page right now.", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)

        OutlinedButton(onClick = {
            vm.resetAll()
            Toast.makeText(ctx, "Reset to defaults", Toast.LENGTH_SHORT).show()
        }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.RestartAlt, null, Modifier.size(16.dp)); Spacer(Modifier.width(8.dp)); Text("Reset everything")
        }
        Spacer(Modifier.height(8.dp))
    }
}
