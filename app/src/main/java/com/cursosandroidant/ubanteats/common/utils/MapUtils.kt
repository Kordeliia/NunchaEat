package com.cursosandroidant.ubanteats.common.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.cursosandroidant.ubanteats.R
import com.cursosandroidant.ubanteats.common.Locations
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CustomCap
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import java.lang.Exception

object MapUtils {
    private var iconDeliveryMarker : Bitmap? = null
    private var iconDestinationMarker : Bitmap? = null
    private var deliverMarker: Marker? = null

    val locationRequest = LocationRequest.create()
        .setInterval(5_000)
        .setFastestInterval(2_000)
    //destino pedido
    fun getDestinationDelivery(): LatLng = Locations.murciaCasa
    //direccion restaurante
    fun getOriginDelivery(): LatLng = Locations.murciaRestaurante
    fun setupMap(context: Context, map: GoogleMap){
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(getDestinationDelivery(), 16.5f))
        map.uiSettings.apply{
            isMyLocationButtonEnabled = false
            isZoomControlsEnabled = false
            isRotateGesturesEnabled = false
            isTiltGesturesEnabled = false
            isMapToolbarEnabled = false
        }
        setupMapStyle(context, map)
        addDeliveryMarker(context, map, getOriginDelivery())
        addDestinationMarker(map, getDestinationDelivery())
    }

    private fun setupMapStyle(context: Context, map: GoogleMap) {
        try{
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style))
        } catch (e : Exception){
            e.printStackTrace()
        }
        //todo remove this
        setupMarkersData(context)

    }
    private fun setupMarkersData(context: Context){
        Utils.getBitmapFromVector(context, R.drawable.ic_delivery)?.let{
            iconDeliveryMarker = it
        }
        Utils.getBitmapFromVector(context, R.drawable.ic_goal)?.let{
            iconDestinationMarker = it
        }
    }
    private fun addDeliveryMarker(context: Context ,map: GoogleMap, location: LatLng){
        iconDeliveryMarker?.let{
            deliverMarker = map.addMarker(MarkerOptions()
                .position(location)
                .icon(BitmapDescriptorFactory.fromBitmap(it))
                .anchor(0.5f, 0.5f)
                .title("El pedido de Five Guys"))
        }
    }
    private fun removeOldDeliveryMarker() = deliverMarker?.remove()
    private fun addDestinationMarker(map: GoogleMap, location: LatLng){
        iconDestinationMarker?.let{
            map.addMarker(MarkerOptions().position(location)
                .anchor(0.3f, 1f)
                .icon(BitmapDescriptorFactory.fromBitmap(it)))
        }
    }

    fun formatDistance(rawDistance: Double): String {
        var distance = rawDistance.toInt()
        val unit = if(distance> 1_000){
            distance/1000
            "km"
        } else{
            "m"
        }
        return String.format("%d%s", distance, unit)
    }

    fun addPolyline(map: GoogleMap, locations: MutableList<LatLng>) {
        map.addPolyline(PolylineOptions()
            .width(16f)
            .color(Color.CYAN)
            .jointType(JointType.ROUND)
            .startCap(RoundCap())
            .addAll(locations))

    }
    fun runDeliveryMap(context : Context, map : GoogleMap, location: LatLng){
        removeOldDeliveryMarker()
        addDeliveryMarker(context, map, location)
        val builder = LatLngBounds.Builder()
            .include(getDestinationDelivery())
            .include(location)
        val padding = 256
        val distanceBounds : LatLngBounds = builder.build()
        map.setOnMapLoadedCallback { {
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(distanceBounds, padding))
        } }
    }
}