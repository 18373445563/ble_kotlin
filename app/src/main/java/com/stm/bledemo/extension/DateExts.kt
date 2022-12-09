package com.stm.bledemo.extension

import java.text.SimpleDateFormat
import java.util.*


fun Date.getNowDateTime(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    return sdf.format(this)
}

