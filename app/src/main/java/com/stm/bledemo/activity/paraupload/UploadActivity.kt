package com.stm.bledemo.activity.paraupload

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stm.bledemo.R
import com.stm.bledemo.activity.oplog.FileDirByMonthAdapter
import com.stm.bledemo.activity.parasetting.ParaSettingActivity
import com.stm.bledemo.ble.BLEManager
import com.stm.bledemo.util.CustomProgressDialog
import com.stm.bledemo.util.ShareLogUtil.eachFileRecurse
import kotlinx.android.synthetic.main.activity_file_op_log.*
import kotlinx.android.synthetic.main.activity_file_op_log.file_op_log_back
import kotlinx.android.synthetic.main.activity_para_upload.*
import kotlinx.android.synthetic.main.file_dir_show.*
import java.io.File
import java.lang.ref.WeakReference


class UploadActivity : AppCompatActivity() {
    var progressDialog: CustomProgressDialog? = null
    var status = 0
    private var uploadDirByMonthAdapter: UploadDirByMonthAdapter? = null
    //进度显示框
    class MyHandler(private val activity: WeakReference<UploadActivity>) : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == 0X111) {
                activity.get()?.progressDialog?.setProgress(activity.get()?.status!!)
            }
        }
    }

    internal val mHandler = MyHandler(WeakReference(this))
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SuspiciousIndentation", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_para_upload)
        uploadDirByMonthAdapter = UploadDirByMonthAdapter(this,this@UploadActivity)
        uploadList!!.adapter = uploadDirByMonthAdapter
        //设置数据
        var list= ArrayList<File>()
        if(!BLEManager.currentBleName.equals("")){
            list=eachFileRecurse( this,BLEManager.currentBleName,"paraUpload")

        }
        uploadDirByMonthAdapter!!.setItems(list)
        progressDialog = CustomProgressDialog(this,0)
        file_op_log_back.setOnClickListener{
            finish()
        }
    }

}

