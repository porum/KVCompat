package io.github.porum.kvcompat;

import android.os.Looper;
import android.os.SystemClock;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import io.github.porum.kvcompat.logger.LogUtils;

/**
 * Created by panda on 2021/4/8 10:46
 */
public class KVColdStartMonitor {
  private static final String TAG = "KVColdStartMonitor";
  private static long startInitTime;
  private static final Map<String, Long> moduleInitTimeMap = new LinkedHashMap<>();
  private static final IKVModuleInitCallback kvModuleInitCallback = new IKVModuleInitCallback() {
    @Override
    public void onStartInit(@NotNull String module, boolean supportMultiProcess) {
      if (isNewModuleOnMainThread(module)) {
        startInitTime = SystemClock.elapsedRealtime();
      }
    }

    @Override
    public void onFinishInit(@NotNull String module, boolean supportMultiProcess, boolean isSuccess) {
      if (isNewModuleOnMainThread(module)) {
        if (startInitTime > 0) {
          long cost = SystemClock.elapsedRealtime() - startInitTime;
          if (isSuccess) {
            moduleInitTimeMap.put(module, cost);
          }
          LogUtils.i(TAG, "init kv storage cost " + cost + "ms, module: " + module
              + ", supportMultiProcess: " + supportMultiProcess + ", isSuccess: " + isSuccess);
        }
        startInitTime = 0;
      }
    }
  };
  private static final IKVEditorCallback kvEditorCallback = new IKVEditorCallback() {
    @Override
    public void onPutString(@NotNull String module, @NotNull String key, @NotNull String value) {
      if (value.length() > 150) {
        LogUtils.w(TAG, "onPutString value too large, module: " + module + ", key: " + key + ", value: " + value);
      }
    }

    @Override
    public void onPutStringSet(@NotNull String module, @NotNull String key, @NotNull Set<String> values) {
      for (String str : values) {
        if (str.length() > 150) {
          LogUtils.w(TAG, "onPutStringSet value too large, module: " + module + ", key: " + key + ", value: " + str);
        }
      }
    }
  };

  public static void startMonitor() {
    KVCompat.addKVModuleInitCallback(kvModuleInitCallback);
    KVCompat.addKVEditorCallback(kvEditorCallback);
  }

  private static boolean isNewModuleOnMainThread(String module) {
    return Looper.myLooper() == Looper.getMainLooper() && !moduleInitTimeMap.containsKey(module);
  }
}
