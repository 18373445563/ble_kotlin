package com.stm.bledemo.util

import android.app.Dialog
import android.content.Context

import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.example.kotlintest.entity.JsonManager
import com.stm.bledemo.R
import com.stm.bledemo.activity.parasetting.ParaSettingActivity
import com.stm.bledemo.ble.BLEManager.isParaUpload
import kotlinx.android.synthetic.main.custom_dialog_layout.*

/**
 * type:0:参数下载，，1：参数上传
 */
class CustomProgressDialog(context: Context?,type:Int) : Dialog(context!!, R.style.transparent_dialog) {
    //设置进度条
    //获取进度条
    var progressBar: ProgressBar? = null

    init {
        //一开始就设置为透明背景
        createLoadingDialog(context,type)
    }

    fun createLoadingDialog(context: Context?,type:Int) {
        val inflater = LayoutInflater.from(context)
        //得到加载的view
        val v: View = inflater.inflate(R.layout.custom_dialog_layout, null)
        //加载布局
        val layout = v.findViewById(R.id.dialog_view) as LinearLayout
        val cancelLoad = v.findViewById<ImageView>(R.id.cancel_load)
        val tvMsg = v.findViewById<TextView>(R.id.tv_msg)
    
        progressBar = v.findViewById(R.id.pb_Circle)
        //判断是否显示取消
        if(type==0){
            cancelLoad.visibility=View.GONE
            tvMsg.text="下载进度"
        }else{
            cancelLoad.visibility=View.VISIBLE
            tvMsg.text="读取进度"
        }
        //设置不可通过点击外面区域取消
        setCanceledOnTouchOutside(false)
        setOnCancelListener { Toast.makeText(context, "加载取消", Toast.LENGTH_SHORT).show() }
        // 设置布局，设为全屏
        cancelLoad.setOnClickListener {
            isParaUpload=false
            JsonManager.paramUpdateData.clear()
            dismiss()
        }
        setContentView(
            layout, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        )
    }
    //设置进度
    fun setProgress(progress: Int) {
        progressBar!!.progress = progress
    }
}
