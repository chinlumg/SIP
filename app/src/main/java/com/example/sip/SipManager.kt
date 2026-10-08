package com.example.sip

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.linphone.core.Account
import org.linphone.core.Call
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.Factory
import org.linphone.core.RegistrationState
import org.linphone.core.TransportType

enum class RegState { NONE, PROGRESS, OK, FAILED }

enum class CallStatus { IDLE, OUTGOING, INCOMING, CONNECTED, ENDED }

data class CallInfo(val status: CallStatus = CallStatus.IDLE, val remote: String = "")

/** 封裝 Liblinphone Core:註冊、撥號、接聽、掛斷,狀態以 StateFlow 對外提供。 */
class SipManager(context: Context) {

    private val _registration = MutableStateFlow(RegState.NONE)
    val registration: StateFlow<RegState> = _registration.asStateFlow()

    private val _registrationMessage = MutableStateFlow("")
    val registrationMessage: StateFlow<String> = _registrationMessage.asStateFlow()

    private val _call = MutableStateFlow(CallInfo())
    val call: StateFlow<CallInfo> = _call.asStateFlow()

    private val factory = Factory.instance()
    private val core: Core = factory.createCore(null, null, context)

    private val listener = object : CoreListenerStub() {
        override fun onAccountRegistrationStateChanged(
            core: Core,
            account: Account,
            state: RegistrationState?,
            message: String
        ) {
            _registration.value = when (state) {
                RegistrationState.Ok -> RegState.OK
                RegistrationState.Progress -> RegState.PROGRESS
                RegistrationState.Failed -> RegState.FAILED
                else -> RegState.NONE
            }
            _registrationMessage.value = message
        }

        override fun onCallStateChanged(
            core: Core,
            call: Call,
            state: Call.State?,
            message: String
        ) {
            val remote = call.remoteAddress.asStringUriOnly()
            _call.value = when (state) {
                Call.State.IncomingReceived -> CallInfo(CallStatus.INCOMING, remote)
                Call.State.OutgoingInit,
                Call.State.OutgoingProgress,
                Call.State.OutgoingRinging -> CallInfo(CallStatus.OUTGOING, remote)
                Call.State.Connected,
                Call.State.StreamsRunning -> CallInfo(CallStatus.CONNECTED, remote)
                Call.State.End,
                Call.State.Error -> CallInfo(CallStatus.ENDED, remote)
                Call.State.Released -> CallInfo()
                else -> _call.value
            }
        }
    }

    init {
        core.addListener(listener)
        core.start()
    }

    fun login(user: String, password: String, domain: String) {
        core.clearAccounts()
        core.clearAllAuthInfo()

        val authInfo = factory.createAuthInfo(user, null, password, null, null, domain)
        core.addAuthInfo(authInfo)

        val params = core.createAccountParams()
        params.identityAddress = factory.createAddress("sip:$user@$domain")
        val server = factory.createAddress("sip:$domain")
        server?.transport = TransportType.Udp
        params.serverAddress = server
        params.isRegisterEnabled = true

        val account = core.createAccount(params)
        core.addAccount(account)
        core.defaultAccount = account
    }

    fun logout() {
        core.defaultAccount?.let { core.removeAccount(it) }
        core.clearAllAuthInfo()
        _registration.value = RegState.NONE
        _registrationMessage.value = ""
    }

    fun dial(number: String) {
        val domain = core.defaultAccount?.params?.identityAddress?.domain ?: return
        val target = if (number.contains("@")) "sip:$number" else "sip:$number@$domain"
        core.invite(target)
    }

    fun answer() {
        core.currentCall?.accept()
    }

    fun hangUp() {
        (core.currentCall ?: core.calls.firstOrNull())?.terminate()
    }

    fun destroy() {
        core.removeListener(listener)
        core.stop()
    }
}
