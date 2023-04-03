package io.github.porum.kvcompat

import com.tencent.mmkv.MMKVRecoverStrategic
import io.github.porum.kvcompat.logger.ILogger
import io.github.porum.kvcompat.logger.LogLevel

class Config internal constructor(
  val logger: ILogger,
  val logLevel: LogLevel,
  val enableThrow: Boolean,
  val maxRetryInitCount: Int,
  val crcCheckFailStrategic: MMKVRecoverStrategic,
  val fileLengthErrorStrategic: MMKVRecoverStrategic
) {

  open class Builder {
    private var logger: ILogger = ILogger.DEFAULT
    private var logLevel: LogLevel = LogLevel.INFO
    private var enableThrow: Boolean = false
    private var maxRetryInitCount: Int = 3
    private var crcCheckFailStrategic: MMKVRecoverStrategic = MMKVRecoverStrategic.OnErrorDiscard
    private var fileLengthErrorStrategic: MMKVRecoverStrategic = MMKVRecoverStrategic.OnErrorDiscard

    open fun logger(logger: ILogger): Builder = apply {
      this.logger = logger
    }

    open fun logLevel(logLevel: LogLevel): Builder = apply {
      this.logLevel = logLevel
    }

    open fun enableThrow(enableThrow: Boolean): Builder = apply {
      this.enableThrow = enableThrow
    }

    open fun maxRetryInitCount(maxRetryInitCount: Int): Builder = apply {
      this.maxRetryInitCount = maxRetryInitCount
    }

    open fun crcCheckFailStrategic(crcCheckFailStrategic: MMKVRecoverStrategic): Builder = apply {
      this.crcCheckFailStrategic = crcCheckFailStrategic
    }

    open fun fileLengthErrorStrategic(fileLengthErrorStrategic: MMKVRecoverStrategic): Builder = apply {
      this.fileLengthErrorStrategic = fileLengthErrorStrategic
    }

    open fun build(): Config {
      return Config(
        logger,
        logLevel,
        enableThrow,
        maxRetryInitCount,
        crcCheckFailStrategic,
        fileLengthErrorStrategic
      )
    }

  }

}