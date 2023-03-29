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
    val versionEt = findViewById<EditText>(R.id.versionEt)
    val repositoryEt = findViewById<EditText>(R.id.repositoryEt)

    val info = KVCompat.moduleOfMainProcess(this, "info")
    info.putString("author", authorEt.text.toString())
    info.putString("project", projectEt.text.toString())
    info.putString("version", versionEt.text.toString())
    info.putString("repository", repositoryEt.text.toString())

    authorEt.setOnEditorActionListener { v, actionId, event ->
      info.putString("author", v.text.toString())
      return@setOnEditorActionListener true
    }
    projectEt.setOnEditorActionListener { v, actionId, event ->
      info.putString("project", v.text.toString())
      return@setOnEditorActionListener true
    }
    repositoryEt.setOnEditorActionListener { v, actionId, event ->
      info.putString("repository", v.text.toString())
      return@setOnEditorActionListener true
    }

    findViewById<AppCompatButton>(R.id.another).setOnClickListener {
      val another = KVCompat.moduleOfMainProcess(this, "another")
      another.putInt("key1", 666)
      another.putLong("key2", System.currentTimeMillis())
      another.putFloat("key3", 3.14F)
      another.putBoolean("key4", true)
      another.putString("key5", "this is another module")
      another.putStringSet("key6", setOf("str0", "str1", "str2"))
    }
  }

}