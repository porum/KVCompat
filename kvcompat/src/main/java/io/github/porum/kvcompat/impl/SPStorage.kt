package io.github.porum.kvcompat.impl

import android.content.SharedPreferences
import io.github.porum.kvcompat.IKVEditorCallback
import io.github.porum.kvcompat.IKVStorage

/**
 * Created by panda on 2021/4/2 18:38
 */
internal class SPStorage(
  private val sp: SharedPreferences,
  override val name: String,
  override val supportMultiProcess: Boolean,
) : IKVStorage {

  private var editorCallback: IKVEditorCallback? = null

  override fun setKVStorageEditorCallback(callback: IKVEditorCallback) {
    editorCallback = callback
  }

  override fun importFromSharedPreferences(sharedPreferences: SharedPreferences): Int {
    return 0
  }

  override fun getAll(): MutableMap<String, *> {
    return sp.all ?: hashMapOf<String, Any>()
  }

  override fun getInt(key: String): Int {
    return sp.getInt(key, 0)
  }

  override fun getInt(key: String, defValue: Int): Int {
    return sp.getInt(key, defValue)
  }

  override fun getLong(key: String): Long {
    return sp.getLong(key, 0L)
  }

  override fun getLong(key: String, defValue: Long): Long {
    return sp.getLong(key, defValue)
  }

  override fun getFloat(key: String): Float {
    return sp.getFloat(key, 0F)
  }

  override fun getFloat(key: String, defValue: Float): Float {
    return sp.getFloat(key, defValue)
  }

  override fun getBoolean(key: String): Boolean {
    return sp.getBoolean(key, false)
  }

  override fun getBoolean(key: String, defValue: Boolean): Boolean {
    return sp.getBoolean(key, defValue)
  }

  override fun getString(key: String): String {
    return sp.getString(key, "") ?: ""
  }

  override fun getString(key: String, defValue: String?): String? {
    return sp.getString(key, defValue)
  }

  override fun getStringSet(key: String): Set<String>? {
    return sp.getStringSet(key, hashSetOf())
  }

  override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? {
    return sp.getStringSet(key, defValues)
  }

  override fun contains(key: String): Boolean {
    return sp.contains(key)
  }

  override fun edit(): SharedPreferences.Editor {
    return sp.edit()
  }

  override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
    return sp.registerOnSharedPreferenceChangeListener(listener)
  }

  override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
    return sp.unregisterOnSharedPreferenceChangeListener(listener)
  }

  override fun putString(key: String, value: String?): SharedPreferences.Editor {
    value?.let { notifyPutString(key, it) }
    return sp.edit().putString(key, value)
  }

  override fun putStringSet(key: String, values: MutableSet<String>?): SharedPreferences.Editor {
    values?.let { notifyPutStringSet(key, it) }
    return sp.edit().putStringSet(key, values)
  }

  override fun putInt(key: String, value: Int): SharedPreferences.Editor {
    return sp.edit().putInt(key, value)
  }

  override fun putLong(key: String, value: Long): SharedPreferences.Editor {
    return sp.edit().putLong(key, value)
  }

  override fun putFloat(key: String, value: Float): SharedPreferences.Editor {
    return sp.edit().putFloat(key, value)
  }

  override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor {
    return sp.edit().putBoolean(key, value)
  }

  override fun remove(key: String): SharedPreferences.Editor {
    return sp.edit().remove(key)
  }

  override fun clear(): SharedPreferences.Editor {
    return sp.edit().clear()
  }

  override fun commit(): Boolean {
    return sp.edit().commit()
  }

  override fun apply() {
    sp.edit().apply()
  }

  private fun notifyPutString(key: String, values: String) {
    editorCallback?.onPutString(name, key, values)
  }

  private fun notifyPutStringSet(key: String, values: Set<String>) {
    editorCallback?.onPutStringSet(name, key, values)
  }
}