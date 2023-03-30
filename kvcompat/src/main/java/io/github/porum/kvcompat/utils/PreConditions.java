package io.github.porum.kvcompat.utils;

import io.github.porum.kvcompat.BuildConfig;
import io.github.porum.kvcompat.logger.LogUtils;

/**
 * Created by panda on 2021/4/7 16:02
 */
public class PreConditions {
  private static final String TAG = "KVCompat.PreConditions";

  public static <T> boolean checkNotNull(T arg, String message) {
    if (arg != null) {
      return true;
    }
    if (!BuildConfig.DEBUG) {
      LogUtils.e(TAG, message);
      return false;
    }
    throw new NullPointerException(message);
  }

}
