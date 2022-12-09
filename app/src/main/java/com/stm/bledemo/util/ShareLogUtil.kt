package com.stm.bledemo.util

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.kotlintest.entity.JsonManager.paramRealValue
import com.example.kotlintest.entity.ParaEntity
import com.google.gson.Gson
import com.stm.bledemo.ble.BLEManager
import com.stm.bledemo.entity.ParaSettingOPBean
import com.stm.bledemo.entity.ParaUploadBean
import com.stm.bledemo.extension.getNowDateTime
import kotlinx.android.synthetic.main.view_param.view.*
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

object ShareLogUtil {

    //获取日志文本
    @RequiresApi(Build.VERSION_CODES.O)
    fun getLogFile(context: Context, bleName: String, catalogue: String): File {
        //这里获取的是app内部文件路径
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formatted = current.format(formatter)
        var bleFileName = formatted + ".txt"
        val file = File("${context.filesDir}/"+catalogue+"/"+bleName )
        if (!file.exists()) {
            file.mkdirs()
        }

        if (!hasFile(file.path)) {
            //data/user/0/com.xxx.xxx/files/log/bleLog.txt
            val logFile = File("${file.path}/" + bleFileName)
            if (!logFile.exists()) {
                try {
                    logFile.createNewFile()
                } catch (e: Exception) {
                    println(e)
                }
            }
            return logFile
        } else {
            return File("${context.filesDir}/"+catalogue+"/" +bleName+"/"+ bleFileName)
        }
    }


    //读取文件内容
    fun readLogFile(file: File): StringBuilder {
        val lines: List<String> = file.readLines()
        val res = StringBuilder()
        lines.forEach { line -> res.append(line + "\n") }
        return res
    }

    //数据转对象
    //读取文件内容
    fun readLogOp(file: File): ArrayList<ParaSettingOPBean> {
        val lines: List<String> = file.readLines()
        val res = ArrayList<ParaSettingOPBean>()
        lines.forEach { line ->
            res.add( Gson().fromJson(line,ParaSettingOPBean::class.java))
        }
        return res
    }

    //读取文件内容
    fun readParaUpload(file: File): ArrayList<ParaUploadBean> {
        val lines: List<String> = file.readLines()
        val res = ArrayList<ParaUploadBean>()
        lines.forEach { line ->
            res.add( Gson().fromJson(line,ParaUploadBean::class.java))
        }
        return res
    }

    //读取文件内容转地址Map
    fun readParaUploadMap(dataPath: String): ConcurrentHashMap<Int, Int> {
        var dataSend = readParaUpload(File(dataPath))
        val res = ConcurrentHashMap<Int,Int>()
        dataSend.forEach { line ->
            res.put(line.uploadAddress.toInt(),line.uploadValue.toInt())
        }
        return res
    }


    //判断路径下是否还有file
    private fun hasFile(filePath: String): Boolean {
        val file = File(filePath)
        if(file.list()!=null){
            return file.list().isNotEmpty()
        }else return false;
    }


    fun isFileExists(file: File): Boolean {
        return file.exists() && !file.isDirectory
    }

    //todo 保存消息日志
    @RequiresApi(Build.VERSION_CODES.O)
    fun setMessageOP(message: Any, context: Context, bleName: String, catalogue: String) {
        getLogFile(context,bleName,catalogue)
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formatted = current.format(formatter)
        var bleFileName = formatted + ".txt"
        var realFilePath = "${context.filesDir}/"+catalogue+"/"+bleName+"/" + bleFileName
        val fw = FileWriter(realFilePath, true)
        fw.appendLine(Gson().toJson(message))
        fw.close()
    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun setMessageAll(message: Any, context: Context, bleName: String, catalogue: String,date:String) {
        getLogFileAll(context,bleName,catalogue,date)
        var bleFileName = "$date.txt"
        var realFilePath = "${context.filesDir}/"+catalogue+"/"+bleName+"/" + bleFileName
        val fw = FileWriter(realFilePath, true)
        fw.appendLine(Gson().toJson(message))
        fw.close()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getLogFileAll(context: Context, bleName: String, catalogue: String,date:String): File {
        //这里获取的是app内部文件路径
        var bleFileName = date + ".txt"
        val file = File("${context.filesDir}/"+catalogue+"/"+bleName )
        if (!file.exists()) {
            file.mkdirs()
        }

        if (!hasFile(file.path)) {
            //data/user/0/com.xxx.xxx/files/log/bleLog.txt
            val logFile = File("${file.path}/" + bleFileName)
            if (!logFile.exists()) {
                try {
                    logFile.createNewFile()
                } catch (e: Exception) {
                    println(e)
                }
            }
            return logFile
        } else {
            return File("${context.filesDir}/"+catalogue+"/" +bleName+"/"+ bleFileName)
        }
    }


    //读取目录下的文件
    fun eachFileRecurse(context: Context,dirFile: String,dirName: String) :ArrayList<File> {
        var res=ArrayList<File>()
        var inputFile=File("${context.filesDir}/${dirName}/"+dirFile);
        val files = inputFile.listFiles() ?: return res
        for (file in files) {
            res.add(file)
        }
        return res
    }

  //文件删除
  fun deleteLogFile( filePath: String): Boolean {

      val file = File(filePath)
      if (!file.exists()) {
          return true
      }else{
          return file.delete()
      }
  }
    //设置日志
    @RequiresApi(Build.VERSION_CODES.O)
    fun setLogOp(paraDate: ParaEntity, context: Context, bleName: String,type: String, value: String, res: String){
        var para=ParaSettingOPBean(paraDate.paraName + ":" + paraDate.paraNameDes,type,value,
            Date().getNowDateTime(),res)
        setMessageOP(para,context,bleName,"log")
    }

    //存参数上传
    @RequiresApi(Build.VERSION_CODES.O)
    fun setParaUpload(paraDate: ParaEntity, context: Context, res: String,date: String){
        if(paramRealValue.get(paraDate.address?.toInt())!=null){
            var para=ParaUploadBean(paraDate.paraName + ":" + paraDate.paraNameDes,paramRealValue.get(paraDate.address?.toInt())!!.realValue.toString(),res, paraDate.address.toString())
            setMessageAll(para,context,BLEManager.currentBleName,"paraUpload",date)
        }
    }
}