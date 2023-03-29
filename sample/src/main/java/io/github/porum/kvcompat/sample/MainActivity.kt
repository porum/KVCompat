package io.github.porum.kvcompat.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.widget.AppCompatButton
import io.github.porum.kvcompat.KVCompat

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val authorEt = findViewById<EditText>(R.id.authorEt)
    val projectEt = findViewById<EditText>(R.id.projectEt)
    val repositoryEt = findViewById<EditText>(R.id.repositoryEt)

    val config = KVCompat.moduleOfMainProcess(this, "config")
    config.putString("version", "1.0.0")

    val global = KVCompat.moduleOfMainProcess(this, "global")
    global.putString("author", authorEt.text.toString())
    global.putString("project", projectEt.text.toString())
    global.putString("repository", repositoryEt.text.toString())

    authorEt.setOnEditorActionListener { v, actionId, event ->
      global.putString("author", v.text.toString())
      return@setOnEditorActionListener true
    }
    projectEt.setOnEditorActionListener { v, actionId, event ->
      global.putString("project", v.text.toString())
      return@setOnEditorActionListener true
    }
    repositoryEt.setOnEditorActionListener { v, actionId, event ->
      global.putString("repository", v.text.toString())
      return@setOnEditorActionListener true
    }

    findViewById<AppCompatButton>(R.id.module1).setOnClickListener {
      val module1 = KVCompat.moduleOfMainProcess(this, "module1")
      module1.putString("key1", "value1")
    }
    findViewById<AppCompatButton>(R.id.module2).setOnClickListener {
      val module2 = KVCompat.moduleOfMainProcess(this, "module2")
      module2.putInt("key1", 666)
      module2.putLong("key2", System.currentTimeMillis())
      module2.putFloat("key3", 3.14F)
      module2.putBoolean("key4", true)
      module2.putString("key5", "this is module1")
      module2.putStringSet("key6", setOf("str0", "str1", "str2"))
    }
  }

}