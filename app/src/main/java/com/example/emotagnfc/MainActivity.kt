
package com.example.emotagnfc

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.IsoDep
import android.nfc.tech.NfcA
import android.nfc.tech.NfcB
import android.nfc.tech.NfcF
import android.nfc.tech.NfcV
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
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

        layout.addView(TextView(this).apply {
            text = "Emotag Geräteanalyse"
            textSize = 25f
        })

        status = TextView(this).apply {
            textSize = 16f
            setPadding(0, 24, 0, 24)
            text = initialStatus()
        }

        layout.addView(status, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))

        layout.addView(Button(this).apply {
            text = "NFC-Status aktualisieren"
            setOnClickListener { status.text = initialStatus() }
        })

        val scroll = ScrollView(this)
        scroll.addView(layout)
        setContentView(scroll)

        @Suppress("DEPRECATION")
        handleTag(intent?.getParcelableExtra(NfcAdapter.EXTRA_TAG))
    }

    private fun initialStatus(): String = when {
        adapter == null -> "Kein NFC-Adapter gefunden."
        adapter?.isEnabled != true ->
            "NFC ist ausgeschaltet. Bitte in den Android-Einstellungen aktivieren."
        else ->
            "NFC ist bereit.\n\nHalte den Emotag ans Handy. " +
            "Die App liest nur technische Informationen und schreibt keine Daten."
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        @Suppress("DEPRECATION")
        handleTag(intent.getParcelableExtra(NfcAdapter.EXTRA_TAG))
    }

    private fun handleTag(tag: Tag?) {
        if (tag == null) return

        val technologies = tag.techList
        val id = tag.id.joinToString("") { "%02X".format(it) }

        val details = StringBuilder()
        details.append("EMOTAG ERKANNT\n\n")
        details.append("Tag-ID: $id\n\n")
        details.append("NFC-Technologien:\n")
        technologies.forEach { details.append("• $it\n") }

        details.append("\nZusatzinformationen:\n")
        details.append("NfcA: ${NfcA.get(tag) != null}\n")
        details.append("NfcB: ${NfcB.get(tag) != null}\n")
        details.append("NfcF: ${NfcF.get(tag) != null}\n")
        details.append("NfcV: ${NfcV.get(tag) != null}\n")
        details.append("IsoDep: ${IsoDep.get(tag) != null}\n")

        val ndef = Ndef.get(tag)
        details.append("NDEF: ${ndef != null}\n")

        if (ndef != null) {
            details.append("NDEF schreibgeschützt: ${ndef.isWritable.not()}\n")
            details.append("NDEF-Kapazität: ${ndef.maxSize} Bytes\n")
        }

        details.append(
            "\nHinweis: Diese Angaben identifizieren die NFC-Schnittstelle, " +
            "aber noch nicht das Bildübertragungsprotokoll."
        )

        status.text = details.toString()
    }
}
