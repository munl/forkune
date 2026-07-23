package com.jian.forkune.datamodels

import kotlinx.serialization.Serializable

/**
 * Shared domain model for a nearby place. Reused by Home (last pick), Shuffling
 * (candidates) and Your-pick (result). Plain data class — no data-layer coupling
 * (no Room / ktor annotations). See clean-architecture §1.
 *
 * `accentColorArgb` + `initials` back the card's coloured accent block in the mocks.
 */
@Serializable
data class Restaurant(
    val name: String,
    val cuisine: String,
    val rating: Double,
    val reviewCount: Int,
    val priceLevel: Int,
    val distanceMi: Double,
    val isOpenNow: Boolean,
    val openUntil: String?,
    val accentColorArgb: Long,
    val initials: String,
)
