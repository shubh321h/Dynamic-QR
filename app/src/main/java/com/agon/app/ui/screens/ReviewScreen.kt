package com.agon.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agon.app.data.AppViewModel
import com.agon.app.data.Defaults
import com.agon.app.data.EventTypes
import com.agon.app.data.ReviewGen
import com.agon.app.ui.components.BrandHeader
import com.agon.app.ui.components.SectionCard
import com.agon.app.ui.components.SelectChip
import com.agon.app.ui.components.StarSelector
import com.agon.app.ui.components.StepRow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReviewScreen(vm: AppViewModel, campaignId: String?) {
    val ctx = LocalContext.current
    val ui by vm.state.collectAsState()
    val campaigns = ui.campaigns
    var selectedId by remember(campaignId, campaigns) {
        mutableStateOf(campaignId ?: campaigns.firstOrNull { it.isActive }?.id ?: campaigns.firstOrNull()?.id ?: "")
    }
    val campaign = campaigns.firstOrNull { it.id == selectedId } ?: campaigns.firstOrNull()
    val destination = campaign?.dynamicDestination?.ifBlank { ui.mapsUrl } ?: ui.mapsUrl

    var stars by remember { mutableStateOf(0) }
    var service by remember { mutableStateOf(Defaults.SERVICE_TYPES[0]) }
    val positives = remember { mutableStateListOf<String>() }
    var tone by remember { mutableStateOf(Defaults.TONES[0]) }
    var name by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var crew by remember { mutableStateOf("") }
    var reviewText by remember {
        mutableStateOf(ReviewGen.build("", 5, Defaults.SERVICE_TYPES[0], emptyList(), Defaults.TONES[0], "", ""))
    }
    var edited by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var showGoogleSheet by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    fun regenerate() {
        val eff = if (stars == 0) 5 else stars
        reviewText = ReviewGen.build(name, eff, service, positives.toList(), tone, crew, city)
        edited = false
    }

    LaunchedEffect(campaign?.id) {
        if (campaign != null) vm.logEvent(EventTypes.START, campaign.id)
    }

    fun copy(text: String) {
        val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("review", text))
        campaign?.let { vm.logEvent(EventTypes.COPY, it.id) }
        Toast.makeText(ctx, "Review copied — paste it on Google", Toast.LENGTH_LONG).show()
    }

    fun openGoogle() {
        if (reviewText.isNotBlank()) copy(reviewText)
        campaign?.let { vm.logEvent(EventTypes.OPEN_GOOGLE, it.id) }
        try {
            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(destination)).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
        } catch (_: Exception) {
            Toast.makeText(ctx, "Couldn't open link", Toast.LENGTH_SHORT).show()
        }
        showGoogleSheet = true
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        BrandHeader(ui.businessName, ui.tagline, ui.mapsUrl)

        // campaign picker (which QR was scanned)
        if (campaigns.size > 1) {
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = campaign?.name ?: "Select location",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("You scanned — location") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    campaigns.filter { it.isActive }.forEach { c ->
                        DropdownMenuItem(text = { Text("${c.name} • ${c.id}") }, onClick = { selectedId = c.id; expanded = false })
                    }
                }
            }
        }

        // progress steps
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
            Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                StepDot("1", "Rate", true)
                StepDot("2", "Review", stars > 0)
                StepDot("3", "Post", false)
            }
        }

        SectionCard(title = ui.headline, subtitle = ui.thankYou) {
            Text("Tap honestly — every rating helps us improve. Stars are never pre-filled.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            StarSelector(stars) { stars = it; if (!edited) regenerate() }
            Text(
                ReviewGen.starLabel(stars),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (stars == 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
            )
            if (ReviewGen.needsAttention(stars)) {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("We're sorry it wasn't perfect. Please share honestly below — our manager reviews every note.", style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(4.dp))
            }
        }

        SectionCard(title = "Tell us a little (optional)", subtitle = "Pick a few — we draft your review instantly. Nothing is posted automatically.") {
            Text("Service used", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Defaults.SERVICE_TYPES.forEach { s -> SelectChip(s, s == service) { service = s; if (!edited) regenerate() } }
            }
            Spacer(Modifier.height(12.dp))
            Text("What went well? (pick any)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Defaults.POSITIVES.forEach { p ->
                    SelectChip(p, positives.contains(p)) {
                        if (positives.contains(p)) positives.remove(p) else positives.add(p)
                        if (!edited) regenerate()
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("Tone", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Defaults.TONES.forEach { t -> SelectChip(t, t == tone) { tone = t; if (!edited) regenerate() } }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Your name (optional)") }, singleLine = true, shape = RoundedCornerShape(14.dp))
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(city, { city = it }, Modifier.weight(1f), label = { Text("City") }, singleLine = true, shape = RoundedCornerShape(14.dp))
                OutlinedTextField(crew, { crew = it }, Modifier.weight(1f), label = { Text("Crew / truck no.") }, singleLine = true, shape = RoundedCornerShape(14.dp))
            }
        }

        SectionCard(title = "Your review", subtitle = "Edit freely — this is your words, your control.") {
            OutlinedTextField(
                value = reviewText,
                onValueChange = { reviewText = it; edited = true },
                modifier = Modifier.fillMaxWidth().height(170.dp),
                shape = RoundedCornerShape(14.dp),
                placeholder = { Text("Your review will appear here…") }
            )
            Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("${reviewText.length} chars" + if (edited) " • edited by you" else " • auto-draft", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row {
                    TextButton(onClick = { regenerate() }) { Icon(Icons.Default.Refresh, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Rewrite") }
                    TextButton(onClick = { copy(reviewText) }) { Icon(Icons.Default.ContentCopy, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Copy") }
                }
            }
            FilledTonalButton(onClick = { regenerate() }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.AutoAwesome, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Generate my review")
            }
        }

        Button(
            onClick = { openGoogle() },
            modifier = Modifier.fillMaxWidth().height(58.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = reviewText.isNotBlank(),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Icon(Icons.Default.OpenInNew, null); Spacer(Modifier.width(8.dp))
            Text(if (stars == 0) "Continue to Google & post review" else "Continue to Google — post my $stars★ review", fontWeight = FontWeight.Bold)
        }
        Text("Copies your text • Opens Google Maps • YOU tap Post. We never auto-submit or bypass Google security.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 4.dp))

        OutlinedButton(onClick = { campaign?.let { vm.logEvent(EventTypes.CONFIRM, it.id) }; showSuccess = true }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp)) {
            Text("I posted on Google — Done")
        }

        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f))) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, null, tint = MaterialTheme.colorScheme.tertiary)
                Spacer(Modifier.width(8.dp))
                Text("Your privacy: no account, no tracking, no personal data stored. Only anonymous tap counts help us improve.", style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(8.dp))
    }

    if (showGoogleSheet) {
        AlertDialog(
            onDismissRequest = { showGoogleSheet = false },
            title = { Text("You're on Google now") },
            text = {
                Column {
                    Text("Your review text was copied. On the Google page:")
                    Spacer(Modifier.height(8.dp))
                    StepRow("1", "Pick the same stars", "Match the $stars★ you chose here.")
                    StepRow("2", "Long-press → Paste", "Paste your review into Google's box. Edit if you like.")
                    StepRow("3", "Tap Post", "Only you can publish. Come back and tap Done.")
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh).padding(10.dp)) {
                        Text(reviewText, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showGoogleSheet = false
                    campaign?.let { vm.logEvent(EventTypes.CONFIRM, it.id) }
                    showSuccess = true
                }) { Text("I posted — Done") }
            },
            dismissButton = { TextButton(onClick = { showGoogleSheet = false }) { Text("Back") } }
        )
    }
    if (showSuccess) {
        AlertDialog(
            onDismissRequest = { showSuccess = false },
            title = { Text("Thank you! \uD83D\uDE9A") },
            text = { Text("Your support moves our small transport family forward. Need help with a future shift? Call us anytime — and thanks for powering honest reviews.") },
            confirmButton = { Button(onClick = { showSuccess = false }) { Text("Done") } }
        )
    }
}

@Composable
private fun StepDot(n: String, label: String, active: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(30.dp).clip(androidx.compose.foundation.shape.CircleShape)
                .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) { Text(n, color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
