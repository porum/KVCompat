package io.github.porum.kvcompat.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Process;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

import io.github.porum.kvcompat.logger.LogUtils;

public class AppUtils {
  private static final String TAG = "AppUtils";

  private static volatile String currentProcessName = "";

  public static boolean isMainProcess(Context context) {
    return TextUtils.equals(context.getPackageName(), getCurrentProcessName(context));
  }

  public static String getCurrentProcessName(Context context) {
    if (TextUtils.isEmpty(currentProcessName)) {
      currentProcessName = getProcessNameByPid(context, Process.myPid());
    }
    return currentProcessName;
  }

  private static String getProcessNameByPid(Context context, int pid) {
    try {
      if (context != null) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
          List<ActivityManager.RunningAppProcessInfo> list = am.getRunningAppProcesses();
          if (list != null && !list.isEmpty()) {
            for (ActivityManager.RunningAppProcessInfo info : list) {
              if (info.pid == pid) {
                return info.processName;
              }
            }
          }
        }
      }
    } catch (Throwable th) {
      LogUtils.w(TAG, "getProcessNameByPid error.", th);
    }

    return getProcessNameByCmdline(pid);
  }

  private static String getProcessNameByCmdline(int pid) {
    try (BufferedReader br = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"))) {
      String processName = br.readLine();
      if (!TextUtils.isEmpty(processName)) {
        processName = processName.trim().intern();
      }
      return processName;
    } catch (Throwable tr) {
      LogUtils.w(TAG, "getProcessNameByCmdline error.", tr);
    }

    return null;
  }
}
