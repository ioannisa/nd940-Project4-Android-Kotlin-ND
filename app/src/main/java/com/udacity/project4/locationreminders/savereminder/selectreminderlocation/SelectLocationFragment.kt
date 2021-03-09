package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.geofence.GeofencingConstants

import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    private val TAG = SelectLocationFragment::class.java.simpleName

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var map: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        // always initialize the location string value to null
        _viewModel.reminderSelectedLocationStr.value = null

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

//        TODO COMPLETED: add the map setup implementation
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


//        TODO COMPLETED: call this function after the user confirms on the selected location
        binding.btnSaveLocation.setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }

    private fun onLocationSelected() {
        //        TODO COMPLETED: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
        _viewModel.saveMarkerLocation.value = true
        _viewModel.navigationCommand.postValue(NavigationCommand.Back)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO COMPLETED: Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        Log.d("TEST", "MAPS INITIALIZED: ")

//        TODO COMPLETED: zoom to the user location after taking his permission
        enableMyLocation(map)
//        TODO COMPLETED: add style to the map
        setMapStyle(map)
//        TODO COMPLETED: put a marker to location that the user selected
        setMapLongClick(map)
    }

    /**
     * Given the json file produced by the https://mapstyle.withgoogle.com/
     * Load the customized maps style on the GoogleMap object
     */
    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    activity,
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    /**
     * On long click update marker position
     */
    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            updateMarkerPosition(latLng)
        }
    }

    /**
     * 1) Remove possible previous marker
     * 2) and add new marker with a circle based on the geofence radius
     */
    private fun updateMarkerPosition(latLng: LatLng){
        // remove previous marker (if any)
        _viewModel.marker?.remove()
        _viewModel.circle?.remove()

        // add new marker
        _viewModel.marker = map.addMarker(
            MarkerOptions()
                .position(latLng)
                .draggable(false)
                .title("Reminder Location Area")
        )

        _viewModel.circle = map.addCircle(CircleOptions()
            .center(latLng)
            .radius(GeofencingConstants.RADIUS_IN_METERS.toDouble())
            .strokeColor(Color.MAGENTA)
            .fillColor(0x220000FF)
            .strokeWidth(5f)
        )
    }




    /*************************************
     * PERMISSIONS SECTION - FOR USER FINE_LOCATION
     *
     * Since API 30, it is enforced to ask separately for FINE LOCATION and BACKGROUND LOCATION
     * Thus we will ask only for the FINE_LOCATION here at the maps to get the user's location
     * and we will separately ask for BACKGROUND_LOCATION permission, when we will use the geofence.
     *
     * Reference: https://developer.android.com/training/location/permissions
     ************************************/

    /**
     * Returns true if location permissions are granted, false otherwise
     */
    fun isPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) === PackageManager.PERMISSION_GRANTED
    }

    /**
     * If location permissions are granted, move to the current location with zoom 16
     * otherwise present permissions dialog
     */
    fun enableMyLocation(map: GoogleMap) {
        if (isPermissionGranted()) {
            gotoMyLocation(map, 16f)
        }
        else {
            requestPermissions(
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_FINE_LOCATION_PERMISSION
            )
        }
    }

    /**
     * 1) Enable My Location
     * 2) Move the camera with the specified zoom at the user's location (Last Known Location)
     */
    @SuppressLint("MissingPermission")
    private fun gotoMyLocation(map: GoogleMap, zoom: Float){
        map.setMyLocationEnabled(true)

        val locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        val location: Location? = locationManager.getLastKnownLocation(
            locationManager.getBestProvider(
                criteria,
                false
            )!!
        )
        val lat: Double = location?.latitude ?: 0.0
        val lng: Double = location?.longitude ?: 0.0

        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(lat, lng),
                zoom
            )
        )
        map.getUiSettings().setZoomControlsEnabled(true)

        // uppon getting current position, put a marker on it as well
        if (location != null) {
            updateMarkerPosition(LatLng(location.latitude, location.longitude))
        }
    }

    /**
     * If presented by permissions dialog...
     * check if permission was granted, and if so Enable My Location
     */
    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_FINE_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation(map)
            }
        }
    }
}

private const val REQUEST_FINE_LOCATION_PERMISSION = 1
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
