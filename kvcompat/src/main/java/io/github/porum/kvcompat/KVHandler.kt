package io.github.porum.kvcompat

import com.tencent.mmkv.MMKVHandler
import com.tencent.mmkv.MMKVLogLevel
import com.tencent.mmkv.MMKVRecoverStrategic
import io.github.porum.kvcompat.logger.LogUtils

private const val TAG = "KVCompat.Handler"

private const val RECOVER_CRC_CHECK_FAIL = 1
private const val RECOVER_FILE_LENGTH_ERROR = 2

internal class KVHandler(private val instanceMap: Map<String, IKVStorage>) : MMKVHandler {

  override fun onMMKVCRCCheckFail(mmapID: String): MMKVRecoverStrategic {
    trackMMKVRecover(mmapID, RECOVER_CRC_CHECK_FAIL)
    return KVCompat.config.crcCheckFailStrategic
  }

  override fun onMMKVFileLengthError(mmapID: String): MMKVRecoverStrategic {
    trackMMKVRecover(mmapID, RECOVER_FILE_LENGTH_ERROR)
    return KVCompat.config.fileLengthErrorStrategic
  }

  override fun wantLogRedirecting() = true

  override fun mmkvLog(
    level: MMKVLogLevel?,
    file: String?,
    line: Int,
    function: String?,
    message: String?
  ) {
    val log = "<$file:$line::$function> $message"
    when (level) {
      MMKVLogLevel.LevelDebug -> LogUtils.d(TAG, log)
      MMKVLogLevel.LevelInfo -> LogUtils.i(TAG, log)
      MMKVLogLevel.LevelWarning -> LogUtils.w(TAG, log)
      MMKVLogLevel.LevelError -> LogUtils.e(TAG, log)
      MMKVLogLevel.LevelNone -> {}
      else -> {}
    }
  }

  private fun trackMMKVRecover(module: String, type: Int) {
    var supportMultiProcess: Boolean
    synchronized(instanceMap) {
      supportMultiProcess = instanceMap[module]?.supportMultiProcess ?: return
    }
    if (type == RECOVER_CRC_CHECK_FAIL) {
      LogUtils.e(TAG, "onMMKVCRCCheckFail, module: $module, supportMultiProcess: $supportMultiProcess")
    } else if (type == RECOVER_FILE_LENGTH_ERROR) {
      LogUtils.e(TAG, "onMMKVFileLengthError, module: $module, supportMultiProcess: $supportMultiProcess")
    }
  }
}