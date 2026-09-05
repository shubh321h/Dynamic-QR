package com.agon.app.data

import kotlinx.serialization.Serializable

@Serializable
data class Campaign(
    val id: String,
    val name: String,
    val label: String,
    val dynamicDestination: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val colorIndex: Int = 0
)

@Serializable
data class AnalyticsEvent(
    val type: String,
    val campaignId: String,
    val timestamp: Long = System.currentTimeMillis()
)

object EventTypes {
    const val SCAN = "SCAN"
    const val START = "START"
    const val COPY = "COPY"
    const val OPEN_GOOGLE = "OPEN_GOOGLE"
    const val CONFIRM = "CONFIRM"
}

object Defaults {
    const val BUSINESS_NAME = "SwiftShift Packers & Movers"
    const val TAGLINE = "Safe • On-time • Insured shifting"
    const val MAPS_URL = "https://maps.app.goo.gl/pS6FZHudLgQkaoXh7"
    const val HEADLINE = "How was your move with us?"
    const val THANK_YOU = "Thank you for choosing SwiftShift! Your 20-second review helps other families move with confidence."
    const val CREW_HINT = "Crew / Truck no. (optional)"
    const val PIN = "1234"

    val SERVICE_TYPES = listOf(
        "Home Shifting",
        "Office Move",
        "Vehicle Transport",
        "Packing Only",
        "Storage"
    )
    val POSITIVES = listOf(
        "On-time pickup",
        "Careful packing",
        "Polite crew",
        "Safe delivery",
        "Transparent pricing",
        "Clean unloading"
    )
    val TONES = listOf("Warm & friendly", "Professional", "Short & crisp")

    fun defaultCampaigns(): List<Campaign> = listOf(
        Campaign(
            id = "SWFT-MAIN-01",
            name = "Main Counter QR",
            label = "Reception standee",
            dynamicDestination = MAPS_URL,
            isActive = true,
            createdAt = System.currentTimeMillis() - 21L * 86400000L,
            colorIndex = 0
        ),
        Campaign(
            id = "SWFT-TRUCK-02",
            name = "Truck Fleet QR",
            label = "Stickers inside trucks",
            dynamicDestination = MAPS_URL,
            isActive = true,
            createdAt = System.currentTimeMillis() - 14L * 86400000L,
            colorIndex = 1
        ),
        Campaign(
            id = "SWFT-BILL-03",
            name = "Bill / Invoice QR",
            label = "Printed on invoice",
            dynamicDestination = MAPS_URL,
            isActive = true,
            createdAt = System.currentTimeMillis() - 7L * 86400000L,
            colorIndex = 2
        )
    )

    fun payloadFor(campaignId: String): String = "https://swiftshift.review/r/$campaignId"

    fun extractMapsToken(url: String): String {
        val clean = url.trim().trimEnd('/')
        return if (clean.contains("/")) clean.substringAfterLast("/").take(32) else clean.take(32)
    }
}
