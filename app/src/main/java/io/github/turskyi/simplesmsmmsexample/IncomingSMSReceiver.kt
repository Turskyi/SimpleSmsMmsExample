package io.github.turskyi.simplesmsmmsexample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsManager
import android.telephony.SmsMessage
import java.util.*

class IncomingSMSReceiver : BroadcastReceiver() {

    companion object {
        private const val queryString = "@echo"
        private const val SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED"
    }

    override fun onReceive(_context: Context?, _intent: Intent) {
        if (_intent.action == SMS_RECEIVED) {
            val sms: SmsManager = SmsManager.getDefault()
            val bundle = _intent.extras
            if (bundle != null) {
                val pdus = bundle["pdus"] as Array<*>?
                val messages: Array<SmsMessage?>? = pdus?.size?.let { arrayOfNulls(it) }
                if(pdus?.indices != null){
                    for (i in pdus.indices){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val format = bundle.getString("format")

                            messages?.set(
                                i,
                                SmsMessage.createFromPdu(pdus[i] as ByteArray, format)
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            messages?.set(
                                i,
                                SmsMessage.createFromPdu(pdus[i] as ByteArray)
                            )
                        }
                    }
                }

                if (messages != null) {
                    for (message in messages) {
                        val msg: String? = message?.messageBody
                        val to: String? = message?.originatingAddress
                        if (msg?.toLowerCase(Locale.getDefault())?.startsWith(queryString) == true) {
                            val out = msg.substring(queryString.length)
                            /** this method sending back the same sms which received */
                            sms.sendTextMessage(to, null, out, null, null)
                        }
                    }
                }
            }
        }
    }
}