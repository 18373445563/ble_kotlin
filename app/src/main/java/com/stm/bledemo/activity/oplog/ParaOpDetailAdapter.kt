package com.stm.bledemo.activity.oplog

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.stm.bledemo.R
import com.stm.bledemo.databinding.ParaOpLogBinding
import com.stm.bledemo.entity.ParaSettingOPBean

class ParaOpDetailAdapter(var paraList: List<ParaSettingOPBean>, var ctx: Context) :
    RecyclerView.Adapter<ParaOpDetailAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ParaOpLogBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    //重写的第一个方法，用来给制定加载那个类型的Recycler布局
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ParaOpLogBinding>(
            inflater,
            R.layout.para_op_log,
            parent,
            false
        )
        return ViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            val paraEntity = paraList[position]
            opParaName.text = paraEntity.paraName
            opParaValue.text = paraEntity.setValue
            if (paraEntity.setType.equals("1")) {
                opType.text = "参数读取"
            } else {
                opType.text = "参数设置"
            }
            if (paraEntity.opRes.equals("1")) {
                opRes.text = "成功"
                opRes.setTextColor(Color.parseColor("#bfbfbf"))
            } else {
                opRes.text = "失败"
                opRes.setTextColor(Color.parseColor("#d81e06"))
            }
            opTime.text = paraEntity.setTime
        }

    }

    override fun getItemCount(): Int {
        return paraList.size
    }

}