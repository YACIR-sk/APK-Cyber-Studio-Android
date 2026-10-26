package com.yacir.apkcyberstudio

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var etPath: EditText
    private lateinit var tvConsole: TextView
    private lateinit var scrollConsole: ScrollView
    private var isDarkMode = true

    private val javaBinary: String
        get() {
            val termuxJava = "/data/data/com.termux/files/usr/bin/java"
            return if (File(termuxJava).exists()) termuxJava else "java"
        }

    private val openFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val realPath = copyUriToWorkDir(uri)
                etPath.setText(realPath)
                appendLog("[✓] تم جلب الملف: $realPath")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etPath = findViewById(R.id.etPath)
        tvConsole = findViewById(R.id.tvConsole)
        scrollConsole = findViewById(R.id.scrollConsole)

        findViewById<Button>(R.id.btnBrowse).setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            openFileLauncher.launch(intent)
        }

        findViewById<ImageButton>(R.id.btnThemeToggle).setOnClickListener {
            isDarkMode = !isDarkMode
            val mode = if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(mode)
        }

        findViewById<Button>(R.id.btnDecompileFull).setOnClickListener {
            val path = etPath.text.toString()
            val out = path.replace(".apk", "_decompiled")
            executeCommand("$javaBinary -jar /sdcard/APK_Studio/apktool.jar d '$path' -o '$out' -f")
        }

        findViewById<Button>(R.id.btnDecompileSrc).setOnClickListener {
            val path = etPath.text.toString()
            val out = path.replace(".apk", "_src")
            executeCommand("$javaBinary -jar /sdcard/APK_Studio/apktool.jar d -r '$path' -o '$out' -f")
        }

        findViewById<Button>(R.id.btnDecompileRes).setOnClickListener {
            val path = etPath.text.toString()
            val out = path.replace(".apk", "_res")
            executeCommand("$javaBinary -jar /sdcard/APK_Studio/apktool.jar d -s '$path' -o '$out' -f")
        }

        findViewById<Button>(R.id.btnRebuild).setOnClickListener {
            val path = etPath.text.toString()
            val out = "${path}_modded.apk"
            executeCommand("$javaBinary -jar /sdcard/APK_Studio/apktool.jar b '$path' -o '$out'")
        }

        findViewById<Button>(R.id.btnSign).setOnClickListener {
            val path = etPath.text.toString()
            executeCommand("$javaBinary -jar /sdcard/APK_Studio/uber-signer.jar --apks '$path'")
        }

        findViewById<Button>(R.id.btnVerify).setOnClickListener {
            val path = etPath.text.toString()
            executeCommand("$javaBinary -jar /sdcard/APK_Studio/uber-signer.jar --verify --apks '$path'")
        }

        findViewById<Button>(R.id.btnInstallFw).setOnClickListener {
            val path = etPath.text.toString()
            executeCommand("$javaBinary -jar /sdcard/APK_Studio/apktool.jar if '$path'")
        }

        findViewById<Button>(R.id.btnInstall).setOnClickListener {
            val path = etPath.text.toString()
            executeCommand("termux-open '$path'")
        }
    }

    private fun copyUriToWorkDir(uri: Uri): String {
        var fileName = "imported_app.apk"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                fileName = cursor.getString(nameIndex)
            }
        }
        val destFile = File("/sdcard/APK_Studio", fileName)
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
        return destFile.absolutePath
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
                        appendLog("[✗] خطأ أثناء التنفيذ (Code: $exitCode)")
                    }
                }
            } catch (e: Exception) {
                appendLog("[ERROR] ${e.message}")
            }
        }
    }
}
