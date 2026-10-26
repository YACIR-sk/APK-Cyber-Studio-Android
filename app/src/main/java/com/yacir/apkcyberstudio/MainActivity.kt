package com.yacir.apkcyberstudio

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var etPath: EditText
    private lateinit var tvConsole: TextView
    private lateinit var scrollConsole: ScrollView
    private var isDarkMode = true

    private val openFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    val realPath = copyUriToWorkDir(uri)
                    etPath.setText(realPath)
                    appendLog("[✓] تم تحديد الملف: $realPath")
                } catch (e: Exception) {
                    appendLog("[ERROR] فشل قراءة الملف: ${e.message}")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkStoragePermission()

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
            val path = etPath.text.toString().trim()
            if (path.isEmpty()) return@setOnClickListener
            val out = path.replace(".apk", "_decompiled")
            sendTermuxCommand("java -jar /sdcard/APK_Studio/apktool.jar d '$path' -o '$out' -f")
        }

        findViewById<Button>(R.id.btnDecompileSrc).setOnClickListener {
            val path = etPath.text.toString().trim()
            if (path.isEmpty()) return@setOnClickListener
            val out = path.replace(".apk", "_src")
            sendTermuxCommand("java -jar /sdcard/APK_Studio/apktool.jar d -r '$path' -o '$out' -f")
        }

        findViewById<Button>(R.id.btnDecompileRes).setOnClickListener {
            val path = etPath.text.toString().trim()
            if (path.isEmpty()) return@setOnClickListener
            val out = path.replace(".apk", "_res")
            sendTermuxCommand("java -jar /sdcard/APK_Studio/apktool.jar d -s '$path' -o '$out' -f")
        }

        findViewById<Button>(R.id.btnRebuild).setOnClickListener {
            val path = etPath.text.toString().trim()
            if (path.isEmpty()) return@setOnClickListener
            val out = "${path}_modded.apk"
            sendTermuxCommand("java -jar /sdcard/APK_Studio/apktool.jar b '$path' -o '$out'")
        }

        findViewById<Button>(R.id.btnSign).setOnClickListener {
            val path = etPath.text.toString().trim()
            if (path.isEmpty()) return@setOnClickListener
            sendTermuxCommand("java -jar /sdcard/APK_Studio/uber-signer.jar --apks '$path'")
        }

        findViewById<Button>(R.id.btnVerify).setOnClickListener {
            val path = etPath.text.toString().trim()
            if (path.isEmpty()) return@setOnClickListener
            sendTermuxCommand("java -jar /sdcard/APK_Studio/uber-signer.jar --verify --apks '$path'")
        }

        findViewById<Button>(R.id.btnInstallFw).setOnClickListener {
            val path = etPath.text.toString().trim()
            if (path.isEmpty()) return@setOnClickListener
            sendTermuxCommand("java -jar /sdcard/APK_Studio/apktool.jar if '$path'")
        }

        findViewById<Button>(R.id.btnInstall).setOnClickListener {
            val path = etPath.text.toString().trim()
            if (path.isEmpty()) return@setOnClickListener
            sendTermuxCommand("termux-open '$path'")
        }
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }
    }

    private fun copyUriToWorkDir(uri: Uri): String {
        val dir = File("/sdcard/APK_Studio")
        if (!dir.exists()) dir.mkdirs()
        
        val destFile = File(dir, "target_app.apk")
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
        return destFile.absolutePath
    }

    private fun sendTermuxCommand(command: String) {
        appendLog("\n[EXEC -> TERMUX] > $command")
        try {
            val intent = Intent("com.termux.RUN_COMMAND").apply {
                setClassName("com.termux", "com.termux.app.RunCommandService")
                putExtra("com.termux.execute.path", "/data/data/com.termux/files/usr/bin/bash")
                putExtra("com.termux.execute.arguments", arrayOf("-c", command))
                putExtra("com.termux.execute.background", true)
            }
            startService(intent)
            Toast.makeText(this, "تم إرسال الأمر إلى Termux", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            appendLog("[ERROR] فشل الاتصال بـ Termux: ${e.message}")
        }
    }

    private fun appendLog(text: String) {
        runOnUiThread {
            tvConsole.append("$text\n")
            scrollConsole.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }
}
