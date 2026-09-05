package com.agon.app.data

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.util.UUID

private val Context.appDataStore by preferencesDataStore(name = "swiftshift_prefs")

private object Keys {
    val BUSINESS = stringPreferencesKey("business_name")
    val TAGLINE = stringPreferencesKey("tagline")
    val MAPS_URL = stringPreferencesKey("maps_url")
    val HEADLINE = stringPreferencesKey("headline")
    val THANK = stringPreferencesKey("thank_you")
    val PIN = stringPreferencesKey("admin_pin")
    val CAMPAIGNS = stringPreferencesKey("campaigns_json")
    val EVENTS = stringPreferencesKey("events_json")
    val SEEDED = stringPreferencesKey("seeded_v1")
}

private val jsonLenient = Json { ignoreUnknownKeys = true }

data class UiState(
    val businessName: String = Defaults.BUSINESS_NAME,
    val tagline: String = Defaults.TAGLINE,
    val mapsUrl: String = Defaults.MAPS_URL,
    val headline: String = Defaults.HEADLINE,
    val thankYou: String = Defaults.THANK_YOU,
    val pin: String = Defaults.PIN,
    val campaigns: List<Campaign> = emptyList(),
    val events: List<AnalyticsEvent> = emptyList(),
    val loaded: Boolean = false
)

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val ctx: Context get() = getApplication()
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    init {
        viewModelScope.launch {
            val p = ctx.appDataStore.data.first()
            var campaigns: List<Campaign> = try {
                val raw = p[Keys.CAMPAIGNS]
                if (raw.isNullOrBlank()) Defaults.defaultCampaigns()
                else jsonLenient.decodeFromString(ListSerializer(Campaign.serializer()), raw)
            } catch (_: Exception) { Defaults.defaultCampaigns() }
            var events: List<AnalyticsEvent> = try {
                val raw = p[Keys.EVENTS]
                if (raw.isNullOrBlank()) emptyList()
                else jsonLenient.decodeFromString(ListSerializer(AnalyticsEvent.serializer()), raw)
            } catch (_: Exception) { emptyList() }
            if (campaigns.isEmpty()) campaigns = Defaults.defaultCampaigns()
            _state.value = UiState(
                businessName = p[Keys.BUSINESS] ?: Defaults.BUSINESS_NAME,
                tagline = p[Keys.TAGLINE] ?: Defaults.TAGLINE,
                mapsUrl = p[Keys.MAPS_URL] ?: Defaults.MAPS_URL,
                headline = p[Keys.HEADLINE] ?: Defaults.HEADLINE,
                thankYou = p[Keys.THANK] ?: Defaults.THANK_YOU,
                pin = p[Keys.PIN] ?: Defaults.PIN,
                campaigns = campaigns,
                events = events,
                loaded = true
            )
            persistCampaigns(campaigns)
            persistEvents(events)
        }
    }

    private suspend fun persistCampaigns(list: List<Campaign>) {
        val raw = jsonLenient.encodeToString(ListSerializer(Campaign.serializer()), list)
        ctx.appDataStore.edit { it[Keys.CAMPAIGNS] = raw }
    }
    private suspend fun persistEvents(list: List<AnalyticsEvent>) {
        val capped = list.takeLast(800)
        val raw = jsonLenient.encodeToString(ListSerializer(AnalyticsEvent.serializer()), capped)
        ctx.appDataStore.edit { it[Keys.EVENTS] = raw }
    }

    fun saveBusiness(name: String, tagline: String, mapsUrl: String, headline: String, thankYou: String) {
        _state.value = _state.value.copy(
            businessName = name.ifBlank { Defaults.BUSINESS_NAME },
            tagline = tagline, headline = headline, thankYou = thankYou,
            mapsUrl = mapsUrl.ifBlank { Defaults.MAPS_URL }
        )
        viewModelScope.launch {
            ctx.appDataStore.edit {
                it[Keys.BUSINESS] = _state.value.businessName
                it[Keys.TAGLINE] = _state.value.tagline
                it[Keys.MAPS_URL] = _state.value.mapsUrl
                it[Keys.HEADLINE] = _state.value.headline
                it[Keys.THANK] = _state.value.thankYou
            }
        }
    }

    fun savePin(newPin: String) {
        _state.value = _state.value.copy(pin = newPin)
        viewModelScope.launch { ctx.appDataStore.edit { it[Keys.PIN] = newPin } }
    }

    fun addCampaign(name: String, label: String, destination: String) {
        val c = Campaign(
            id = "SWFT-" + UUID.randomUUID().toString().take(4).uppercase() + "-" + (100..999).random(),
            name = name.ifBlank { "New QR Campaign" },
            label = label.ifBlank { "General use" },
            dynamicDestination = destination.ifBlank { _state.value.mapsUrl },
            isActive = true,
            createdAt = System.currentTimeMillis(),
            colorIndex = (_state.value.campaigns.size % 4)
        )
        val updated = _state.value.campaigns + c
        _state.value = _state.value.copy(campaigns = updated)
        viewModelScope.launch { persistCampaigns(updated) }
    }

    fun updateCampaign(c: Campaign) {
        val updated = _state.value.campaigns.map { if (it.id == c.id) c else it }
        _state.value = _state.value.copy(campaigns = updated)
        viewModelScope.launch { persistCampaigns(updated) }
    }

    fun deleteCampaign(id: String) {
        val updated = _state.value.campaigns.filterNot { it.id == id }
        _state.value = _state.value.copy(campaigns = updated)
        viewModelScope.launch { persistCampaigns(updated) }
    }

    fun updateDestination(id: String, newDest: String) {
        updateCampaign(_state.value.campaigns.first { it.id == id }.copy(dynamicDestination = newDest))
    }

    fun logEvent(type: String, campaignId: String) {
        val e = AnalyticsEvent(type = type, campaignId = campaignId)
        val updated = (_state.value.events + e).takeLast(800)
        _state.value = _state.value.copy(events = updated)
        viewModelScope.launch { persistEvents(updated) }
    }

    fun clearAnalytics() {
        _state.value = _state.value.copy(events = emptyList())
        viewModelScope.launch { persistEvents(emptyList()) }
    }

    fun loadDemoData() {
        val camps = _state.value.campaigns.ifEmpty { Defaults.defaultCampaigns() }
        val now = System.currentTimeMillis()
        val list = mutableListOf<AnalyticsEvent>()
        val rnd = java.util.Random(42)
        for (day in 13 downTo 0) {
            val base = 4 + rnd.nextInt(10)
            repeat(base) {
                val camp = camps[rnd.nextInt(camps.size)]
                val ts = now - day * 86400000L - rnd.nextInt(86000000).toLong()
                list.add(AnalyticsEvent(EventTypes.SCAN, camp.id, ts))
                if (rnd.nextDouble() < 0.82) list.add(AnalyticsEvent(EventTypes.START, camp.id, ts + 40000))
                if (rnd.nextDouble() < 0.64) list.add(AnalyticsEvent(EventTypes.COPY, camp.id, ts + 120000))
                if (rnd.nextDouble() < 0.58) list.add(AnalyticsEvent(EventTypes.OPEN_GOOGLE, camp.id, ts + 180000))
                if (rnd.nextDouble() < 0.41) list.add(AnalyticsEvent(EventTypes.CONFIRM, camp.id, ts + 600000))
            }
        }
        val sorted = (list + _state.value.events).sortedBy { it.timestamp }.takeLast(800)
        _state.value = _state.value.copy(events = sorted, campaigns = camps)
        viewModelScope.launch {
            persistEvents(sorted)
            persistCampaigns(camps)
        }
    }

    fun resetAll() {
        val camps = Defaults.defaultCampaigns()
        _state.value = UiState(
            businessName = Defaults.BUSINESS_NAME,
            tagline = Defaults.TAGLINE,
            mapsUrl = Defaults.MAPS_URL,
            headline = Defaults.HEADLINE,
            thankYou = Defaults.THANK_YOU,
            pin = Defaults.PIN,
            campaigns = camps,
            events = emptyList(),
            loaded = true
        )
        viewModelScope.launch {
            ctx.appDataStore.edit { p ->
                p[Keys.BUSINESS] = Defaults.BUSINESS_NAME
                p[Keys.TAGLINE] = Defaults.TAGLINE
                p[Keys.MAPS_URL] = Defaults.MAPS_URL
                p[Keys.HEADLINE] = Defaults.HEADLINE
                p[Keys.THANK] = Defaults.THANK_YOU
                p[Keys.PIN] = Defaults.PIN
            }
            persistCampaigns(camps)
            persistEvents(emptyList())
        }
    }
}

