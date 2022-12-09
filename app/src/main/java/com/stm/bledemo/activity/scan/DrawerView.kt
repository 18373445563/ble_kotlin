package com.stm.bledemo.activity.scan

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.stm.bledemo.R
import com.stm.bledemo.activity.otherinfo.DeviceInfoActivity
import kotlinx.android.synthetic.main.view_drawer.view.*

class DrawerView : LinearLayout, View.OnClickListener {

    private var mContext: Context? = null

    companion object {
        const val TASK_TAG = "taskMgrFragment"
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        LayoutInflater.from(context).inflate(R.layout.view_drawer, this)
        mContext = context
        initLogoutDialog()
        initView()
        initEvent()
    }

    private fun initView() {
    }

    fun initFragment(savedInstanceState: Bundle?) {
        var fragmentManager = (mContext as ScanActivity).supportFragmentManager
        var transaction = fragmentManager.beginTransaction()
        transaction.commit()
    }

    private fun initEvent() {
        testtvUserName.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.testtvUserName -> {
                //跳转另一个页面
                val intent: Intent = Intent(mContext, DeviceInfoActivity::class.java)
                mContext?.startActivity(intent)
            }
        }

    }


    private fun initLogoutDialog() {
    }

    fun onDestroy() {

    }
}