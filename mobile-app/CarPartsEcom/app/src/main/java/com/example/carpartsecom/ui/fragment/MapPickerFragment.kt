package com.example.carpartsecom.ui.fragment

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.carpartsecom.MainActivity
import com.example.carpartsecom.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * Fragment for picking a location on OpenStreetMap
 * Usage: Pass a callback to receive the selected location
 */
class MapPickerFragment : Fragment() {

    private var mapView: MapView? = null
    private var selectedMarker: Marker? = null
    private var selectedLocation: GeoPoint? = null

    // Callback to return selected location
    var onLocationSelected: ((lat: Double, lng: Double) -> Unit)? = null

    // Default location (New York)
    private var initialLat = 40.7128
    private var initialLng = -74.0060

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                // Permission granted - could center on user location
            }
        }
    }

    companion object {
        fun newInstance(
            initialLat: Double? = null,
            initialLng: Double? = null,
            onLocationSelected: (lat: Double, lng: Double) -> Unit
        ): MapPickerFragment {
            return MapPickerFragment().apply {
                this.onLocationSelected = onLocationSelected
                initialLat?.let { this.initialLat = it }
                initialLng?.let { this.initialLng = it }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure osmdroid
        Configuration.getInstance().load(
            requireContext(),
            requireContext().getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = requireContext().packageName
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = view.findViewById(R.id.mapView)
        val confirmButton = view.findViewById<MaterialButton>(R.id.confirmLocationButton)
        val cancelButton = view.findViewById<MaterialButton>(R.id.cancelButton)
        val coordinatesText = view.findViewById<TextView>(R.id.coordinatesText)
        val myLocationFab = view.findViewById<FloatingActionButton>(R.id.myLocationFab)

        setupMap()

        // Request location permission
        requestLocationPermission()

        // Update coordinates display when location changes
        updateCoordinatesText(coordinatesText, initialLat, initialLng)

        // Set initial marker
        setMarkerAtLocation(GeoPoint(initialLat, initialLng))

        // Map click listener to place marker
        mapView?.overlays?.add(object : org.osmdroid.views.overlay.Overlay() {
            override fun onSingleTapConfirmed(e: android.view.MotionEvent?, mapView: MapView?): Boolean {
                val projection = mapView?.projection
                val geoPoint = projection?.fromPixels(e?.x?.toInt() ?: 0, e?.y?.toInt() ?: 0) as? GeoPoint
                geoPoint?.let {
                    setMarkerAtLocation(it)
                    updateCoordinatesText(coordinatesText, it.latitude, it.longitude)
                }
                return true
            }
        })

        // My location button
        myLocationFab.setOnClickListener {
            // Center on default location (or user location if available)
            mapView?.controller?.animateTo(GeoPoint(initialLat, initialLng))
        }

        // Confirm button
        confirmButton.setOnClickListener {
            selectedLocation?.let { location ->
                onLocationSelected?.invoke(location.latitude, location.longitude)
            }
            requireActivity().onBackPressed()
        }

        // Cancel button
        cancelButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setupMap() {
        mapView?.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
            controller.setCenter(GeoPoint(initialLat, initialLng))
        }
    }

    private fun setMarkerAtLocation(geoPoint: GeoPoint) {
        mapView?.let { map ->
            // Remove existing marker
            selectedMarker?.let { map.overlays.remove(it) }

            // Create new marker
            selectedMarker = Marker(map).apply {
                position = geoPoint
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Delivery Location"
            }

            map.overlays.add(selectedMarker)
            map.invalidate()

            selectedLocation = geoPoint
        }
    }

    private fun updateCoordinatesText(textView: TextView?, lat: Double, lng: Double) {
        textView?.text = String.format("%.6f, %.6f", lat, lng)
    }

    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
            }
            else -> {
                locationPermissionRequest.launch(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView?.onDetach()
        mapView = null
    }
}

