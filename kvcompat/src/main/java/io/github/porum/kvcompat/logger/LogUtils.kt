package io.github.porum.kvcompat.logger

import io.github.porum.kvcompat.KVCompat

internal object LogUtils {

  private val logger: ILogger
    get() = KVCompat.config.logger

  private val logLevel: LogLevel
    get() = KVCompat.config.logLevel

  @JvmStatic
  fun d(tag: String, message: String) {
    if (LogLevel.DEBUG.ordinal >= logLevel.ordinal) {
      logger.d(tag, message)
    }
  }

  @JvmStatic
  fun i(tag: String, message: String) {
    if (LogLevel.INFO.ordinal >= logLevel.ordinal) {
      logger.i(tag, message)
    }
  }

  @JvmStatic
  fun w(tag: String, message: String) {
    if (LogLevel.WARN.ordinal >= logLevel.ordinal) {
      logger.w(tag, message)
    }
  }

  @JvmStatic
  fun w(tag: String, message: String, throwable: Throwable) {
    if (LogLevel.WARN.ordinal >= logLevel.ordinal) {
      logger.w(tag, message, throwable)
    }
  }

  @JvmStatic
  fun e(tag: String, message: String) {
    if (LogLevel.ERROR.ordinal >= logLevel.ordinal) {
      logger.e(tag, message)
    }
  }

  @JvmStatic
  fun e(tag: String, message: String, throwable: Throwable) {
    if (LogLevel.ERROR.ordinal >= logLevel.ordinal) {
      logger.e(tag, message, throwable)
    }
  }
}