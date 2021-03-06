package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.geofence.GeofencingConstants
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    private val TAG: String = "SaveReminderFragment"

        //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient

    private var reminderDataItem: ReminderDataItem? = null

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = GeofencingConstants.ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel
        geofencingClient = LocationServices.getGeofencingClient(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

//            TODO COMPLETED: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db
            reminderDataItem = ReminderDataItem(title, description, location, latitude, longitude)

            // First we need to have validated data
            reminderDataItem?.let {
                // IF data is validated
                if (_viewModel.validateEnteredData(it)) {
                    // and if we have the BACKGROUND permission granted
                    if (isPermissionGranted()) {
                        setReminderGeoFence(it.id, it.latitude ?: 0.0, it.longitude ?: 0.0)    // THEN add the GeoFence
                        _viewModel.saveReminder(it)                                                             // and save its data to DB
                    }
                    // if we have validated data, but no background permission
                    else{
                        // if permission not set, request it and WHEN granted it will add GeoFence and save data to DB
                       requestBackgroundLocationPermission()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    /*************************
     *** GEOFENCES SECTION ***
     *************************/

    @SuppressLint("MissingPermission")
    fun setReminderGeoFence(id: String, latitude: Double, longitude: Double){

        // Create a GeoFence with ENTER transition
        val geofence = Geofence.Builder()
            .setRequestId(id)
            .setCircularRegion(
                latitude,
                longitude,
                GeofencingConstants.RADIUS_IN_METERS
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.removeGeofences(geofencePendingIntent).run {
            addOnCompleteListener {
                geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
                    addOnSuccessListener {
                        _viewModel.showSnackBarInt.value = R.string.geofences_added
                        Log.d(TAG, "GEOFENCE ADDED SUCCESSFULLY: ${geofence.requestId}")
                    }
                    addOnFailureListener {
                        //_viewModel.showErrorMessage.postValue(getString(R.string.geofences_not_added))
                        _viewModel.showSnackBarInt.value = R.string.geofences_not_added
                        Log.d(TAG, "GEOFENCE FAILED TO ADD: ")
                        if ((it.message != null)) {
                            Log.w(TAG, it.message?:"Exception")
                        }
                    }
                }
            }
        }
    }


    /*************************************
     * GEOFENCES PERMISSIONS SECTION - (USER BACKGROUND_LOCATION)
     *
     * Since API 30, it is enforced to ask separately for FINE LOCATION and BACKGROUND LOCATION
     * Here we are about to access the GeoFences section, thus we will focus
     * on the BACKGROUND_LOCATION permission only as the FINE_LOCATION has already been granted
     * in order to get the GeoFence coordinates.
     *
     * Reference: https://developer.android.com/training/location/permissions
     ************************************/

    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.Q

    /**
     * Returns true if ACCESS_BACKGROUND_LOCATION is granted, or if API less than 29
     */
    @TargetApi(29)
    fun isPermissionGranted() : Boolean {
        return if (runningQOrLater) {
            PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            true
        }
    }

    /**
     * If location permissions are granted, move to the current location with zoom 16
     * otherwise present permissions dialog
     */
    @TargetApi(29)
    fun requestBackgroundLocationPermission(): Boolean {
        if (isPermissionGranted()) {
            return true
        }
        else {
            requestPermissions(
                arrayOf<String>(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                REQUEST_BACKGROUND_LOCATION_PERMISSION
            )
        }
        return false
    }

    /**
     * If presented by permissions dialog...
     * check if permission was granted, and if so Enable My Location
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_BACKGROUND_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {             // ok we granted the permission...
                reminderDataItem?.let {
                    reminderDataItem?.let {
                        if (_viewModel.validateEnteredData(it)) {                                                   // IF data is also validated
                            _viewModel.saveReminder(it)                                                             // THEN save its data to DB
                            setReminderGeoFence(it.id, it.latitude ?: 0.0, it.longitude ?: 0.0)    // and set the GeoFence
                        }
                    }
                }
            }
        }
    }
}

private const val REQUEST_BACKGROUND_LOCATION_PERMISSION = 2
