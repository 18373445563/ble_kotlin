package com.stm.bledemo.activity.paraupload

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.NotificationCompat.getColor
import androidx.core.content.ContextCompat.getColor
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.stm.bledemo.R
import com.stm.bledemo.databinding.ParaUploadBinding
import com.stm.bledemo.entity.ParaUploadBean


class ParaUploadDetailAdapter(var paraList: List<ParaUploadBean>, var ctx: Context) :
    RecyclerView.Adapter<ParaUploadDetailAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ParaUploadBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    //重写的第一个方法，用来给制定加载那个类型的Recycler布局
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ParaUploadBinding>(
            inflater,
            R.layout.para_upload,
            parent,
            false
        )
        return ViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            val paraEntity = paraList[position]
            uploadParaName.text = paraEntity.uploadName
            uploadParaValue.text = paraEntity.uploadValue
            if (paraEntity.uploadRes.equals("1")) {
                opUploadRes.text = "成功"
                opUploadRes.setTextColor(Color.parseColor("#bfbfbf"))
            } else {
                opUploadRes.text = "失败"
                opUploadRes.setTextColor(Color.parseColor("#d81e06"))
            }
        }

    }

    override fun getItemCount(): Int {
        return paraList.size
    }

}