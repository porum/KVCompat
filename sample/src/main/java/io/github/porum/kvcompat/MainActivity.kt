package io.github.porum.kvcompat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val global = KVCompat.moduleOfMainProcess(this, "global")
    global.putString("author", "porum")
    global.putString("project", "KVCompat")
    global.putString("url", "https://github.com/porum/KVCompat")

    val config = KVCompat.moduleOfMainProcess(this, "config")
    val version = config.getString("version")
    if (version.isEmpty()) {
      config.putString("version", "1.0.0")
    }
    findViewById<TextView>(R.id.textView).text = config.getString("version")
  }
}