package io.github.porum.kvcompat.impl

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import com.tencent.mmkv.MMKV
import io.github.porum.kvcompat.BuildConfig
import io.github.porum.kvcompat.IKVEditorCallback
import io.github.porum.kvcompat.IKVStorage
import io.github.porum.kvcompat.logger.LogUtils
import java.util.concurrent.CopyOnWriteArrayList


/**
 * Created by panda on 2021/4/2 18:43
 */
class MMKVStorage(
  private val mmkv: MMKV,
  override val name: String,
  override val supportMultiProcess: Boolean,
) : IKVStorage {
  companion object {
    private const val TAG = "MMKVStorage"
  }

  private val changedListeners = CopyOnWriteArrayList<OnSharedPreferenceChangeListener>()

  private var editorCallback: IKVEditorCallback? = null

  override fun setKVStorageEditorCallback(callback: IKVEditorCallback) {
    editorCallback = callback
  }

  @Suppress("UNCHECKED_CAST")
  override fun importFromSharedPreferences(sharedPreferences: SharedPreferences): Int {
    val allMap = sharedPreferences.all
    if (allMap.isNullOrEmpty()) {
      return 0
    }

    allMap.forEach {
      val key: String? = it.key
      val value: Any? = it.value
      if (key != null && value != null) {
        when (value) {
          is Int -> putInt(key, value)
          is Long -> putLong(key, value)
          is Float -> putFloat(key, value)
          is Double -> putDouble(key, value)
          is Boolean -> putBoolean(key, value)
          is String -> putString(key, value)
          is Set<*> -> putStringSet(key, value as Set<String>?)
          else -> {
            val ex =
              UnsupportedOperationException("importFromSharedPreferences unknown type: ${value.javaClass}")
            if (!BuildConfig.DEBUG) {
              LogUtils.e(TAG, "unsupported", ex)
            } else {
              throw ex
            }
          }
        }
      }
    }

    return allMap.size
  }

  override fun getAll(): MutableMap<String, *> {
    val keys: Array<String>? = mmkv.allKeys()
    if (keys.isNullOrEmpty()) {
      return hashMapOf<String, Any>()
    }

    val allMap = hashMapOf<String, Any?>()
    for (key in keys) {
      val separatorIndex = key.lastIndexOf('@')
      if (separatorIndex == -1 || separatorIndex == key.length - 1) {
        continue
      }

      when (val type = key.substring(separatorIndex + 1)) {
        Int::class.simpleName -> allMap[key] = getInt(key, 0)
        Long::class.simpleName -> allMap[key] = getLong(key, 0L)
        Float::class.simpleName -> allMap[key] = getFloat(key, 0f)
        Boolean::class.simpleName -> allMap[key] = getBoolean(key, false)
        String::class.simpleName -> allMap[key] = getString(key, "")
        Set::class.simpleName -> allMap[key] = getStringSet(key, hashSetOf())
        else -> {
          val exception = UnsupportedOperationException("getAll unknown type: $type")
          if (!BuildConfig.DEBUG) {
            LogUtils.e(TAG, "unsupported", exception)
          } else {
            throw exception
          }
        }
      }
    }

    return allMap
  }

  override fun getInt(key: String): Int {
    try {
      return mmkv.decodeInt(getTypedKey<Int>(key))
    } catch (th: Throwable) {
      LogUtils.e(TAG, "get kv error. key=$key", th)
    }
    return 0
  }

  override fun getInt(key: String, defValue: Int): Int {
    try {
      return mmkv.decodeInt(getTypedKey<Int>(key), defValue)
    } catch (th: Throwable) {
      LogUtils.e(TAG, "get kv error. key=$key", th)
    }
    return defValue
  }

  override fun getLong(key: String): Long {
    try {
      return mmkv.decodeLong(getTypedKey<Long>(key))
    } catch (th: Throwable) {
      LogUtils.e(TAG, "get kv error. key=$key", th)
    }
    return 0L
  }

  override fun getLong(key: String, defValue: Long): Long {
    try {
      return mmkv.decodeLong(getTypedKey<Long>(key), defValue)
    } catch (th: Throwable) {
      LogUtils.e(TAG, "get kv error. key=$key", th)
    }
    return defValue
  }

  override fun getFloat(key: String): Float {
    try {
      return mmkv.decodeFloat(getTypedKey<Float>(key))
    } catch (th: Throwable) {
      LogUtils.e(TAG, "get kv error. key=$key", th)
    }
    return 0F
  }

  override fun getFloat(key: String, defValue: Float): Float {
    try {
      return mmkv.decodeFloat(getTypedKey<Float>(key), defValue)
    } catch (th: Throwable) {
      LogUtils.e(TAG, "get kv error. key=$key", th)
    }
    return defValue
  }

  override fun getBoolean(key: String): Boolean {
    try {
      return mmkv.decodeBool(getTypedKey<Boolean>(key))
    } catch (th: Throwable) {
      LogUtils.e(TAG, "get kv error. key=$key", th)
    }
    return false
  }

  override fun getBoolean(key: String, defValue: Boolean): Boolean {
    try {
      return mmkv.decodeBool(getTypedKey<Boolean>(key), defValue)
    } catch (th: Throwable) {
      LogUtils.e(TAG, "get kv error. key=$key", th)
    }
    return defValue
  }

  override fun getString(key: String): String {
    try {
      return mmkv.decodeString(getTypedKey<String>(key)) ?: ""
    } catch (th: Throwable) {
      LogUtils.e(TAG, "get kv error. key=$key", th)
    }
    return ""
  }

  override fun getString(key: String, defValue: String?): String? {
    try {
      return mmkv.decodeString(getTypedKey<String>(key), defValue)
    } catch (th: Throwable) {
      LogUtils.e(TAG, "get kv error. key=$key", th)
    }
    return defValue
  }

  override fun getStringSet(key: String): Set<String>? {
    try {
      return mmkv.decodeStringSet(getTypedKey<Set<String>>(key))
    } catch (th: Throwable) {
      LogUtils.e(TAG, "get kv error. key=$key", th)
    }
    return null
  }

  override fun getStringSet(key: String, defValues: MutableSet<String>?): Set<String>? {
    try {
      return mmkv.decodeStringSet(getTypedKey<Set<String>>(key), defValues)
    } catch (th: Throwable) {
      LogUtils.e(TAG, "get kv error. key=$key", th)
    }
    return hashSetOf()
  }

  override fun contains(key: String): Boolean {
    try {
      return mmkv.contains(getRealKey(key))
    } catch (th: Throwable) {
      LogUtils.e(TAG, "contains key=$key", th)
    }
    return false
  }

  override fun edit(): SharedPreferences.Editor {
    return this
  }

  override fun registerOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener) {
    changedListeners.add(listener)
  }

  override fun unregisterOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener) {
    changedListeners.remove(listener)
  }

  override fun putString(key: String, value: String?): SharedPreferences.Editor {
    value?.let { notifyPutString(key, it) }
    try {
      val typedKey = getTypedKey<String>(key)
      mmkv.putString(typedKey, value)
      notifyChanged(typedKey)
    } catch (th: Throwable) {
      LogUtils.e(TAG, "put kv error. key=$key, value=$value", th)
    }
    return this
  }

  override fun putStringSet(key: String, values: Set<String>?): SharedPreferences.Editor {
    values?.let { notifyPutStringSet(key, it) }
    try {
      val typedKey = getTypedKey<Set<String>>(key)
      mmkv.putStringSet(typedKey, values)
      notifyChanged(typedKey)
    } catch (th: Throwable) {
      LogUtils.e(TAG, "put kv error. key=$key, values=$values", th)
    }
    return this
  }

  override fun putInt(key: String, value: Int): SharedPreferences.Editor {
    try {
      val typedKey = getTypedKey<Int>(key)
      mmkv.putInt(typedKey, value)
      notifyChanged(typedKey)
    } catch (th: Throwable) {
      LogUtils.e(TAG, "put kv error. key=$key, value=$value", th)
    }
    return this
  }

  override fun putLong(key: String, value: Long): SharedPreferences.Editor {
    try {
      val typedKey = getTypedKey<Long>(key)
      mmkv.putLong(typedKey, value)
      notifyChanged(typedKey)
    } catch (th: Throwable) {
      LogUtils.e(TAG, "put kv error. key=$key, value=$value", th)
    }
    return this
  }

  override fun putFloat(key: String, value: Float): SharedPreferences.Editor {
    try {
      val typedKey = getTypedKey<Float>(key)
      mmkv.putFloat(typedKey, value)
      notifyChanged(typedKey)
    } catch (th: Throwable) {
      LogUtils.e(TAG, "put kv error. key=$key, value=$value", th)
    }
    return this
  }

  override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor {
    try {
      val typedKey = getTypedKey<Boolean>(key)
      mmkv.putBoolean(typedKey, value)
      notifyChanged(typedKey)
    } catch (th: Throwable) {
      LogUtils.e(TAG, "put kv error. key=$key, value=$value", th)
    }
    return this
  }

  fun putDouble(key: String, value: Double): SharedPreferences.Editor {
    try {
      val typedKey = getTypedKey<Double>(key)
      mmkv.encode(typedKey, value)
      notifyChanged(typedKey)
    } catch (th: Throwable) {
      LogUtils.e(TAG, "put kv error. key=$key, value=$value", th)
    }
    return this
  }

  override fun remove(key: String): SharedPreferences.Editor {
    try {
      mmkv.remove(getRealKey(key))
      notifyChanged(key)
    } catch (th: Throwable) {
      LogUtils.e(TAG, "remove key=$key", th)
    }
    return this
  }

  override fun clear(): SharedPreferences.Editor {
    try {
      mmkv.clear()
    } catch (th: Throwable) {
      LogUtils.e(TAG, "clear", th)
    }
    return this
  }

  override fun commit(): Boolean {
    try {
      return mmkv.commit()
    } catch (th: Throwable) {
      LogUtils.e(TAG, "commit", th)
    }
    return false
  }

  override fun apply() {
    try {
      mmkv.apply()
    } catch (th: Throwable) {
      LogUtils.e(TAG, "apply", th)
    }
  }

  private inline fun <reified T> getTypedKey(key: String): String {
    val type = "@" + T::class.simpleName
    return if (key.endsWith(type)) key else key + type
  }

  private fun getRealKey(key: String): String {
    val typedKeys = listOf(
      getTypedKey<Int>(key),
      getTypedKey<Long>(key),
      getTypedKey<Float>(key),
      getTypedKey<Boolean>(key),
      getTypedKey<String>(key),
      getTypedKey<Set<String>>(key),
    )

    val typedKey = typedKeys.find { mmkv.containsKey(it) }
    if (typedKey != null) {
      return typedKey
    }
    return ""
  }

  private fun notifyPutString(key: String, values: String) {
    editorCallback?.onPutString(name, key, values)
  }

  private fun notifyPutStringSet(key: String, values: Set<String>) {
    editorCallback?.onPutStringSet(name, key, values)
  }

  private fun notifyChanged(key: String) {
    for (listener in changedListeners) {
      listener.onSharedPreferenceChanged(this, key)
    }
  }
}