package com.example.viewsdteti.ui.location

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.viewsdteti.databinding.FragmentLocationBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.Locale

class LocationFragment : Fragment() {

    private var _binding: FragmentLocationBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST = 1001

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationBinding.inflate(inflater, container, false)
        val root: View = binding.root

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setupButtons()
        return root
    }

    private fun setupButtons() {
        binding.buttonGetLocation.setOnClickListener {
            getCurrentLocation()
        }

        binding.buttonGeocoding.setOnClickListener {
            val address = binding.editTextAddress.text.toString()
            if (address.isNotEmpty()) {
                performGeocoding(address)
            } else {
                Toast.makeText(context, "Please enter an address", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonReverseGeocoding.setOnClickListener {
            val lat = binding.editTextLatitude.text.toString().toDoubleOrNull()
            val lon = binding.editTextLongitude.text.toString().toDoubleOrNull()
            
            if (lat != null && lon != null) {
                performReverseGeocoding(lat, lon)
            } else {
                Toast.makeText(context, "Please enter valid coordinates", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCurrentLocation() {
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
                    binding.textLocationResult.text = "Current Location:\nLatitude: ${it.latitude}\nLongitude: ${it.longitude}"
//                    performReverseGeocoding(it.latitude, it.longitude)
                } ?: run {
                    binding.textLocationResult.text = "Location not available"
                }
            }
            .addOnFailureListener {
                binding.textLocationResult.text = "Error getting location: ${it.message}"
            }
    }

    private fun performGeocoding(address: String) {
        try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addresses: List<Address>? = geocoder.getFromLocationName(address, 1)
            
            if (!addresses.isNullOrEmpty()) {
                val location = addresses[0]
                binding.textLocationResult.text = "Geocoding Result:\nLatitude: ${location.latitude}\nLongitude: ${location.longitude}"
            } else {
                binding.textLocationResult.text = "No location found for the address"
            }
        } catch (e: Exception) {
            binding.textLocationResult.text = "Geocoding error: ${e.message}"
        }
    }

    private fun performReverseGeocoding(latitude: Double, longitude: Double) {
        try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val addressText = StringBuilder()
                for (i in 0..address.maxAddressLineIndex) {
                    addressText.append(address.getAddressLine(i))
                    if (i < address.maxAddressLineIndex) addressText.append(", ")
                }
                binding.textLocationResult.text = "Reverse Geocoding Result:\n$addressText"
            } else {
                binding.textLocationResult.text = "No address found for the coordinates"
            }
        } catch (e: Exception) {
            binding.textLocationResult.text = "Reverse geocoding error: ${e.message}"
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation()
                } else {
                    Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 