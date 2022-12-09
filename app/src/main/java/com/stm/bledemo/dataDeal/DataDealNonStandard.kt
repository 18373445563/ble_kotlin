package com.example.kotlintest.util

import com.example.kotlintest.entity.JsonManager.paramRealValue
import com.stm.bledemo.dataDeal.DataDealAbstract
import com.stm.bledemo.ble.BLEManager
import com.stm.bledemo.extension.decToHexString
import com.stm.bledemo.extension.hexToByteArray
import com.stm.bledemo.extension.removeWhiteSpace
import com.stm.bledemo.extension.toHexString
import kotlinx.coroutines.launch

class DataDealNonStandard :DataDealAbstract(){
    //接收数据处理
    //接收数据处理
    override fun dealGetData(arr_buff: ByteArray,address:Int) {
        //获取地址位
        var address = getValueToDec(arr_buff[2], arr_buff[3])
        //获取读取参数个数
        var readNum = getDataToDec(arr_buff[4])
        //获取Map地址，循环遍历存放值
        for (i in 0 until readNum.toInt()) {
            //获取map值
            if (paramRealValue[address + i] != null) {
                paramRealValue[address + i]?.realValue =
                    getValueToDec(arr_buff[5 + i], arr_buff[6 + i]).toString()
            }
        }

    }


    override fun isLegitimate(arr_buff: ByteArray): Boolean {
        //数据分割
        var hexStr = arr_buff.toHexString()
        var dataArray = hexStr.split(" ")
        var crcArray = hexStr.substring(0, hexStr.length - 6).removeWhiteSpace().hexToByteArray()
        return ((dataArray.get(1).equals("03") || dataArray.get(1)
            .equals("10")) && isCrc16(arr_buff, crcArray))
    }


    /**单个写数据
     */
    override fun dealSingleWriteData(address: Int, value: Int) {
        var valuelist = IntArray(value);
        dealWriteData(address, 1, valuelist)
    }

    /**多个写数据
     */
    override fun dealWriteData(address: Int, num: Int, value: IntArray) {

        var hexStr = writeDataByteArray(address,num,value)

        BLEManager.scope.launch {
            BLEManager.writeCharacteristic(BLEManager.writeCharacteristic, hexStr)
        }
    }

    /**读取数据
     */
    override fun readData(address: Int, num: Int) {
        //hexStr
        var hexStr = readDataByteArray(address, num)

        BLEManager.scope.launch {
            BLEManager.writeCharacteristic(BLEManager.writeCharacteristic, hexStr)
        }
    }

    /**返回读取数据
     */
    override fun readDataByteArray(address: Int, num: Int): ByteArray {
        //十进制转16进制字符串，还需补零
        var sendData = "0103" + decToHexString(address, 4) + decToHexString(num, 2)
        //转16进制，计算校验位
        var crcData = getCrc16(sendData.hexToByteArray())?.toHexString()
        //hexStr
        var hexStr = (sendData + crcData).removeWhiteSpace().hexToByteArray()
        return hexStr
    }

    /**返回写数据
     */

    override fun writeDataByteArray(address: Int, num: Int, value: IntArray): ByteArray {
        //十进制转16进制字符串，还需补零
        var sendPrefix = "0110" + decToHexString(address, 4) + decToHexString(num, 2)
        var data = ""
        for (i in value.indices) {
            data = data + decToHexString(value[i], 4)
        }
        var sendData = sendPrefix + data
        //转16进制，计算校验位
        var crcData = getCrc16(sendData.hexToByteArray())?.toHexString()
        //hexStr
        var hexStr = (sendData + crcData).removeWhiteSpace().hexToByteArray()

        return hexStr
    }


}


