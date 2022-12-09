package com.stm.bledemo.activity.otherinfo


import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.stm.bledemo.R
import com.stm.bledemo.util.CustomUtils.getVersionCode
import com.stm.bledemo.util.CustomUtils.getVersionName

import kotlinx.android.synthetic.main.activity_detail_para.*
import kotlinx.android.synthetic.main.activity_device_info.*
import kotlinx.android.synthetic.main.activity_version_info.*


class VersionInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_version_info)
        soft_name.text=getVersionName(this)
        soft_version.text="版本号："+(getVersionCode(this).toFloat()/100).toString()
        version_back.setOnClickListener{
            finish()
        }
    }
}


