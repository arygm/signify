package com.arygm.quickfix.model.search

import com.arygm.quickfix.model.locations.Location
import com.google.firebase.Timestamp

data class AvailabilitySlot(val start: Timestamp, val end: Timestamp)

data class Announcement(
    val announcementId: String,
    val userId: String,
    val title: String,
    val category: String,
    val description: String,
    val location: Location?,
    val availability: List<AvailabilitySlot>,
    val quickFixImages: List<String>
)
