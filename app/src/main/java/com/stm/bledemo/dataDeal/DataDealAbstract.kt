package com.stm.bledemo.dataDeal

import com.example.kotlintest.entity.JsonManager
import com.example.kotlintest.entity.ParaEntity
import okhttp3.internal.and

abstract class DataDealAbstract {
    //接收数据处理
    abstract fun dealGetData(arr_buff: ByteArray,address:Int)
    //判断合法
    abstract fun isLegitimate(arr_buff: ByteArray):Boolean
    /**单个写数据
     */
    abstract fun dealSingleWriteData(address: Int, value: Int)
    /**多个写数据
     */
    abstract fun dealWriteData(address: Int, num: Int, value: IntArray)
    /**读取数据
     */
    abstract fun readData(address: Int, num: Int)
    /**返回读取数据
     */
    abstract fun readDataByteArray(address: Int, num: Int):ByteArray
    /**返回写数据
     */
    abstract fun writeDataByteArray(address: Int, num: Int, value: IntArray):ByteArray

    fun isCrc16(arr_buff: ByteArray, arr_buff_new: ByteArray): Boolean {
        var calcData = getCrc16(arr_buff_new)
        return calcData?.get(0) == arr_buff.get(arr_buff.size - 2) && calcData?.get(1) == arr_buff.get(
            arr_buff.size - 1
        )
    }


    fun getCrc16(arr_buff: ByteArray): ByteArray? {
        val len = arr_buff.size
        // 预置 1 个 16 位的寄存器为十六进制FFFF, 称此寄存器为 CRC寄存器。
        var crc = 0xFFFF
        var j: Int
        var i: Int = 0
        while (i < len) {

            // 把第一个 8 位二进制数据 与 16 位的 CRC寄存器的低 8 位相异或, 把结果放于 CRC寄存器
            crc = crc and 0xFF00 or (crc and 0x00FF) xor (arr_buff[i] and 0xFF)
            j = 0
            while (j < 8) {

                // 把 CRC 寄存器的内容右移一位( 朝低位)用 0 填补最高位, 并检查右移后的移出位
                if (crc and 0x0001 > 0) {
                    // 如果移出位为 1, CRC寄存器与多项式A001进行异或
                    crc = crc shr 1
                    crc = crc xor 0xA001
                } else  // 如果移出位为 0,再次右移一位
                    crc = crc shr 1
                j++
            }
            i++
        }
        val src = ByteArray(2)
        src[1] = ((crc shr 8 and 0xFF).toByte())
        src[0] = ((crc and 0xFF).toByte())
        return src
    }

   // 获取高低位的值
    fun getValueToDec(bytePara1: Byte, bytePara2: Byte): Int {

        return byteArrayToInt(bytePara1).shl(8) + byteArrayToInt(bytePara2)
    }

    //获取
    fun getDataToDec(bytePara: Byte): Int {
        return  bytePara and 0xff shl (4) * 8
    }

    fun byteArrayToInt(bytes: ByteArray): Int {
        var value = 0
        for (i in 0..3) {
            value += bytes[i] and 0xff shl (3 - i) * 8
        }
        return value
    }

    fun byteArrayToInt(bytes: Byte): Int {
        return  bytes and 0xff shl (4) * 8
    }

    //list赋值

    fun paraValueToList(list: ArrayList<ParaEntity>) {
        for (i in 0 until list.size){
            var key=list.get(i).address?.toInt()
            list.get(i).realValue=JsonManager.paramRealValue[key]?.realValue
        }
    }



}