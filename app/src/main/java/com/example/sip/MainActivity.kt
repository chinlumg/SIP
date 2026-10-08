package com.example.sip

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sip.ui.CallScreen
import com.example.sip.ui.DialScreen
import com.example.sip.ui.LoginScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sip = (application as SipApp).sipManager
        setContent {
            MaterialTheme {
                Surface { SipRoot(sip) }
            }
        }
    }
}

@Composable
private fun SipRoot(sip: SipManager) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val registration by sip.registration.collectAsStateWithLifecycle()
    val message by sip.registrationMessage.collectAsStateWithLifecycle()
    val call by sip.call.collectAsStateWithLifecycle()

    var micGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result -> micGranted = result[Manifest.permission.RECORD_AUDIO] == true }

    LaunchedEffect(Unit) {
        val perms = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= 33) perms += Manifest.permission.POST_NOTIFICATIONS
        launcher.launch(perms.toTypedArray())
    }

    when {
        call.status != CallStatus.IDLE ->
            CallScreen(call, onAnswer = sip::answer, onHangUp = sip::hangUp)
        registration == RegState.OK ->
            DialScreen(
                micGranted = micGranted,
                onDial = sip::dial,
                onLogout = sip::logout
            )
        else ->
            LoginScreen(
                state = registration,
                message = message,
                onLogin = sip::login
            )
    }
}
