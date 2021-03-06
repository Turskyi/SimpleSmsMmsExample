package io.github.turskyi.simplesmsmmsexample

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val phoneNumberEditText = findViewById<View>(R.id.editTextPhoneNumber) as EditText
        val smsTextEditText = findViewById<View>(R.id.editTextSmsText) as EditText
        val sendButton: Button = findViewById<View>(R.id.buttonSms) as Button
        sendButton.setOnClickListener {
            openSmsAppWithFilledText(phoneNumberEditText, smsTextEditText)
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
}