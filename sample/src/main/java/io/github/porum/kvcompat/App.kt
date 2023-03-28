package io.github.porum.kvcompat

import android.app.Application
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.core.FlipperClient
import com.facebook.soloader.SoLoader
import io.github.porum.kvcompat.flipper.plugin.KVCompatFlipperPlugin
import io.github.porum.kvcompat.flipper.plugin.KVStorageDescriptor

class App : Application() {

  private var client: FlipperClient? = null

  override fun onCreate() {
    super.onCreate()

    SoLoader.init(this, false)
    if (BuildConfig.DEBUG && FlipperUtils.shouldEnableFlipper(this)) {
      client = AndroidFlipperClient.getInstance(this).also {
        it.addPlugin(KVCompatFlipperPlugin(this, emptyList()))
        it.start()
      }
      observeChanged()
    }
  }

  private fun observeChanged() {
    KVCompat.addKVModuleInitCallback(object : IKVModuleInitCallback {
      override fun onFinishInit(module: String, supportMultiProcess: Boolean, isSuccess: Boolean) {
        client?.getPluginByClass(KVCompatFlipperPlugin::class.java)
          ?.addDescriptor(this@App, KVStorageDescriptor(module, supportMultiProcess))
      }

      override fun onStartInit(module: String, supportMultiProcess: Boolean) {
      }
    })
  }

}