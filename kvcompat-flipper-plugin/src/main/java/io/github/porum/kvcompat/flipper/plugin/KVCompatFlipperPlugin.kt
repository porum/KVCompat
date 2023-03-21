package io.github.porum.kvcompat.flipper.plugin

import android.content.Context
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import com.facebook.flipper.core.FlipperConnection
import com.facebook.flipper.core.FlipperObject
import com.facebook.flipper.core.FlipperPlugin
import io.github.porum.kvcompat.IKVStorage
import io.github.porum.kvcompat.KVCompat


/**
 * Created by panda on 2023/2/9 17:59
 */
class KVCompatFlipperPlugin : FlipperPlugin {

  private var connection: FlipperConnection? = null
  private var kvStorageList: HashMap<IKVStorage, KVStorageDescriptor>

  private val onChangedListener = OnSharedPreferenceChangeListener { prefs, key ->
    if (connection == null || prefs !is IKVStorage) return@OnSharedPreferenceChangeListener
    try {
      val kvStorage = getKVStorageFor(prefs.name)
    } catch (e: IllegalStateException) {

    }
  }

  constructor(context: Context, name: String) : this(context, name, false)

  constructor(context: Context, name: String, supportMultiProcess: Boolean)
      : this(context, listOf(KVStorageDescriptor(name, supportMultiProcess)))

  constructor(context: Context, descriptors: List<KVStorageDescriptor>) {
    kvStorageList = HashMap(descriptors.size)
    for (descriptor in descriptors) {
      kvStorageList[descriptor.getKVStorage(context)] = descriptor
    }
  }

  override fun getId() = "KVCompat"

  private fun getKVStorageFor(name: String): IKVStorage {
    for ((kvStorage, descriptor) in kvStorageList.entries) {
      if (descriptor.name == name) {
        return kvStorage
      }
    }
    throw IllegalStateException("Unknown module: $name")
  }

  private fun getFlipperObjectFor(name: String) = getFlipperObjectFor(getKVStorageFor(name))

  private fun getFlipperObjectFor(kvStorage: IKVStorage): FlipperObject {
    val builder = FlipperObject.Builder()
    for ((key, value) in kvStorage.all) {
      builder.put(key, value)
    }
    return builder.build()
  }

  override fun onConnect(connection: FlipperConnection) {
    this.connection = connection

    connection.receive("getAllModules") { params, responder ->
      val builder = FlipperObject.Builder()
      for ((kvStorage, descriptor) in kvStorageList) {
        builder.put(descriptor.name, getFlipperObjectFor(kvStorage))
      }
      responder.success(builder.build())
    }

    connection.receive("getModule") { params, responder ->
      val name = params.getString("name")
      if (name != null) {
        responder.success(getFlipperObjectFor(name))
      }
    }

    connection.receive("putValue") { params, responder ->
      val module = params.getString("module")
      val key = params.getString("key")
      val kvStorage = getKVStorageFor(module)
      when (kvStorage.all[key]) {
        is Boolean -> kvStorage.putBoolean(key, params.getBoolean("value"))
        is Long -> kvStorage.putLong(key, params.getLong("value"))
        is Int -> kvStorage.putInt(key, params.getInt("value"))
        is Float -> kvStorage.putFloat(key, params.getFloat("value"))
        is String -> kvStorage.putString(key, params.getString("value"))
        else -> throw IllegalArgumentException("Type not supported: $key")
      }
      kvStorage.apply()
      responder.success(getFlipperObjectFor(module))
    }

    connection.receive("deleteKey") { params, responder ->
      val module = params.getString("module")
      val key = params.getString("key")
      val kvStorage = getKVStorageFor(module)
      kvStorage.remove(key)
      kvStorage.apply()
      responder.success(getFlipperObjectFor(module))
    }
  }

  override fun onDisconnect() {
    connection = null
  }

  override fun runInBackground() = false
}

data class KVStorageDescriptor(
  val name: String,
  val supportMultiProcess: Boolean
) {

  fun getKVStorage(context: Context): IKVStorage {
    return KVCompat.module(context, name, supportMultiProcess)
  }
}