// ---------- analytics helpers (pure) ----------
data class Funnel(val scans: Int, val starts: Int, val copies: Int, val opens: Int, val confirms: Int)

fun funnelOf(events: List<AnalyticsEvent>): Funnel = Funnel(
    scans = events.count { it.type == EventTypes.SCAN },
    starts = events.count { it.type == EventTypes.START },
    copies = events.count { it.type == EventTypes.COPY },
    opens = events.count { it.type == EventTypes.OPEN_GOOGLE },
    confirms = events.count { it.type == EventTypes.CONFIRM }
)

fun perCampaign(events: List<AnalyticsEvent>): Map<String, Int> =
    events.filter { it.type == EventTypes.SCAN }.groupingBy { it.campaignId }.eachCount()

fun last14Days(events: List<AnalyticsEvent>): List<Pair<String, Int>> {
    val cal = java.util.Calendar.getInstance()
    val out = mutableListOf<Pair<String, Int>>()
    val fmt = java.text.SimpleDateFormat("dd MMM", java.util.Locale.ENGLISH)
    for (i in 13 downTo 0) {
        cal.timeInMillis = System.currentTimeMillis() - i * 86400000L
        val label = fmt.format(cal.time)
        val dayStart = java.util.Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis() - i * 86400000L
            set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        val dayEnd = dayStart + 86400000L
        val count = events.count { it.type == EventTypes.SCAN && it.timestamp in dayStart until dayEnd }
        out.add(label to count)
    }
    return out
}
