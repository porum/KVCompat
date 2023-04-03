package io.github.porum.kvcompat.sample

import android.app.Application
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.core.FlipperClient
import com.facebook.soloader.SoLoader
import io.github.porum.kvcompat.Config
import io.github.porum.kvcompat.IKVModuleInitCallback
import io.github.porum.kvcompat.KVCompat
import io.github.porum.kvcompat.flipper.plugin.KVCompatFlipperPlugin
import io.github.porum.kvcompat.flipper.plugin.KVStorageDescriptor
import io.github.porum.kvcompat.logger.LogLevel

class App : Application() {

  private var client: FlipperClient? = null

  override fun onCreate() {
    super.onCreate()

    KVCompat.config = Config.Builder()
      .enableThrow(false)
      .maxRetryInitCount(3)
      .logLevel(LogLevel.DEBUG)
      .build()

    SoLoader.init(this, false)
    if (BuildConfig.DEBUG && FlipperUtils.shouldEnableFlipper(this)) {
      client = AndroidFlipperClient.getInstance(this).also {
        it.addPlugin(KVCompatFlipperPlugin(this, emptyList()))
        it.start()
      }
      observeModuleInit()
    }
  }

  private fun observeModuleInit() {
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