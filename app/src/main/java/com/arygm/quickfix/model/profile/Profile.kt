package com.arygm.quickfix.model.profile

import com.arygm.quickfix.model.locations.Location
import com.arygm.quickfix.model.profile.dataFields.AddOnService
import com.arygm.quickfix.model.profile.dataFields.IncludedService
import com.arygm.quickfix.model.profile.dataFields.Review
import java.time.LocalDate
import java.time.LocalTime

open class Profile(
    val uid: String,
    var quickFixes: List<String> = emptyList(), // common field
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Profile) return false

    return uid == other.uid
  }

  override fun hashCode(): Int {
    return uid.hashCode()
  }
}

class UserProfile(
    // String of uid that will represents the uid of the saved lists
    val locations: List<Location>,
    val announcements: List<String>, // Each string correspond to an announcement id.
    val wallet: Double = 0.0,
    uid: String,
    quickFixes: List<String> =
        emptyList(), // String of uid that will represents the uid of the QuickFixes
    val savedList: List<String> = emptyList(),
) : Profile(uid, quickFixes) {
  // quickFixes: List<String>, // String of uid that will represents the uid of the QuickFixes
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is UserProfile) return false
    if (!super.equals(other)) return false

    return locations == other.locations &&
        announcements == other.announcements &&
        wallet == other.wallet &&
        savedList == other.savedList
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + locations.hashCode()
    result = 31 * result + announcements.hashCode()
    result = 31 * result + wallet.hashCode()
    result = 31 * result + savedList.hashCode()
    return result
  }

  fun copy(
      locations: List<Location> = this.locations,
      announcements: List<String> = this.announcements,
      wallet: Double = this.wallet,
      uid: String = this.uid,
      quickFixes: List<String> = this.quickFixes,
      savedList: List<String> = this.savedList
  ): UserProfile {
    return UserProfile(
        locations = locations,
        announcements = announcements,
        wallet = wallet,
        uid = uid,
        quickFixes = quickFixes,
        savedList = savedList)
  }
}

class WorkerProfile(
    val fieldOfWork: String = "General Work", // Default generic field of work
    val description: String = "No description available", // Default description
    val location: Location? =
        Location(0.0, 0.0, "Unknown Location"), // Default to an empty Location
    quickFixes: List<String> = emptyList(),
    val includedServices: List<IncludedService> =
        listOf(
            IncludedService(name = "Basic Consultation"),
            IncludedService(name = "Service Inspection")), // Default services
    val addOnServices: List<AddOnService> =
        listOf(
            AddOnService(name = "Express Delivery"),
            AddOnService(name = "Premium Materials")), // Default add-on services
    val reviews: ArrayDeque<Review> = ArrayDeque(emptyList()), // Default reviews
    val profilePicture: String = "", // Default profile picture URL
    val bannerPicture: String =
        "https://example.com/default-banner-pic.jpg", // Default banner picture URL
    val price: Double = 0.0, // Default price
    val displayName: String = "", // Default display name
    val unavailability_list: List<LocalDate> =
        listOf(
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(3)), // Default unavailability dates
    val workingHours: Pair<LocalTime, LocalTime> =
        Pair(LocalTime.of(9, 0), LocalTime.of(17, 0)), // Default working hours (9 AM to 5 PM)
    uid: String = "", // Default UID
    val tags: List<String> = listOf("Reliable", "Experienced", "Professional"), // Default tags
    val rating: Double = reviews.map { it.rating }.average(),
    val wallet: Double = 0.0,
) : Profile(uid, quickFixes) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is WorkerProfile) return false
    if (!super.equals(other)) return false

    return fieldOfWork == other.fieldOfWork &&
        description == other.description &&
        location == other.location &&
        includedServices == other.includedServices &&
        addOnServices == other.addOnServices &&
        reviews == other.reviews &&
        profilePicture == other.profilePicture &&
        bannerPicture == other.bannerPicture &&
        price == other.price &&
        displayName == other.displayName &&
        unavailability_list == other.unavailability_list &&
        workingHours == other.workingHours &&
        tags == other.tags &&
        rating == other.rating
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + fieldOfWork.hashCode()
    result = 31 * result + description.hashCode()
    result = 31 * result + (location?.hashCode() ?: 0)
    result = 31 * result + includedServices.hashCode()
    result = 31 * result + addOnServices.hashCode()
    result = 31 * result + reviews.hashCode()
    result = 31 * result + profilePicture.hashCode()
    result = 31 * result + bannerPicture.hashCode()
    result = 31 * result + price.hashCode()
    result = 31 * result + displayName.hashCode()
    result = 31 * result + unavailability_list.hashCode()
    result = 31 * result + workingHours.hashCode()
    result = 31 * result + tags.hashCode()
    result = 31 * result + rating.hashCode()
    return result
  }

  fun toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "uid" to this.uid,
        "description" to this.description,
        "fieldOfWork" to this.fieldOfWork,
        "location" to this.location?.toFirestoreMap(),
        "price" to this.price,
        "display_name" to this.displayName,
        "included_services" to this.includedServices.map { it.toFirestoreMap() },
        "addOnServices" to this.addOnServices.map { it.toFirestoreMap() },
        "workingHours" to
            mapOf(
                "start" to this.workingHours.first.toString(), // Convert LocalTime to String
                "end" to this.workingHours.second.toString()),
        "unavailability_list" to
            this.unavailability_list.map { it.toString() }, // Convert LocalDate to String
        "reviews" to this.reviews.map { it.toFirestoreMap() }.toList(),
        "tags" to this.tags,
        "profileImageUrl" to this.profilePicture,
        "bannerImageUrl" to this.bannerPicture,
        "quickFixes" to this.quickFixes)
  }
}
