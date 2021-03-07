package io.github.turskyi.simplesmsmmsexample

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/* this code is working only till Android 6 */
class SMSActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1

        //  Flags for sending and delivering SMS
        var SENT_SMS_FLAG = "SENT_SMS"
        var DELIVER_SMS_FLAG = "DELIVER_SMS"
    }

    var callButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_s_m_s)
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkPermission()) {
                Log.e("permission", "Permission already granted.")
            } else {
                //If the app doesn’t have the SEND_SMS permission, request it//
                requestPermission()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(sentReceiver, IntentFilter(SENT_SMS_FLAG))
        registerReceiver(deliverReceiver, IntentFilter(DELIVER_SMS_FLAG))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(sentReceiver)
        unregisterReceiver(deliverReceiver)
    }

    var sentReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, `in`: Intent?) {
            when (resultCode) {
                RESULT_OK -> {
                    // sent SMS message successfully;
                    val toast = Toast.makeText(
                        applicationContext,
                        "Message sent!", Toast.LENGTH_SHORT
                    )
                    toast.show()
                }
                else -> {
                }
            }
        }
    }

    private var deliverReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, `in`: Intent) {
            // SMS delivered actions
            when (resultCode) {
                RESULT_OK -> {
                    // sent SMS message successfully;
                    val toast = Toast.makeText(
                        applicationContext,
                        "Message delivered!", Toast.LENGTH_SHORT
                    )
                    toast.show()
                }
                else -> {
                }
            }
        }
    }

    fun onClick(@Suppress("UNUSED_PARAMETER") view: View?) {
        val phoneNumber = findViewById<EditText>(R.id.edit_phone)
        val smsMessage = findViewById<EditText>(R.id.edit_sms)
        val phoneNum = phoneNumber.text.toString()
        val sms = smsMessage.text.toString()
        if (!TextUtils.isEmpty(phoneNum) && !TextUtils.isEmpty(sms)) {
            if (checkPermission()) {
                val sentIn = Intent(SENT_SMS_FLAG)
                val sentPIn = PendingIntent.getBroadcast(
                    this, 0,
                    sentIn, 0
                )
                val deliverIn = Intent(SENT_SMS_FLAG)
                val deliverPIn = PendingIntent.getBroadcast(
                    this, 0,
                    deliverIn, 0
                )
                val smsManager = SmsManager.getDefault()

                /* for short sms*/
//                smsManager.sendTextMessage(
//                    phoneNum, null, sms, sentPIn,
//                    deliverPIn
//                );

                val deliverIntents: ArrayList<PendingIntent> = arrayListOf()
                deliverIntents.add(deliverPIn)

                /* for long sms */
                val messageArray = smsManager.divideMessage(sms)
                val sentIntents = ArrayList<PendingIntent>()
                for (i in 0 until messageArray.size) sentIntents.add(sentPIn)
                smsManager.sendMultipartTextMessage(
                    phoneNum,
                    null, messageArray, sentIntents, deliverIntents
                )

            } else {
                Toast.makeText(this@SMSActivity, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermission(): Boolean {
        val smsPermissionResult = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.SEND_SMS
        )
        return smsPermissionResult == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this@SMSActivity, arrayOf(
                Manifest.permission.SEND_SMS
            ), PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                callButton = findViewById(R.id.sendsms)
                if (grantResults.isNotEmpty()) {
                    val smsPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    if (smsPermission) {
                        Toast.makeText(
                            this@SMSActivity,
                            "Permission accepted", Toast.LENGTH_LONG
                        ).show()
                        //If the permission is denied…
                    } else {
                        Toast.makeText(
                            this@SMSActivity,
                            "Permission denied", Toast.LENGTH_LONG
                        ).show()
                        // disable the call button.//
                        callButton?.isEnabled = false
                    }
                }
            }
        }
    }
}