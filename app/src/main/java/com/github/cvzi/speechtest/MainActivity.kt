package com.github.cvzi.speechtest

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.MotionEvent
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var editText: EditText
    private lateinit var micButton: ImageView

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            checkPermission()
        }

        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        editText = findViewById(R.id.text)
        micButton = findViewById(R.id.button)

        micButton.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                speechRecognizer.stopListening()
            }
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                micButton.setColorFilter(Color.RED)
                speechRecognizer.startListening(speechRecognizerIntent)
                editText.setText("")
                editText.hint = "Listening..."
            }
            false
        }
    }

    override fun onResume() {
        super.onResume()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(bundle: Bundle) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(v: Float) {}
            override fun onBufferReceived(bytes: ByteArray) {}
            override fun onEndOfSpeech() {}
            override fun onError(i: Int) {
                micButton.setColorFilter(getColor(R.color.teal_200))
                editText.setText("Error $i ${enumValues<SpeechError>().getOrNull(i) ?: ""}")
                editText.hint = "Tap mic button to Speak"
            }

            override fun onResults(bundle: Bundle) {
                micButton.setColorFilter(getColor(R.color.teal_200))
                val data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                editText.setText(data!![0])
                editText.hint = "Tap mic button to Speak"
            }

            override fun onPartialResults(bundle: Bundle) {}
            override fun onEvent(i: Int, bundle: Bundle) {}
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }

    private fun checkPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            RecordAudioRequestCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RecordAudioRequestCode && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) Toast.makeText(
                this,
                "Permission Granted",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    companion object {
        const val RecordAudioRequestCode = 1

        enum class SpeechError {
            ERROR_UNKNOWN,
            ERROR_NETWORK_TIMEOUT,
            ERROR_NETWORK,
            ERROR_AUDIO,
            ERROR_SERVER,
            ERROR_CLIENT,
            ERROR_SPEECH_TIMEOUT,
            ERROR_NO_MATCH,
            ERROR_RECOGNIZER_BUSY,
            ERROR_INSUFFICIENT_PERMISSIONS,
            ERROR_TOO_MANY_REQUESTS,
            ERROR_SERVER_DISCONNECTED,
            ERROR_LANGUAGE_NOT_SUPPORTED,
            ERROR_LANGUAGE_UNAVAILABLE,
            ERROR_CANNOT_CHECK_SUPPORT,
        }
    }
}