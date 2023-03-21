package io.github.porum.kvcompat.impl

import android.content.SharedPreferences
import io.github.porum.kvcompat.IKVStorage

/**
 * Created by panda on 2021/4/2 20:13
 */
class DummyKVStorage(
  override val name: String,
  override val supportMultiProcess: Boolean,
) : IKVStorage {
  override fun importFromSharedPreferences(sharedPreferences: SharedPreferences): Int {
    return 0
  }

  override fun getInt(key: String): Int {
    return 0
  }

  override fun getInt(key: String?, defValue: Int): Int {
    return defValue
  }

  override fun getLong(key: String): Long {
    return 0L
  }

  override fun getLong(key: String?, defValue: Long): Long {
    return defValue
  }

  override fun getFloat(key: String): Float {
    return 0F
  }

  override fun getFloat(key: String?, defValue: Float): Float {
    return defValue
  }

  override fun getBoolean(key: String): Boolean {
    return false
  }

  override fun getBoolean(key: String?, defValue: Boolean): Boolean {
    return defValue
  }

  override fun getString(key: String): String {
    return ""
  }

  override fun getString(key: String?, defValue: String?): String? {
    return defValue
  }

  override fun getStringSet(key: String): Set<String> {
    return hashSetOf()
  }

  override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? {
    return defValues
  }

  override fun getAll(): MutableMap<String, *> {
    return hashMapOf<String, Any>()
  }

  override fun contains(key: String?): Boolean {
    return false
  }

  override fun edit(): SharedPreferences.Editor {
    return this
  }

  override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
  }

  override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
  }

  override fun putString(key: String?, value: String?): SharedPreferences.Editor {
    return this
  }

  override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor {
    return this
  }

  override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
    return this
  }

  override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
    return this
  }

  override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
    return this
  }

  override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
    return this
  }

  override fun remove(key: String?): SharedPreferences.Editor {
    return this
  }

  override fun clear(): SharedPreferences.Editor {
    return this
  }

  override fun commit(): Boolean {
    return false
  }

  override fun apply() {

  }
}