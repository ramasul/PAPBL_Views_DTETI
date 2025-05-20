package com.example.viewsdteti.ui.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.viewsdteti.BuildConfig
import com.example.viewsdteti.R
import com.example.viewsdteti.databinding.FragmentDashboardBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.Polygon
import org.maplibre.android.annotations.PolygonOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraPosition.Builder
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val mapTilerApiKey = BuildConfig.MAP_TILER_API
    private val styleUrl = "https://api.maptiler.com/maps/basic-v2/style.json?key=${mapTilerApiKey}"
    
    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST = 1001
    
    // UGM coordinates
    private val ugmLocation = LatLng(-7.770717, 110.377724)
    
    // Store markers and shapes for cleanup
    private val markers = mutableListOf<Marker>()
    private val shapes = mutableListOf<Polygon>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        MapLibre.getInstance(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root = binding.root

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        setupMap()
        setupButtons()

        return root
    }

    private fun setupMap() {
        mapView.getMapAsync { map ->
            map.setStyle(styleUrl) { style ->
                // Set initial camera position to UGM
                map.cameraPosition = Builder()
                    .target(ugmLocation)
                    .zoom(15.0)
                    .build()

                // This Features is Somehow Unstable
                map.uiSettings.apply {
                    isRotateGesturesEnabled = false
                    isCompassEnabled = false
                    isTiltGesturesEnabled = false
                }

                // Add UGM marker
                addUGMMarker(style)
                
                // Setup map click listener for info windows
                map.addOnMapClickListener { point ->
                    showInfoWindow(point, map)
                    true
                }

                // Add camera movement listener to hide info windows
                map.addOnCameraMoveListener {
                    markers.forEach { marker ->
                        marker.hideInfoWindow()
                    }
                }
            }
        }
    }

    private fun setupButtons() {
        binding.buttonAddMarker.setOnClickListener {
            mapView.getMapAsync { map ->
                addCustomMarker(map)
            }
        }

        binding.buttonShowLocation.setOnClickListener {
            showCurrentLocation()
        }

        binding.buttonDrawShape.setOnClickListener {
            mapView.getMapAsync { map ->
                drawShape(map)
            }
        }
    }

    private fun addUGMMarker(style: Style) {
        // Simple marker at UGM
        val markerOptions = MarkerOptions()
            .setPosition(ugmLocation)
            .setTitle("Universitas Gadjah Mada")
            .setSnippet("Yogyakarta, Indonesia")
        
        mapView.getMapAsync { map ->
            val marker = map.addMarker(markerOptions)
            markers.add(marker)
        }
    }

    private fun addCustomMarker(map: org.maplibre.android.maps.MapLibreMap) {
        // Custom marker slightly offset from UGM
        val customLocation = LatLng(ugmLocation.latitude + 0.002, ugmLocation.longitude + 0.002)
        val markerOptions = MarkerOptions()
            .setPosition(customLocation)
            .setTitle("Custom Marker")
            .setSnippet("This is a custom marker")
        
        val marker = map.addMarker(markerOptions)
        markers.add(marker)
    }

    private fun showCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
            return
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                location?.let {
                    val currentLocation = LatLng(it.latitude, it.longitude)
                    mapView.getMapAsync { map ->
                        // Add marker at current location
                        val markerOptions = MarkerOptions()
                            .setPosition(currentLocation)
                            .setTitle("Current Location")
                            .setSnippet("You are here")
                        
                        val marker = map.addMarker(markerOptions)
                        markers.add(marker)
                        
                        // Move camera to current location
                        map.cameraPosition = Builder()
                            .target(currentLocation)
                            .zoom(15.0)
                            .build()
                    }
                }
            }
    }

    private fun showInfoWindow(point: LatLng, map: org.maplibre.android.maps.MapLibreMap) {
        // Clear previous markers except UGM marker
        markers.forEach { marker ->
            if (marker.position != ugmLocation) {
                map.removeMarker(marker)
            }
        }
        markers.clear()
        // Re-add UGM marker
        val ugmMarker = map.addMarker(
            MarkerOptions()
                .setPosition(ugmLocation)
                .setTitle("Universitas Gadjah Mada")
                .setSnippet("Yogyakarta, Indonesia")
        )
        markers.add(ugmMarker)

        // Add new marker at clicked location
        val markerOptions = MarkerOptions()
            .setPosition(point)
            .setTitle("Dropped Pin")
            .setSnippet("Lat: ${point.latitude}, Long: ${point.longitude}")
        
        val marker = map.addMarker(markerOptions)
        markers.add(marker)
        marker.showInfoWindow(map, mapView)
    }

    private fun drawShape(map: org.maplibre.android.maps.MapLibreMap) {
        // Draw a simple triangle around UGM
        val points = listOf(
            ugmLocation,
            LatLng(ugmLocation.latitude + 0.002, ugmLocation.longitude),
            LatLng(ugmLocation.latitude, ugmLocation.longitude + 0.002)
        )

        val polygonOptions = PolygonOptions()
            .addAll(points)
            .fillColor(Color.argb(128, 255, 0, 0)) // Semi-transparent red
            .strokeColor(Color.RED)

        val polygon = map.addPolygon(polygonOptions)
        shapes.add(polygon)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showCurrentLocation()
                } else {
                    Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // MapView lifecycle methods
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up markers and shapes
        mapView.getMapAsync { map ->
            markers.forEach { map.removeMarker(it) }
            shapes.forEach { map.removePolygon(it) }
        }
        mapView.onDestroy()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}