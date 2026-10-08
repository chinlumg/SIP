package com.example.sip.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.sip.CallInfo
import com.example.sip.CallStatus
import com.example.sip.RegState

@Composable
fun LoginScreen(
    state: RegState,
    message: String,
    onLogin: (user: String, password: String, domain: String) -> Unit
) {
    var user by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var domain by rememberSaveable { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        Text("SIP 登入", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(user, { user = it }, label = { Text("帳號") },
            singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(password, { password = it }, label = { Text("密碼") },
            singleLine = true, visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth())
        OutlinedTextField(domain, { domain = it }, label = { Text("網域 / 伺服器") },
            singleLine = true, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = { onLogin(user.trim(), password, domain.trim()) },
            enabled = user.isNotBlank() && domain.isNotBlank() && state != RegState.PROGRESS,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (state == RegState.PROGRESS) "註冊中…" else "登入") }
        if (state == RegState.FAILED) {
            Text("註冊失敗:$message", color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun DialScreen(micGranted: Boolean, onDial: (String) -> Unit, onLogout: () -> Unit) {
    var number by rememberSaveable { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        Text("撥號", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(number, { number = it }, label = { Text("號碼或 SIP 位址") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.fillMaxWidth())
        if (!micGranted) {
            Text("需要麥克風權限才能撥打", color = MaterialTheme.colorScheme.error)
        }
        Button(
            onClick = { onDial(number.trim()) },
            enabled = number.isNotBlank() && micGranted,
            modifier = Modifier.fillMaxWidth()
        ) { Text("撥打") }
        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("登出") }
    }
}

@Composable
fun CallScreen(call: CallInfo, onAnswer: () -> Unit, onHangUp: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(call.remote, style = MaterialTheme.typography.headlineSmall)
        Text(
            when (call.status) {
                CallStatus.OUTGOING -> "撥號中…"
                CallStatus.INCOMING -> "來電"
                CallStatus.CONNECTED -> "通話中"
                CallStatus.ENDED -> "通話結束"
                CallStatus.IDLE -> ""
            }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (call.status == CallStatus.INCOMING) {
                Button(onClick = onAnswer) { Text("接聽") }
            }
            if (call.status != CallStatus.ENDED) {
                OutlinedButton(onClick = onHangUp) {
                    Text(if (call.status == CallStatus.INCOMING) "拒接" else "掛斷")
                }
            }
        }
    }
}
