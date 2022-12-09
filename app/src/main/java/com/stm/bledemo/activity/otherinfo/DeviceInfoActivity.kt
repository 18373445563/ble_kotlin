package com.stm.bledemo.activity.otherinfo


import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.stm.bledemo.R
import com.stm.bledemo.ble.BLEManager.bAdapter
import kotlinx.android.synthetic.main.activity_device_info.*
import kotlinx.android.synthetic.main.fragment_device_info.*

/**
Created by wrs on 16/12/2019,下午 4:54
projectName: ZKotlin
packageName: com.example.admin.zkotlin
 */

class DeviceInfoActivity :  AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_info)
        device_deviceNameText.text = Settings.Secure.getString(
            contentResolver, "bluetooth_name"
        )
        device_modelText.text = Build.MODEL
        if(bAdapter.isEnabled){
            if (bAdapter.isLe2MPhySupported) device_phy2MText.text = "YES"
            if (bAdapter.isLeCodedPhySupported) device_phyCodedText.text = "YES"
            if (bAdapter.isLeExtendedAdvertisingSupported) device_extAdvText.text = "YES"
            if (bAdapter.isLePeriodicAdvertisingSupported) device_perAdvText.text = "YES"
        }
        back.setOnClickListener{
            finish()
        }
    }

}