package me.sv.route.utils

import com.google.maps.model.LatLng

fun LatLng.copy(): LatLng{
    return LatLng(this.lat, this.lng)
}