package io.github.porum.kvcompat.logger

import com.tencent.mmkv.MMKVLogLevel

enum class LogLevel {
  DEBUG, INFO, WARN, ERROR, NONE
}

internal fun LogLevel.getMMKVLogLevel(): MMKVLogLevel {
  return when (this) {
    LogLevel.DEBUG -> MMKVLogLevel.LevelDebug
    LogLevel.INFO -> MMKVLogLevel.LevelInfo
    LogLevel.WARN -> MMKVLogLevel.LevelWarning
    LogLevel.ERROR -> MMKVLogLevel.LevelError
    LogLevel.NONE -> MMKVLogLevel.LevelNone
  }
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