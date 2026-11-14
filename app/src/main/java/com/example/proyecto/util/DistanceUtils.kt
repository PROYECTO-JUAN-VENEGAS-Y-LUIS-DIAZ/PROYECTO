package com.example.proyecto.util

import com.example.proyecto.domain.model.GeoPoint
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object DistanceUtils {
    private const val EARTH_RADIUS_KM = 6371.0

    fun haversineKm(from: GeoPoint, to: GeoPoint): Double {
        val dLat = Math.toRadians(to.latitude - from.latitude)
        val dLon = Math.toRadians(to.longitude - from.longitude)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(from.latitude)) * cos(Math.toRadians(to.latitude)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }

    fun formatKm(km: Double): String = String.format(Locale.getDefault(), "%.1f km", km)
}