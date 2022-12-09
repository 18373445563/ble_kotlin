package com.stm.bledemo.activity.paraupload

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlintest.entity.ParaEntity
import com.stm.bledemo.R
import com.stm.bledemo.entity.ParaUploadBean
import com.stm.bledemo.util.ShareLogUtil.readLogOp
import com.stm.bledemo.util.ShareLogUtil.readParaUpload
import kotlinx.android.synthetic.main.activity_op_log_detail.*
import kotlinx.android.synthetic.main.activity_op_log_detail.op_log_detail_back
import kotlinx.android.synthetic.main.activity_para_upload_detail.*

import java.io.File
import java.util.regex.Matcher
import java.util.regex.Pattern


class ParaUploadActivity : AppCompatActivity() {
    var list = ArrayList<ParaUploadBean>()
    var listTemp = ArrayList<ParaUploadBean>()
    var adapter: ParaUploadDetailAdapter? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_para_upload_detail)
        //接收跳转路径
        val filePath = intent.getStringExtra("filePath")
        list = readParaUpload(File(filePath))
        listTemp.addAll(list)
        uploadDetailList.layoutManager =LinearLayoutManager(this)
        adapter = ParaUploadDetailAdapter(listTemp, this)
        uploadDetailList.adapter = adapter
        op_log_detail_back.setOnClickListener {
            finish()
        }

        ed_search.addTextChangedListener(Watcher())
        upload_search_cancel.setOnClickListener{
            ed_search.setText("")
        }

    }

    inner class Watcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            var str = s.toString()
            var p: Pattern = Pattern.compile(str, Pattern.CASE_INSENSITIVE)
            listTemp.clear()
            for (i in 0 until list.size) {
                val pp: ParaUploadBean = list[i]
                val matcher: Matcher = p.matcher(pp.uploadName)
                if (matcher.find()) {
                    listTemp.add(pp)
                }
            }
            adapter!!.notifyDataSetChanged()

        }

        override fun afterTextChanged(s: Editable?) {

        }
    }

}

