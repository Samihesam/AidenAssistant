package com.example.personalassistant

import android.telecom.Call
import android.telecom.InCallService

class AssistantInCallService : InCallService() {

    companion object {
        var currentCall: Call? = null
        const val ACTION_INCOMING_CALL = "com.example.personalassistant.INCOMING_CALL"
    }

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            when (state) {
                Call.STATE_RINGING -> notifyAssistant(call, "incoming")
                Call.STATE_ACTIVE -> notifyAssistant(call, "active")
                Call.STATE_DISCONNECTED -> {
                    notifyAssistant(call, "ended")
                    currentCall = null
                }
            }
        }
    }

    override fun onCallAdded(call: Call) {
        currentCall = call
        call.registerCallback(callCallback)

        if (call.state == Call.STATE_RINGING) {
            notifyAssistant(call, "incoming")
        }
    }

    override fun onCallRemoved(call: Call) {
        call.unregisterCallback(callCallback)
        if (currentCall == call) currentCall = null
    }

    private fun notifyAssistant(call: Call, status: String) {
        val phoneNumber = call.details?.handle?.schemeSpecificPart ?: "ناشناس"
        val broadcast = android.content.Intent(ACTION_INCOMING_CALL).apply {
            putExtra("status", status)
            putExtra("phone_number", phoneNumber)
        }
        sendBroadcast(broadcast)
    }

    fun answerCall() {
        currentCall?.answer(0)
    }

    fun rejectCall() {
        currentCall?.reject(false, null)
    }

    fun hangUp() {
        currentCall?.disconnect()
    }
}
