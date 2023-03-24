package io.github.porum.kvcompat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText

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

  }

}