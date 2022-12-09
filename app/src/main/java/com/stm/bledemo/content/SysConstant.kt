package com.example.kotlintest.content

import java.util.*

class SysConstant {
    companion object{
        const val REQUEST_URL1="http://192.168.10.173:9000/app/"
        //写通道uuid
        const val WRITE_CHARACT_UUID = "00002b11-0000-1000-8000-00805f9b34fb"

        //通知通道 uuid
        const val NOTIFY_CHARACT_UUID ="00002b10-0000-1000-8000-00805f9b34fb"
        //获取服务
        val SERVER_CHARACT_UUID: UUID =UUID.fromString("00002b00-0000-1000-8000-00805f9b34fb")

        //读取参数个数
        const val READ_PARA_NUM =5

        //快速点击
        const val MIN_CLICK_DELAY_TIME = 1000

    }
}