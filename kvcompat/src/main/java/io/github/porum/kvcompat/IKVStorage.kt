package io.github.porum.kvcompat

import android.content.SharedPreferences

/**
 * Created by panda on 2021/4/2 18:25
 */
interface IKVStorage : SharedPreferences, SharedPreferences.Editor {
  val name: String
  val supportMultiProcess: Boolean
  fun setKVStorageEditorCallback(callback: IKVEditorCallback)
  fun importFromSharedPreferences(sharedPreferences: SharedPreferences): Int
  fun getInt(key: String): Int
  fun getLong(key: String): Long
  fun getFloat(key: String): Float
  fun getBoolean(key: String): Boolean
  fun getString(key: String): String
  fun getStringSet(key: String): Set<String>?
}

interface IKVEditorCallback {
  fun onPutString(module: String, key: String, value: String)
  fun onPutStringSet(module: String, key: String, values: Set<String>)
}

interface IKVModuleInitCallback {
  fun onStartInit(module: String, supportMultiProcess: Boolean)
  fun onFinishInit(module: String, supportMultiProcess: Boolean, isSuccess: Boolean)
}
