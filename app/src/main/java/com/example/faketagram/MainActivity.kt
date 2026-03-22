package com.example.faketagram

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.faketagram.ui.theme.FaketagramTheme
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.google.firebase.storage.storage

private const val hostAddress = "10.0.2.2"

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        // When running in debug mode, connect to the Firebase Emulator Suite.
        // "10.0.2.2" is a special IP address which allows the Android Emulator
        // to connect to "localhost" on the host computer. The port values (9xxx)
        // must match the values defined in the firebase.json file.
        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            Firebase.database.useEmulator(hostAddress, 9000)
            Firebase.auth.useEmulator(hostAddress, 9099)
            Firebase.storage.useEmulator(hostAddress, 9199)
        }

        auth = Firebase.auth
        if (auth.currentUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        setContent {
            FaketagramTheme {
                FaketagramApp()
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in.
        if (auth.currentUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FaketagramTheme {}
}