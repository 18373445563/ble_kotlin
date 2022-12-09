package com.example.kotlintest.httpUtil

import android.app.ProgressDialog
import android.content.Context
import android.text.TextUtils
import com.stm.bledemo.httpUtil.DownLoadEntity
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

abstract class FileCallBack<T> : BaseCallback<DownLoadEntity> {
    internal var dialog: ProgressDialog? = null
    private var msg: String? = null


    constructor(context: Context) {
        if (dialog == null) {
            dialog = ProgressDialog(context)
            dialog!!.setCanceledOnTouchOutside(false)
        }
        dialog!!.setMessage("加载中......")
    }

    constructor(context: Context, message: String) {
        if (dialog == null) {
            dialog = ProgressDialog(context)
            dialog!!.setCanceledOnTouchOutside(false)
        }
        this.msg = message
        dialog!!.setMessage(message)
    }



    override fun onRequestBefore(request: Request) {
        showDialog()
    }

    override fun onFailure(request: Request, e: IOException) {
        dissmissDialog()
    }


    override fun onResponse(response: Response) {
        dissmissDialog()
    }

    override fun onProgress(response: Response, total: Long, current: Long) {
        try {
            if (TextUtils.isEmpty(msg)) {
                msg = "加载中"
            }
            if (dialog != null && dialog!!.isShowing)
                dialog!!.setMessage(msg + (current * 1f / total * 100).toInt() + "%")
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun dissmissDialog() {
        if (dialog != null && dialog!!.isShowing) {
            dialog!!.dismiss()
            dialog = null
        }
    }

    private fun showDialog() {
        if (dialog != null && !dialog!!.isShowing) {
            dialog!!.show()
        }
    }
}
