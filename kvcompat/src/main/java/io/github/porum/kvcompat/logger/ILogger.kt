package io.github.porum.kvcompat.logger

enum class LogLevel {
  DEBUG, INFO, WARN, ERROR
}

interface ILogger {
  fun d(tag: String, msg: String)
  fun i(tag: String, msg: String)
  fun w(tag: String, msg: String)
  fun w(tag: String, msg: String, throwable: Throwable)
  fun e(tag: String, msg: String)
  fun e(tag: String, msg: String, throwable: Throwable)

  companion object DEFAULT : ILogger {
    override fun d(tag: String, msg: String) {
      android.util.Log.d(tag, msg)
    }

    override fun i(tag: String, msg: String) {
      android.util.Log.i(tag, msg)
    }

    override fun w(tag: String, msg: String) {
      android.util.Log.w(tag, msg)
    }

    override fun w(tag: String, msg: String, throwable: Throwable) {
      android.util.Log.w(tag, msg, throwable)
    }

    override fun e(tag: String, msg: String) {
      android.util.Log.e(tag, msg)
    }

    override fun e(tag: String, msg: String, throwable: Throwable) {
      android.util.Log.e(tag, msg, throwable)
    }

  }
}