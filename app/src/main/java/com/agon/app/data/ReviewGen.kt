package com.agon.app.data

object ReviewGen {
    fun build(
        name: String,
        stars: Int,
        service: String,
        positives: List<String>,
        tone: String,
        crew: String,
        city: String
    ): String {
        val who = name.trim().ifBlank { "" }
        val crewPart = crew.trim().let { if (it.isBlank()) "" else " Special thanks to $it and the crew" }
        val cityPart = city.trim().let { if (it.isBlank()) "" else " here in $it" }
        val pos = positives.take(4)
        val posSentence = when {
            pos.isEmpty() -> ""
            pos.size == 1 -> pos[0].lowercase()
            pos.size == 2 -> "${pos[0].lowercase()} and ${pos[1].lowercase()}"
            else -> pos.dropLast(1).joinToString(", ") { it.lowercase() } + " and ${pos.last().lowercase()}"
        }
        return when (tone) {
            "Short & crisp" -> short(stars, service, posSentence, crewPart, cityPart)
            "Professional" -> professional(stars, service, posSentence, crewPart, cityPart)
            else -> warm(stars, service, posSentence, crewPart, cityPart)
        }
    }

    private fun warm(stars: Int, service: String, pos: String, crew: String, city: String): String {
        val open = when {
            stars >= 5 -> "Absolutely wonderful experience with this team for our ${service.lowercase()}$city!"
            stars == 4 -> "Really happy with our ${service.lowercase()}$city — smooth from start to finish."
            stars == 3 -> "Decent ${service.lowercase()} experience$city with a few good moments."
            stars == 2 -> "Our ${service.lowercase()}$city had some issues, though the team tried to help."
            else -> "Our ${service.lowercase()}$city did not go as expected."
        }
        val mid = if (pos.isBlank()) "Packing was neat, handling was careful and everything reached safely."
        else "I especially liked the $pos — packing was neat and everything reached safely."
        val close = when {
            stars >= 4 -> "$crew. Highly recommended for anyone planning a stress-free move!"
            stars == 3 -> "$crew. With a little tightening up, this could easily be 5 stars."
            else -> "$crew. Hope the team looks into the gaps for future moves."
        }
        return "$open $mid$close".replace("  ", " ").trim()
    }

    private fun professional(stars: Int, service: String, pos: String, crew: String, city: String): String {
        val open = when {
            stars >= 5 -> "Used their ${service.lowercase()} service$city. Excellent end-to-end execution."
            stars == 4 -> "Used their ${service.lowercase()} service$city. Well-managed move overall."
            stars == 3 -> "Used their ${service.lowercase()} service$city. Satisfactory with scope for improvement."
            else -> "Used their ${service.lowercase()} service$city. Below expectations on this occasion."
        }
        val mid = if (pos.isBlank()) "Packing quality, inventory handling and timelines were handled systematically."
        else "Notable strengths: $pos. Packing quality and timelines were handled systematically."
        val close = when {
            stars >= 4 -> "$crew. Would recommend for domestic and office relocations."
            stars == 3 -> "$crew. Recommended with minor reservations."
            else -> "$crew. Expecting better consistency next time."
        }
        return "$open $mid$close".replace("  ", " ").trim()
    }

    // short() keeps same signature order-safe
    private fun short(stars: Int, service: String, pos: String, crew: String, city: String): String {
        val core = when {
            stars >= 5 -> "Superb ${service.lowercase()}$city! Neat packing, on time, zero damage."
            stars == 4 -> "Great ${service.lowercase()}$city. On time and careful."
            stars == 3 -> "Okay ${service.lowercase()}$city. Good crew, minor delays."
            stars == 2 -> "${service} done$city, but packing/delays need work."
            else -> "${service} was disappointing$city. Needs improvement."
        }
        val extra = if (pos.isBlank()) "" else " Loved the $pos."
        val thanks = if (crew.isBlank()) " Highly recommended!" else "$crew. Highly recommended!"
        val tail = if (stars >= 4) thanks else if (stars == 3) " Overall fine." else " Hope they improve."
        return (core + extra + tail).replace("  ", " ").trim()
    }

    fun starLabel(stars: Int): String = when (stars) {
        5 -> "Excellent — love it!"
        4 -> "Good — happy"
        3 -> "Average — okay"
        2 -> "Poor — issues"
        1 -> "Very poor"
        else -> "Tap a star"
    }

    fun needsAttention(stars: Int): Boolean = stars in 1..3
}
