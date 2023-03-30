package io.github.porum.kvcompat;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.tencent.mmkv.MMKV;
import com.tencent.mmkv.MMKVLogLevel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.github.porum.kvcompat.impl.DummyKVStorage;
import io.github.porum.kvcompat.impl.MMKVStorage;
import io.github.porum.kvcompat.impl.SPStorage;
import io.github.porum.kvcompat.utils.AppUtils;
import io.github.porum.kvcompat.logger.LogUtils;
import io.github.porum.kvcompat.utils.PreConditions;

/**
 * Created by panda on 2021/4/6 15:26
 */
public class KVCompat {
  private static final String TAG = "KVCompat";

  private static volatile boolean isInit = false;
  private static int retryCount = 0;

  private static final Map<String, IKVStorage> instanceMap = new ConcurrentHashMap<>();

  private static final ArrayList<IKVModuleInitCallback> kvModuleInitCallbacks = new ArrayList<>();
  private static final ArrayList<IKVEditorCallback> kvEditorCallbacks = new ArrayList<>();

  private static final IKVEditorCallback kvEditorCallback = new IKVEditorCallback() {

    @Override
    public void onPutString(@NotNull String module, @NotNull String key, @NotNull String value) {
      IKVEditorCallback[] callbacks = collectKVEditorCallbacks();
      if (callbacks != null) {
        for (IKVEditorCallback callback : callbacks) {
          callback.onPutString(module, key, value);
        }
      }
    }

    @Override
    public void onPutStringSet(@NotNull String module, @NotNull String key, @NotNull Set<String> values) {
      IKVEditorCallback[] callbacks = collectKVEditorCallbacks();
      if (callbacks != null) {
        for (IKVEditorCallback callback : callbacks) {
          callback.onPutStringSet(module, key, values);
        }
      }
    }
  };

  private static Config sConfig;

  public static void setConfig(Config config) {
    sConfig = config;
  }

  public static Config getConfig() {
    if (sConfig == null) {
      sConfig = new Config.Builder().build();
    }
    return sConfig;
  }

  public static IKVStorage moduleOfMainProcess(Context context, String module) {
    if (!AppUtils.isMainProcess(context)) {
      String errMsg = "method moduleOfMainProcess can not to be called in sub process, module: " + module + ", process: " + AppUtils.getCurrentProcessName(context);
      if (!BuildConfig.DEBUG) {
        LogUtils.e(TAG, errMsg);
      } else {
        throw new UnsupportedOperationException(errMsg);
      }
    }
    return module(context, module, false);
  }

  public static IKVStorage moduleAppendProcessName(Context context, String module) {
    String processName = AppUtils.getCurrentProcessName(context);
    if (!TextUtils.isEmpty(processName)) {
      String[] splits = processName.split(":");
      if (splits.length == 2) {
        return module(context, module + "-" + splits[1], false);
      }
    }
    return module(context, module + "-main", false);
  }

  public static IKVStorage module(Context context, String module, boolean supportMultiProcess) {
    LogUtils.i(TAG, "module call, module: " + module + ", supportMultiProcess: " + supportMultiProcess);

    if (instanceMap.containsKey(module)) {
      return instanceMap.get(module);
    }

    if (!PreConditions.checkNotNull(context, "KVStorage init failed due to context is null.")) {
      IKVStorage dummyStorage = new DummyKVStorage(module, supportMultiProcess);
      instanceMap.put(module, dummyStorage);
      return dummyStorage;
    }

    notifyStartInit(module, supportMultiProcess);

    IKVStorage storage;
    MMKV mmkv = null;
    boolean isSuccess = true;
    if (retryInit(context)) {
      try {
        mmkv = MMKV.mmkvWithID(module, supportMultiProcess ? MMKV.MULTI_PROCESS_MODE : MMKV.SINGLE_PROCESS_MODE);
      } catch (Throwable th) {
        isSuccess = false;
        LogUtils.e(TAG, "call mmkvwithID occur error.", th);
      }
    }
    if (mmkv != null) {
      storage = new MMKVStorage(mmkv, module, supportMultiProcess);
      storage.setKVStorageEditorCallback(kvEditorCallback);
      instanceMap.put(module, storage);
    } else {
      SharedPreferences prefs = context.getSharedPreferences(module, Context.MODE_PRIVATE);
      storage = new SPStorage(prefs, module, false);
      instanceMap.put(module, storage);
    }


    notifyFinishInit(module, supportMultiProcess, isSuccess);
    return storage;
  }

  private static boolean retryInit(Context context) {
    while (!isInit && retryCount < getConfig().getMaxRetryInitCount()) {
      initMMKV(context);
      retryCount++;
    }
    return isInit;
  }

  private static void initMMKV(Context context) {
    if (!isInit) {
      synchronized (KVCompat.class) {
        if (!isInit) {
          try {
            String dir = context.getFilesDir().getAbsolutePath() + "/mmkv";
            String rootDir = MMKV.initialize(context, dir, null, MMKVLogLevel.LevelInfo, new KVHandler(instanceMap));
            LogUtils.i(TAG, "init mmkv success, version: " + MMKV.version() + ", root: " + rootDir);
            isInit = true;
          } catch (Throwable th) {
            LogUtils.e(TAG, "init mmkv failed.", th);
          }
        }
      }
    }
  }

  private static void notifyStartInit(String module, boolean supportMultiProcess) {
    IKVModuleInitCallback[] callbacks = collectModuleInitCallbacks();
    if (callbacks != null) {
      for (IKVModuleInitCallback callback : callbacks) {
        callback.onStartInit(module, supportMultiProcess);
      }
    }
  }

  private static void notifyFinishInit(String module, boolean supportMultiProcess, boolean isSuccess) {
    IKVModuleInitCallback[] callbacks = collectModuleInitCallbacks();
    if (callbacks != null) {
      for (IKVModuleInitCallback callback : callbacks) {
        callback.onFinishInit(module, supportMultiProcess, isSuccess);
      }
    }
  }

  private static IKVModuleInitCallback[] collectModuleInitCallbacks() {
    IKVModuleInitCallback[] callbacks = null;
    synchronized (kvModuleInitCallbacks) {
      if (!kvModuleInitCallbacks.isEmpty()) {
        callbacks = kvModuleInitCallbacks.toArray(new IKVModuleInitCallback[0]);
      }
    }
    return callbacks;
  }

  private static IKVEditorCallback[] collectKVEditorCallbacks() {
    IKVEditorCallback[] callbacks = null;
    synchronized (kvEditorCallbacks) {
      if (!kvEditorCallbacks.isEmpty()) {
        callbacks = kvEditorCallbacks.toArray(new IKVEditorCallback[0]);
      }
    }
    return callbacks;
  }

  public static void addKVEditorCallback(IKVEditorCallback callback) {
    synchronized (kvEditorCallbacks) {
      kvEditorCallbacks.add(callback);
    }
  }

  public static void removeKVEditorCallback(IKVEditorCallback callback) {
    synchronized (kvEditorCallbacks) {
      kvEditorCallbacks.remove(callback);
    }
  }

  public static void addKVModuleInitCallback(IKVModuleInitCallback callback) {
    synchronized (kvModuleInitCallbacks) {
      kvModuleInitCallbacks.add(callback);
    }
  }

  public static Map<String, IKVStorage> getInstanceMap() {
    return instanceMap;
  }
}
