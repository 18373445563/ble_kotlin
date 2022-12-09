package com.stm.bledemo.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.content.FileProvider
import com.stm.bledemo.BuildConfig
import java.io.File


object ShareUtil {

    //参考https://blog.csdn.net/gongjdde/article/details/102910296
    fun allShare(context: Context, content: String?) {
        var intent = Intent()
        // 设置分享行为
        intent.action = Intent.ACTION_SEND
        // 设置分享内容的类型
        intent.type = "text/plain"
        // 添加分享内容标题
        intent.putExtra(Intent.EXTRA_SUBJECT, "分享")
        // 添加分享内容
        intent.putExtra(Intent.EXTRA_TEXT, content)
        // 创建分享的 Dialog
        intent = Intent.createChooser(intent, "分享")
        context.startActivity(intent)
    }
    fun shareFile(context: Context, fileName: String?) {
        val file = File(fileName)
        if (null != file && file.exists()) {
            val share = Intent(Intent.ACTION_SEND)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val contentUri: Uri = FileProvider.getUriForFile(
                    context,
                    BuildConfig.APPLICATION_ID+".provider",
                    file
                )
                share.putExtra(Intent.EXTRA_STREAM, contentUri)
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
            }
            share.type = "application/vnd.ms-excel" //此处可发送多种文件
            share.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(Intent.createChooser(share, "分享文件"))
        } else {
            Toast.makeText(context, "分享文件不存在", Toast.LENGTH_SHORT).show()
        }
    }
}