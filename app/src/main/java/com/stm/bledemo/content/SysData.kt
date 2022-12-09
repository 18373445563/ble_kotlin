package com.example.kotlintest.content

import android.app.Activity
import com.stm.bledemo.activity.scan.ScanInterface
import java.util.*

object SysData {
    //存取读取数据
    var realTimeData: HashMap<Int,ByteArray> = HashMap<Int,ByteArray>()
    //门机状态
    var craneStates = false

     val activities = HashSet<Activity>()
}