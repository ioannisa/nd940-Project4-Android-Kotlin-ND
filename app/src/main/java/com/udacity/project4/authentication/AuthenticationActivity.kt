package com.udacity.project4.authentication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthenticationBinding

    // startActivityForResult is now deprecated, and this is the "new way" of combining call with callback for Activity Results
    private val activityForResult by lazy {
        // activityForResult is the object to handle launching and retrieving activity result
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val response = IdpResponse.fromResultIntent(result.data)
            // callback nested to the call, we no longer need the overridden onActivityResult() method anymore
//          TODO COMPLETED: If the user was authenticated, send him to RemindersActivity
            if (result.resultCode == RESULT_OK) {
                startActivity(Intent(this, RemindersActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Sign-in Unsuccessful ${response?.error?.errorCode}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//         TODO COMPLETED: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google
        // get connected user status
        val fbAuth = FirebaseAuth.getInstance()

        when (fbAuth.currentUser){
            // No User Connected -> Login Screen
            null -> {
                binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)
                binding.lifecycleOwner = this

                binding.btnLogin.setOnClickListener {
                    firebaseLogin()
                }

                // LifecycleOwners must call register before the onStart() method
                // thus we register our activity for result by simply calling the lazy variable
                activityForResult
            }
            // If the user was authenticated, send him to RemindersActivity
            else -> {
                startActivity(Intent(this, RemindersActivity::class.java))
                finish()
            }
        }



//          TODO (OPTIONAL - NOT IMPLEMENTED YET): a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout
    }

    /**
     * This method will call upon the firebase login activity and handle the login callbacks
     */
    private fun firebaseLogin(){
        // the val activityForResult has already been initialized by lazy in our onCreate

        // create the intent for the Firebase Login
        val intent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(arrayListOf(
                        AuthUI.IdpConfig.EmailBuilder().build(),
                        AuthUI.IdpConfig.GoogleBuilder().build()
                ))
                .build()

        // launch the intent (for result)
        activityForResult.launch(intent)
    }
}
