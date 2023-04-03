package io.github.porum.kvcompat.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Process
import java.io.File

internal object AppUtils {

  @Volatile
  private var currentProcessName: String? = null

  @JvmStatic
  fun isMainProcess(context: Context) = context.packageName == getCurrentProcessName(context)

  @JvmStatic
  fun getCurrentProcessName(context: Context): String? {
    return currentProcessName ?: (getProcessNameByRunningInfo(context, Process.myPid())
      ?: getProcessNameByCmdline(Process.myPid()))?.also {
      currentProcessName = it
    }
  }

  private fun getProcessNameByRunningInfo(context: Context, pid: Int): String? {
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return null
    return am.runningAppProcesses.firstOrNull { it.pid == pid }?.processName
  }

  @Suppress("SpellCheckingInspection")
  private fun getProcessNameByCmdline(pid: Int): String? {
    return File("/proc/$pid/cmdline").bufferedReader().use {
      readLine()?.trim()?.intern()
    }
  }
}