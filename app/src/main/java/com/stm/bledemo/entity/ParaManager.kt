package com.example.kotlintest.entity


import android.content.Context
import com.example.kotlintest.content.SysConstant
import com.example.kotlintest.util.DataDeal

import com.example.kotlintest.util.JsonUtil
import com.example.kotlintest.util.JsonUtil.getListObject
import com.example.kotlintest.util.JsonUtil.json2Para
import com.google.gson.internal.LinkedTreeMap
import kotlinx.coroutines.delay
import org.json.JSONObject
import timber.log.Timber
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

import kotlin.collections.LinkedHashMap


object JsonManager {

    /**
     * 参数菜单
     */
    val paramMenu = LinkedHashMap<String, List<String>>()

    /**
     * 参数详情
     */
    val paramDetail = LinkedHashMap<String, List<ParaEntity>>()

    /**
     * 读取参数值
     */
    val paramRealValue = HashMap<Int, ParaValueDetailEntity>()

    /**
     * 读取实时参数
     */
    val realValueArr = IntArray(10)

    /**
     * 读取数据监控位
     */
    val paramMonitorValue = ArrayList<Int>()

    /**
     * 参数上传发送数据：<起始位，发送数据个数>
     */
    val paramUpdateData = ConcurrentHashMap<Int, Int>()


    fun getParamMenu(ctx: Context, fileName: String) {
        var paramStr = JsonUtil.file2JsonStr(ctx, fileName)
        var jsonToArr = JsonUtil.jsonStr2Object(paramStr)
        for ((k, v) in jsonToArr) {
            var listSecond = LinkedList<String>();
            // Map<String,List<Map<String,String>>>
            for (i in v.indices) {
                var second: Map<String, Any> = v[i]
                for ((secondK, secondV) in second) {
                    listSecond.add(secondK)
                    //数据转化map to ParaEntity
                    var mapToParaEntity =
                        getListObject(secondV as List<Map<String, Object>>, ParaEntity::class.java)
                    paramDetail.put(
                        secondK,
                        mapToParaEntity
                    )
                    for (j in mapToParaEntity.indices) {
                        var pal = ParaValueDetailEntity()
                        pal.realValue = "0"
                        pal.paraChildName =
                            mapToParaEntity[j].paraName + ":" + mapToParaEntity.get(j).paraNameDes
                        paramRealValue[mapToParaEntity.get(j).address!!.toInt()] = pal
                        if (mapToParaEntity[j].isMonitor.equals("Y")) {
                            paramMonitorValue.add(mapToParaEntity.get(j).address!!.toInt())
                        }
                    }

                }
            }
            paramMenu.put(k, listSecond)
        }
    }

    fun getParamUpdateData() {
        paramUpdateData.clear()
        if (paramMenu.size > 0) {
            for ((k, v) in paramMenu) {
                //获取二级菜单
                var list = paramMenu[k]
                if (list != null) {
                    for (i in list.indices) {
                        var listDetail = paramDetail[list[i]]
                        if (listDetail != null && listDetail.isNotEmpty()) {
                            //获取发送次数及余数
                            var loopNum = listDetail.size / SysConstant.READ_PARA_NUM
                            var remainder = listDetail.size % SysConstant.READ_PARA_NUM
                            var startIndex = listDetail[0].address?.toInt()
                            for (i in 0 until loopNum) {
                                if (startIndex != null) {
                                    paramUpdateData.put(
                                        startIndex + SysConstant.READ_PARA_NUM * i,
                                        5
                                    )
                                }
                            }
                            if (remainder != 0) {
                                if (startIndex != null) {
                                    paramUpdateData.put(
                                        startIndex + SysConstant.READ_PARA_NUM * loopNum,
                                        remainder
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}