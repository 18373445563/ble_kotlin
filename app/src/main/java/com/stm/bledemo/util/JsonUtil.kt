package com.example.kotlintest.util

import android.content.Context
import android.content.res.AssetManager
import com.example.kotlintest.entity.ParaEntity
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object JsonUtil {
    fun file2JsonStr(ctx: Context, fileName: String): String? {
        val stringBuilder = StringBuilder()
        try {
//            val assetManager: AssetManager = getApplication<Application>().assets

            val assetManager: AssetManager = ctx.assets
            val isr = InputStreamReader(assetManager.open(fileName))
            val bf = BufferedReader(isr)
            var line: String?
            while (bf.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
            bf.close()
            isr.close()
            return stringBuilder.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }


    fun jsonStr2Object(jsonStr: String?): Map<String,List<LinkedHashMap<String,String>>> {
        val gson = Gson()
        val entity: Map<String,List<LinkedHashMap<String,String>>>  = gson.fromJson(jsonStr, Map::class.java) as Map<String,List<LinkedHashMap<String,String>>>
        return entity
    }

    fun json2Para(jsonStr: LinkedTreeMap<String,String>): ParaEntity {
        val gson = Gson()
        val str = gson.toJson(jsonStr)
        val entity: ParaEntity = gson.fromJson(str, ParaEntity::class.java) as ParaEntity
        return entity
    }

    inline  fun <reified T> getListObject(list:List<Map<String,Object>>, cls:Class<T>) : List<T>{
        val paramList = ArrayList<T>()
        if (!list.isEmpty()){
            for (i in list.indices) {
                paramList.add(this.parseMapObject(list[i],cls));
            }
        }
        return paramList;
    }

    inline  fun <reified T> parseMapObject( paramMap:Map<String,Object>, cls:Class<T>) :T {
        val gson = Gson()
        val entity: T = gson.fromJson(gson.toJson(paramMap), cls) as T
        return entity
    }

}