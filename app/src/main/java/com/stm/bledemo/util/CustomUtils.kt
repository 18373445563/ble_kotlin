package com.stm.bledemo.util

import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AlertDialog
import com.example.kotlintest.content.SysData.activities


object CustomUtils {

    fun getVersionName(context: Context): String? //获取版本号
    {
        var res = ""
        try {
            var pi = context.packageManager.getPackageInfo(context.packageName, 0)
            res = pi.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return res
    }

    fun getVersionCode(context: Context): Int //获取版本号(内部识别号)
    {
        return try {
            val pi = context.packageManager.getPackageInfo(context.packageName, 0)
            pi.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
            0
        }
    }

    //常用弹框
    fun showDialog(context: Context, msg: String): Dialog {
        var dialog = AlertDialog.Builder(context)
        dialog.setTitle("提示")
        dialog.setMessage(msg)
        dialog.setCancelable(false)
        dialog.setPositiveButton("知道啦！") { p0, p1 ->
            p0?.dismiss()
        }
        return dialog.show()
    }

    fun finishAllActivity() {
        activities.forEach() { it?.finish() }
    }

}