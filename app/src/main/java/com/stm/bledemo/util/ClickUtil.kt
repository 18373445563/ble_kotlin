package com.example.kotlintest.util

import com.example.kotlintest.content.SysConstant.Companion.MIN_CLICK_DELAY_TIME


object ClickUtil {
    // 两次点击按钮之间的点击间隔不能少于1000毫秒
    private var lastClickTime: Long = 0
    val isFastClick: Boolean
        get() {
            var flag = false
            val curClickTime = System.currentTimeMillis()
            if (curClickTime - lastClickTime >= MIN_CLICK_DELAY_TIME) {
                flag = true
            }
            lastClickTime = curClickTime
            return flag
        }


}