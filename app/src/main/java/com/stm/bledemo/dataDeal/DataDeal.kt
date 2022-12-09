package com.example.kotlintest.util

import com.example.kotlintest.entity.JsonManager.paramRealValue
import com.example.kotlintest.entity.JsonManager.realValueArr
import com.stm.bledemo.dataDeal.DataDealAbstract
import com.stm.bledemo.ble.BLEManager
import com.stm.bledemo.extension.decToHexString
import com.stm.bledemo.extension.hexToByteArray
import com.stm.bledemo.extension.removeWhiteSpace
import com.stm.bledemo.extension.toHexString
import kotlinx.coroutines.launch
import timber.log.Timber

class DataDeal : DataDealAbstract() {
    //接收数据处理
    override fun dealGetData(arr_buff: ByteArray, ad: Int) {
        //判断数据类型
        if (getDataToDec(arr_buff[0]) == 1 && getDataToDec(arr_buff[1]) == 3) {
            //接收读取
            var num = getDataToDec(arr_buff[2]) / 2
            setParaValue(arr_buff, num, ad, 3)
            Timber.e("readStartAddress03!！！" + BLEManager.readStartAddress)
        } else if (getDataToDec(arr_buff[0]) == 0 && getDataToDec(arr_buff[1]) == 16) {
            var num = (arr_buff.size - 6) / 2
            setRealValue(arr_buff, num, 7)
        }
//        else if (getDataToDec(arr_buff[1]) == 16) {
//            //接收写入获取地址
//            var add = getValueToDec(arr_buff[2], arr_buff[3])
//            //获取长度
//            var num = (arr_buff.size - 6) / 2
//            //更新写入地址的值
//            setParaValue(arr_buff, num, add, 4)
//            Timber.e("readStartAddress10!！！" + BLEManager.readStartAddress)
//        }

    }


    override fun isLegitimate(arr_buff: ByteArray): Boolean {
        //数据分割
        var hexStr = arr_buff.toHexString()
        var dataArray = hexStr.split(" ")
        var crcArray = hexStr.substring(0, hexStr.length - 6).removeWhiteSpace().hexToByteArray()
        return ((dataArray[1] == "03" || dataArray[1] == "10") && isCrc16(arr_buff, crcArray))
    }

    /**单个写数据
     */
    override fun dealSingleWriteData(address: Int, value: Int) {
        var valuelist = IntArray(1);
        valuelist[0] = value
        dealWriteData(address, 1, valuelist)
    }

    /**多个写数据
     */
    override fun dealWriteData(address: Int, num: Int, value: IntArray) {
        BLEManager.readStartAddress = address
        var hexStr = writeDataByteArray(address, num, value)
        BLEManager.scope.launch {
            if (BLEManager.writeCharacteristic != null) {
                BLEManager.writeCharacteristic(BLEManager.writeCharacteristic, hexStr)
            }
        }
    }

    /**读取数据
     */
//    override fun readData(address: Int, num: Int) {
//        //hexStr
//        BLEManager.readStartAddress = address
//        var hexStr = readDataByteArray(address, num)
//
//        BLEManager.scope.launch {
//
//            BLEManager.writeCharacteristic(BLEManager.writeCharacteristic, hexStr)
//        }
//    }

    override fun readData(address: Int, num: Int) {
        //hexStr
        BLEManager.readStartAddress = address
        var hexStr = readDataByteArray(address, num)

        BLEManager.scope.launch {
            if (BLEManager.writeCharacteristic != null) {
                // Timber.e("Monitor------------------"+"setCharacteristic!！！！！！-----------${BLEManager.notifyGattCharacteristic}----------------${BLEManager.writeCharacteristic}")
                BLEManager.writeCharacteristic(BLEManager.writeCharacteristic, hexStr)
            }
            // println("address：$address||||||num：$num")
        }
    }

    /**返回读取数据
     */
    override fun readDataByteArray(address: Int, num: Int): ByteArray {
        //十进制转16进制字符串，还需补零
        var sendData = "0103" + decToHexString(address, 4) + decToHexString(num, 4)
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
        var sendPrefix =
            "0110" + decToHexString(address, 4) + decToHexString(num, 4) + decToHexString(
                2 * num,
                2
            )
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

    private fun setParaValue(arr_buff: ByteArray, num: Int, address: Int, startIndex: Int) {
        var tempAddress = address
        for (i in 0 until num) {
            if (paramRealValue[tempAddress] != null) {
                paramRealValue[tempAddress]?.realValue =
                    getValueToDec(
                        arr_buff[startIndex + 2 * i],
                        arr_buff[startIndex + 2 * i + 1]
                    ).toString()
                Timber.e(
                    "getValueToDec=======" + tempAddress + "==========" + paramRealValue[tempAddress]?.realValue + ";;;;" + getValueToDec(
                        arr_buff[startIndex + 2 * i],
                        arr_buff[startIndex + 2 * i + 1]
                    ).toString()
                )
            }
            tempAddress++
        }
    }

    private fun setRealValue(arr_buff: ByteArray, num: Int, startIndex: Int) {
        for (i in 0 until num) {
            var value=getValueToDec(arr_buff[startIndex + 2 * i], arr_buff[startIndex + 2 * i + 1])
            realValueArr[i]=value
        }
    }


    suspend fun readTimeData(address: Int, num: Int) {
        //hexStr
        BLEManager.readStartAddress = address
        var hexStr = readDataByteArray(address, num)
        if (BLEManager.writeCharacteristic != null) {
            BLEManager.writeCharacteristic(BLEManager.writeCharacteristic, hexStr)
        }
    }

    fun saveReadData(map: HashMap<Int, ByteArray>) {
        for ((k, v) in map) {
            dealGetData(v, k)
            //删除数据
            map.remove(k)
        }
    }


}


