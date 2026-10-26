package com.yacir.apkcyberstudio

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var etPath: EditText
    private lateinit var tvConsole: TextView
    private lateinit var scrollConsole: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etPath = findViewById(R.id.etPath)
        tvConsole = findViewById(R.id.tvConsole)
        scrollConsole = findViewById(R.id.scrollConsole)

        findViewById<Button>(R.id.btnDecompile).setOnClickListener {
            val path = etPath.text.toString()
            val out = path.replace(".apk", "_decompiled")
            executeCommand("java -jar /sdcard/APK_Studio/apktool.jar d '$path' -o '$out' -f")
        }

        findViewById<Button>(R.id.btnRebuild).setOnClickListener {
            val path = etPath.text.toString()
            val out = "${path}_modded.apk"
            executeCommand("java -jar /sdcard/APK_Studio/apktool.jar b '$path' -o '$out'")
        }

        findViewById<Button>(R.id.btnSign).setOnClickListener {
            val path = etPath.text.toString()
            executeCommand("java -jar /sdcard/APK_Studio/uber-signer.jar --apks '$path'")
        }

        findViewById<Button>(R.id.btnInstall).setOnClickListener {
            val path = etPath.text.toString()
            executeCommand("termux-open '$path'")
        }
    }

    private fun appendLog(text: String) {
        runOnUiThread {
            tvConsole.append("$text\n")
            scrollConsole.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    private fun executeCommand(command: String) {
        appendLog("\n[EXEC] > $command")
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    appendLog(line!!)
                }

                val exitCode = process.waitFor()
                withContext(Dispatchers.Main) {
                    if (exitCode == 0) {
                        appendLog("[✓] تمت العملية بنجاح!")
                    } else {
                        appendLog("[✗] حدث خطأ أثناء التنفيذ (Code: $exitCode)")
                    }
                }
            } catch (e: Exception) {
                appendLog("[ERROR] ${e.message}")
            }
        }
    }
}
