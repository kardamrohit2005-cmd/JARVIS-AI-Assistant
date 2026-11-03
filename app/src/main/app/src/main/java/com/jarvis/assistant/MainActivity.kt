package com.jarvis.assistant

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var statusText: TextView
    private val SPEECH_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // JARVIS Screen UI (बिना अलग XML फाइल के)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#0B0F19"))
            setPadding(48, 48, 48, 48)
        }

        val titleText = TextView(this).apply {
            text = "J.A.R.V.I.S."
            textSize = 34f
            setTextColor(Color.parseColor("#00E5FF"))
            gravity = Gravity.CENTER
        }

        statusText = TextView(this).apply {
            text = "System Ready. Tap the button to speak."
            textSize = 18f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 50, 0, 70)
        }

        val speakButton = Button(this).apply {
            text = "SPEAK TO JARVIS"
            textSize = 16f
            setBackgroundColor(Color.parseColor("#00E5FF"))
            setTextColor(Color.BLACK)
            setOnClickListener {
                checkPermissionAndListen()
            }
        }

        layout.addView(titleText)
        layout.addView(statusText)
        layout.addView(speakButton)
        setContentView(layout)

        tts = TextToSpeech(this, this)
    }

    private fun checkPermissionAndListen() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        } else {
            startVoiceInput()
        }
    }

    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "JARVIS सुन रहा है...")
        }
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE)
        } catch (e: Exception) {
            statusText.text = "Voice input आपके डिवाइस पर उपलब्ध नहीं है।"
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            val spokenText = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""
            statusText.text = "You: $spokenText"
            respondToCommand(spokenText.lowercase(Locale.getDefault()))
        }
    }

    private fun respondToCommand(command: String) {
        val reply = when {
            command.contains("hello") || command.contains("hi") || command.contains("namaste") ->
                "Hello sir, JARVIS is online. How can I help you?"
            command.contains("time") || command.contains("samay") ->
                "Current time is " + java.text.SimpleDateFormat("hh:mm a", Locale.getDefault()).format(java.util.Date())
            command.contains("who are you") || command.contains("kaun ho") ->
                "I am JARVIS, your personal AI voice assistant."
            else -> "You said: $command"
        }

        statusText.text = "You: $command\n\nJARVIS: $reply"
        tts.speak(reply, TextToSpeech.QUEUE_FLUSH, null, "JarvisReply")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
        }
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}
