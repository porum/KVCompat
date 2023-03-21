package io.github.porum.kvcompat.logger

import io.github.porum.kvcompat.KVCompat

object LogUtils {

  private val logger: ILogger
    get() = KVCompat.getLogger() ?: ILogger

  @JvmStatic
  fun d(tag: String, message: String) {
    logger.d(tag, message)
  }

  @JvmStatic
  fun i(tag: String, message: String) {
    logger.i(tag, message)
  }

  @JvmStatic
  fun w(tag: String, message: String) {
    logger.w(tag, message)
  }

  @JvmStatic
  fun w(tag: String, message: String, throwable: Throwable) {
    logger.w(tag, message, throwable)
  }

  @JvmStatic
  fun e(tag: String, message: String) {
    logger.e(tag, message)
  }

  @JvmStatic
  fun e(tag: String, message: String, throwable: Throwable) {
    logger.e(tag, message, throwable)
  }
}