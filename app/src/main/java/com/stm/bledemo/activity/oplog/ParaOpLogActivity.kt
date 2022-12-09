package com.stm.bledemo.activity.oplog

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stm.bledemo.R
import com.stm.bledemo.util.ShareLogUtil.readLogOp
import kotlinx.android.synthetic.main.activity_op_log_detail.*

import java.io.File


class ParaOpLogActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_op_log_detail)
        //接收跳转路径
        val filePath = intent.getStringExtra("filePath")
        var list=readLogOp(File(filePath))

        var recyclerView=findViewById<RecyclerView>(R.id.op_detail_list)
        var layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager=layoutManager
        var adapter=ParaOpDetailAdapter(list,this)
        recyclerView.adapter=adapter
        op_log_detail_back.setOnClickListener{
            finish()
        }

    }

}

