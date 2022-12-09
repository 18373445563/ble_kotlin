package com.stm.bledemo.activity.oplog

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stm.bledemo.R
import com.stm.bledemo.ble.BLEManager
import com.stm.bledemo.util.ShareLogUtil.eachFileRecurse
import kotlinx.android.synthetic.main.activity_file_op_log.*
import kotlinx.android.synthetic.main.file_dir_show.*
import java.io.File


class FileOpLogActivity : AppCompatActivity() {

    private var fileDirByMonthAdapter: FileDirByMonthAdapter? = null
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_op_log)
        fileDirByMonthAdapter = FileDirByMonthAdapter(this)
        fileList!!.adapter = fileDirByMonthAdapter
        //设置数据
        var list= ArrayList<File>()
        if(BLEManager.currentBleName != ""){
            list=eachFileRecurse( this,BLEManager.currentBleName,"log")
        }
        fileDirByMonthAdapter!!.setItems(list)
        file_op_log_back.setOnClickListener{
            finish()
        }

    }

}

