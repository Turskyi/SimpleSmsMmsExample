package io.github.turskyi.simplesmsmmsexample

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    companion object {
        const val SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED"
        private const val PERMISSION_REQUEST_CODE = 2
        private const val PICK_IMAGE = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkPermission()) {
                Log.e("permission", "Permission already granted.")
            } else {
                //If the app doesn’t have the SEND_SMS permission, request it//
                requestPermission()
            }
        }

        if (checkPermission()) {
            /** listen for receiving sms */
            val filter = IntentFilter(SMS_RECEIVED)
            val receiver: BroadcastReceiver = IncomingSMSReceiver()
            registerReceiver(receiver, filter)
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }

        val phoneNumberEditText = findViewById<View>(R.id.editTextPhoneNumber) as EditText
        val smsTextEditText = findViewById<View>(R.id.editTextSmsText) as EditText
        val sendButton: Button = findViewById<View>(R.id.buttonSms) as Button
        val smsButton: Button = findViewById<View>(R.id.buttonSMSActivity) as Button
        val mmsButton: Button = findViewById<View>(R.id.buttonMMS) as Button
        sendButton.setOnClickListener {
            openSmsAppWithFilledText(phoneNumberEditText, smsTextEditText)
        }
        smsButton.setOnClickListener {
            val intent = Intent(this, SMSActivity::class.java)
            startActivity(intent)
        }
        mmsButton.setOnClickListener {
            /** Getting the URI path of the data to be attached */
            val getIntent = Intent(Intent.ACTION_GET_CONTENT)
            getIntent.type = "image/*"

            val pickIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickIntent.type = "image/*"

            val chooserIntent = Intent.createChooser(getIntent, "Select Image")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))

            startActivityForResult(chooserIntent, PICK_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                return
            } else {
                /** send received picture to mms */
                val attachedUri: Uri? = data.data
                val mmsIntent = Intent(Intent.ACTION_SEND, attachedUri)
                mmsIntent.putExtra("sms_body", "Please see the attached image")
                mmsIntent.putExtra("address", "07912355432")
                mmsIntent.putExtra(Intent.EXTRA_STREAM, attachedUri)
                mmsIntent.type = "image/*" // or image/png
                startActivity(mmsIntent)
            }
        }
    }

    private fun openSmsAppWithFilledText(
        phoneNumberEditText: EditText,
        smsTextEditText: EditText
    ) {
        val smsNumber = phoneNumberEditText.text.toString()
        val smsText = smsTextEditText.text.toString()
        val uri: Uri = Uri.parse("sms:$smsNumber")
        val smsIntent = Intent(Intent.ACTION_SENDTO, uri)
        smsIntent.putExtra("sms_body", smsText)
        startActivity(smsIntent)
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
            this@MainActivity, arrayOf(
                Manifest.permission.SEND_SMS
            ), PERMISSION_REQUEST_CODE
        )
    }
}