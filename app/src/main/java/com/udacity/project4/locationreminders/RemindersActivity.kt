package com.udacity.project4.locationreminders

import android.app.AlertDialog
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.udacity.project4.R
import kotlinx.android.synthetic.main.activity_reminders.*


/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)

        // check if the device's PLACES is activated in the settings
        checkDeviceLocationSettings()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                (nav_host_fragment as NavHostFragment).navController.popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * This application heavily relies on PLACES in the settings enabled in device.
     *
     * We need PLACES activated to get current user's location, but also to allow geofences to track our position
     * Thus, when we enter the main screen, we check for availability of the PLACES and request user to turn it on if it is off.
     */
    private fun checkDeviceLocationSettings(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(this)
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        // we don't care about onSuccessListener (which means places is activated),
        // we just care for failure callbacks
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                try {
                    exception.startResolutionForResult(
                        this,
                        REQUEST_TURN_DEVICE_LOCATION_ON
                    )

                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                    AlertDialog.Builder(this)
                        .setTitle(getString(R.string.dlg_no_resolve_places_title))
                        .setMessage(getString(R.string.dlg_no_resolve_places_body))
                        .setPositiveButton("ok") { dialog, which ->
                            this.finish()
                        }
                        .show()

                }
            } else {
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.dlg_no_resolve_places_title))
                    .setMessage(getString(R.string.dlg_no_resolve_places_body))
                    .setPositiveButton("ok") { dialog, which ->
                        this.finish()
                    }
                    .show()
            }
        }

        locationSettingsResponseTask.addOnCompleteListener {
            if ( !it.isSuccessful ) {

            }
        }
    }

    /**
     * OnAcitivtyResult is still NOT deprecated when used to to receive ResolvableApiException callbacks.
     *
     * Here we use it to grab result from the "startResolutionForResult" in our "checkDeviceLocationSettings" function
     *
     * More specifically, if the LOCATION is off, and we can resolve this via "startResolutionForResult", then
     * the system will try to turn on location for us if we press ok, or do nothing if we press cancel.
     *
     * We want that result returned back in the onActivityResult to find out what the outcome of the resolution was
     * in order to take the appropriate next steps.
     *
     * https://developers.google.com/android/reference/com/google/android/gms/common/api/ResolvableApiException
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // listen only for the request code related to turning the device location on
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            // resultCode 0 -> for cancel or failure to enable Places
            // resultCode 1 -> for successfully enabling Places

            AlertDialog.Builder(this)
                .setTitle(getString(R.string.dlg_no_resolve_places_title))
                .setMessage(getString(R.string.dlg_no_resolve_places_body))
                .setPositiveButton("TURN ON") { dialog, which ->
                    checkDeviceLocationSettings()
                }
                .setNegativeButton("EXIT") { dialog, which ->
                    this.finish()
                }
                .show()
        }
    }
}

private const val TAG = "RemindersActivity"
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29

