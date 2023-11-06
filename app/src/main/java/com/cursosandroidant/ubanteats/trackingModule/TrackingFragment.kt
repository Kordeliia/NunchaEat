package com.cursosandroidant.ubanteats.trackingModule

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.cursosandroidant.ubanteats.R
import com.cursosandroidant.ubanteats.common.Locations
import com.cursosandroidant.ubanteats.common.dataAccess.FakeDatabase
import com.cursosandroidant.ubanteats.common.utils.MapUtils
import com.cursosandroidant.ubanteats.common.utils.MapUtils.locationRequest
import com.cursosandroidant.ubanteats.databinding.FragmentTrackingBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.create
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/****
 * Project: Ubant Eats
 * From: com.cursosandroidant.ubanteats.trackingModule
 * Created by Alain Nicolás Tello on 12/10/22 at 19:16
 * All rights reserved 2022.
 *
 * All my Udemy Courses:
 * https://www.udemy.com/user/alain-nicolas-tello/
 * And Frogames formación:
 * https://cursos.frogamesformacion.com/pages/instructor-alain-nicolas
 *
 * Web: www.alainnicolastello.com
 ***/
class TrackingFragment : Fragment(), OnMapReadyCallback{
    
    private var _binding: FragmentTrackingBinding? = null
    private val binding get() = _binding!!
    private lateinit var map : GoogleMap
    private var locations = mutableListOf<LatLng>()
    private lateinit var fusedLocationClient : FusedLocationProviderClient
    private var locationCallback = object : LocationCallback(){
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            result.locations.run{
                this.forEach{
                    locations.add(LatLng(it.latitude, it.longitude))
                }
                MapUtils.addPolyline(map, locations)
                if(locations.isNotEmpty()){
                    calRealDistante(locations.last())
                    MapUtils.runDeliveryMap(requireActivity(), map, locations.last())
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        exitTransition = MaterialFadeThrough()
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentTrackingBinding.inflate(LayoutInflater.from(context))
        val mapFragment = requireActivity().supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButtons()
        setupDeliveryUserToUI()
    }
    private fun setupDeliveryUserToUI() {
        val user = FakeDatabase.getDeliveryUser()
        with(binding){
            tvRepartidor.setText(getString(R.string.tracking_name, user.name))
            Glide.with(this@TrackingFragment)
                .load(user.photoURL)
                .circleCrop()
                .into(imgPhoto)
        }
    }
    private fun setupButtons() {
        binding.btnFinish.setOnClickListener {
            NavHostFragment.findNavController(this).navigate(R.id.action_products_to_car)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
    private fun calRealDistante(location : LatLng){
        val distance = SphericalUtil.computeDistanceBetween(location, MapUtils.getDestinationDelivery())
        binding.tvRepDistancia.setText(getString(R.string.tracking_distance, MapUtils.formatDistance(distance)))
        binding.btnFinish.isEnabled = distance < 60
    }
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        //todo remove this
        map.isMyLocationEnabled = true
        MapUtils.setupMap(requireActivity(), map)
        //todo move this
        calRealDistante(MapUtils.getOriginDelivery())
    }
}