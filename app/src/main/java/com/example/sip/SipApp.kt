package com.example.sip

import android.app.Application

class SipApp : Application() {
    lateinit var sipManager: SipManager
        private set

    override fun onCreate() {
        super.onCreate()
        sipManager = SipManager(this)
    }
}
