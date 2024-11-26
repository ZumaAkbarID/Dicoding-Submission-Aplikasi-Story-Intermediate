package com.rwa.tellme.view.maps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.PorterDuff
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.rwa.tellme.R
import com.rwa.tellme.data.model.StoryModel
import com.rwa.tellme.databinding.ActivityMapsBinding
import com.rwa.tellme.utils.showAlertDialog
import com.rwa.tellme.utils.showToastMessage
import com.rwa.tellme.view.StoryViewModelFactory

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var storyViewModel: MapsViewModel
    private val boundsBuilder = LatLngBounds.Builder()
    private lateinit var progressBar: ProgressBar
    private var listStory: List<StoryModel> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val storyFactory = StoryViewModelFactory.getInstance(this)
        storyViewModel = ViewModelProvider(this, storyFactory)[MapsViewModel::class.java]

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.topMapsBar.overflowIcon?.setColorFilter(resources.getColor(R.color.white), PorterDuff.Mode.SRC_ATOP)
        handleActionMenu()

        setupProgressBar()

        storyViewModel.showAllStoryWithLocation()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        mMap.setOnPoiClickListener { pointOfInterest ->
            val poiMarker = mMap.addMarker(
                MarkerOptions()
                    .position(pointOfInterest.latLng)
                    .title(pointOfInterest.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
            )
            poiMarker?.showInfoWindow()
        }

        getMyLocation()
        setMapStyle()
        observeViewModel()
    }

    private fun showMarker() {
        if(listStory.isNotEmpty()) {
            listStory.forEach { story ->
                val latLng = LatLng(story.lat, story.lon)
                mMap.addMarker(MarkerOptions().position(latLng).title(story.name).snippet(story.description))
                boundsBuilder.include(latLng)
            }

            val bounds: LatLngBounds = boundsBuilder.build()
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                    bounds,
                    resources.displayMetrics.widthPixels,
                    resources.displayMetrics.heightPixels,
                    300
                )
            )
        } else {
            showToastMessage(this, "Data Kosong")
        }
    }

    private fun observeViewModel() {
        storyViewModel.listStory.observe(this) { stories ->
            if (stories.isNotEmpty()) {
                listStory = stories
                showMarker()
            }
        }

        storyViewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setMapStyle() {
        try {
            val success =
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if (!success) {
                showToastMessage(this, "Style parsing failed.")
            }
        } catch (exception: Resources.NotFoundException) {
            showToastMessage(this, "Can't find style. Error: ${exception.message}")
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
            }
        }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                showAlertDialog(this, getString(R.string.turn_on_loc), getString(R.string.turn_on_loc_message)) {
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }
            }
            mMap.isMyLocationEnabled = true
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun handleActionMenu() {
        binding.topMapsBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.normal_type -> {
                    mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                    true
                }
                R.id.satellite_type -> {
                    mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    true
                }
                R.id.terrain_type -> {
                    mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                    true
                }
                R.id.hybrid_type -> {
                    mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                    true
                }
                else -> false
            }
        }
    }

    private fun setupProgressBar() {
        progressBar = binding.progressBar
        storyViewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }
}