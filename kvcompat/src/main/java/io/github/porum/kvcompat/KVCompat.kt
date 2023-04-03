package io.github.porum.kvcompat

import android.content.Context
import com.tencent.mmkv.MMKV
import io.github.porum.kvcompat.impl.MMKVStorage
import io.github.porum.kvcompat.impl.SPStorage
import io.github.porum.kvcompat.logger.LogUtils
import io.github.porum.kvcompat.logger.getMMKVLogLevel
import io.github.porum.kvcompat.utils.AppUtils
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

@Suppress("SpellCheckingInspection")
object KVCompat {
  private const val TAG = "KVCompat"

  @JvmField
  var config: Config = Config.Builder().build()

  @Volatile
  private var isInit = false

  private var retryCount = 0

  private val instanceMap = ConcurrentHashMap<String, IKVStorage>()

  private val kvModuleInitCallbacks = CopyOnWriteArrayList<IKVModuleInitCallback>()
  private val kvEditorCallbacks = CopyOnWriteArrayList<IKVEditorCallback>()

  private val innerKVEditorCallback = object : IKVEditorCallback {
    override fun onPutString(module: String, key: String, value: String) {
      notifyPutString(module, key, value)
    }

    override fun onPutStringSet(module: String, key: String, values: Set<String>) {
      notifyPutStringSet(module, key, values)
    }
  }


  @JvmStatic
  fun moduleOfMainProcess(context: Context, module: String): IKVStorage {
    if (!AppUtils.isMainProcess(context)) {
      val errMsg = "moduleOfMainProcess can not to be called in sub process, " +
          "module: $module, process: ${AppUtils.getCurrentProcessName(context)}"
      if (config.enableThrow) {
        throw UnsupportedOperationException(errMsg)
      } else {
        LogUtils.e(TAG, errMsg)
      }
    }
    return module(context, module, false)
  }

  @JvmStatic
  fun moduleAppendProcessName(context: Context, module: String): IKVStorage {
    val processName = AppUtils.getCurrentProcessName(context)
    val splits = processName?.split(":".toRegex())
    if (splits?.size == 2) {
      return module(context, "$module-${splits[1]}", false)
    }
    return module(context, "$module-main", false)
  }

  @JvmStatic
  fun module(context: Context, module: String, supportMultiProcess: Boolean): IKVStorage {
    if (instanceMap.containsKey(module)) {
      return instanceMap[module]!!
    }

    notifyStartInit(module, supportMultiProcess)

    val storage: IKVStorage
    var mmkv: MMKV? = null
    var isSuccess = true
    if (retryInit(context)) {
      try {
        val mode = if (supportMultiProcess) MMKV.MULTI_PROCESS_MODE else MMKV.SINGLE_PROCESS_MODE
        mmkv = MMKV.mmkvWithID(module, mode)
      } catch (th: Throwable) {
        isSuccess = false
        LogUtils.e(TAG, "call mmkvwithID occur error.", th)
      }
    }
    if (mmkv != null) {
      storage = MMKVStorage(mmkv, module, supportMultiProcess)
      storage.setKVStorageEditorCallback(innerKVEditorCallback)
      instanceMap[module] = storage
    } else {
      val prefs = context.getSharedPreferences(module, Context.MODE_PRIVATE)
      storage = SPStorage(prefs, module, false)
      instanceMap[module] = storage
    }

    notifyFinishInit(module, supportMultiProcess, isSuccess)
    return storage
  }

  private fun retryInit(context: Context): Boolean {
    while (!isInit && retryCount < config.maxRetryInitCount) {
      initMMKV(context)
      retryCount++
    }
    return isInit
  }

  private fun initMMKV(context: Context) {
    if (!isInit) {
      synchronized(KVCompat::class.java) {
        if (!isInit) {
          try {
            val dir = context.filesDir.absolutePath + "/mmkv"
            val logLevel = config.logLevel.getMMKVLogLevel()
            val handler = KVHandler(instanceMap)
            val rootDir = MMKV.initialize(context, dir, null, logLevel, handler)
            LogUtils.d(TAG, "init mmkv success, version: ${MMKV.version()}, root: $rootDir")
            isInit = true
          } catch (th: Throwable) {
            LogUtils.e(TAG, "init mmkv failed.", th)
          }
        }
      }
    }
  }

  fun addKVEditorCallback(callback: IKVEditorCallback) {
    kvEditorCallbacks.add(callback)
  }

  fun removeKVEditorCallback(victim: IKVEditorCallback) {
    kvEditorCallbacks.remove(victim)
  }

  private fun notifyPutString(module: String, key: String, value: String) {
    for (callback in kvEditorCallbacks) {
      callback.onPutString(module, key, value)
    }
  }

  private fun notifyPutStringSet(module: String, key: String, values: Set<String>) {
    for (callback in kvEditorCallbacks) {
      callback.onPutStringSet(module, key, values)
    }
  }

  fun addKVModuleInitCallback(callback: IKVModuleInitCallback) {
    kvModuleInitCallbacks.add(callback)
  }

  fun removeKVModuleInitCallback(victim: IKVModuleInitCallback) {
    kvModuleInitCallbacks.remove(victim)
  }

  private fun notifyStartInit(module: String, supportMultiProcess: Boolean) {
    for (callback in kvModuleInitCallbacks) {
      callback.onStartInit(module, supportMultiProcess)
    }
  }

  private fun notifyFinishInit(module: String, supportMultiProcess: Boolean, isSuccess: Boolean) {
    for (callback in kvModuleInitCallbacks) {
      callback.onFinishInit(module, supportMultiProcess, isSuccess)
    }
  }

}