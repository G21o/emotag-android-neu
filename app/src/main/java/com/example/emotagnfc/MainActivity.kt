package com.example.emotagnfc

import android.app.Activity
import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.view.ViewGroup

class MainActivity : Activity() {
    private lateinit var status: TextView
    private var adapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = NfcAdapter.getDefaultAdapter(this)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(36, 48, 36, 36)
        }
        val title = TextView(this).apply {
            text = "Emotag NFC-Test"
            textSize = 26f
        }
        status = TextView(this).apply {
            textSize = 17f
            text = initialStatus()
            setPadding(0, 24, 0, 24)
        }
        val refresh = Button(this).apply {
            text = "NFC-Status prüfen"
            setOnClickListener { status.text = initialStatus() }
        }
        layout.addView(title)
        layout.addView(status, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        layout.addView(refresh)
        setContentView(layout)
        handleIntentTag(intent?.getParcelableExtra(NfcAdapter.EXTRA_TAG))
    }

    private fun initialStatus(): String = when {
        adapter == null -> "Dieses Gerät meldet keinen NFC-Adapter."
        adapter?.isEnabled != true -> "NFC ist ausgeschaltet. Aktiviere NFC in den Android-Einstellungen."
        else -> "NFC ist verfügbar. Halte einen NFC-Tag an die Rückseite des Handys. Diese Test-App schreibt keine Daten."
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handleIntentTag(intent.getParcelableExtra(NfcAdapter.EXTRA_TAG))
    }

    private fun handleIntentTag(tag: Tag?) {
        if (tag == null) return
        status.text = try {
            val ndef = Ndef.get(tag)
            if (ndef == null) {
                "Tag erkannt, aber kein NDEF-Datensatz verfügbar. Keine Daten geschrieben."
            } else {
                ndef.connect()
                val message = ndef.ndefMessage
                ndef.close()
                val records = message?.records?.size ?: 0
                "NFC-Tag erkannt. NDEF-Datensätze: $records. Keine Daten geschrieben."
            }
        } catch (e: Exception) {
            "Tag erkannt, Lesen fehlgeschlagen: ${e.message ?: "unbekannter Fehler"}"
        }
    }
}
