package com.stm.bledemo.entity
/**
 *参数名
 * 操作类型：1：读；2：写
 * 设置值
 * 操作时间：
 * 操作结果:1：成功；0：失败
 */
class ParaSettingOPBean(var paraName:String,var setType:String, var setValue:String,var setTime:String,var opRes:String)