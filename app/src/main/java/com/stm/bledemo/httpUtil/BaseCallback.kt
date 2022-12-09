package com.example.kotlintest.httpUtil

import com.google.gson.internal.`$Gson$Types`
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

open class BaseCallback<T> {
    var mType: Type? = null

    init {
        mType = getSuperclassTypeParameter(javaClass)
    }

    companion object {
        internal fun getSuperclassTypeParameter(subClass: Class<*>): Type {

            val superclass = subClass.genericSuperclass
            if (superclass is Class<*>) {
                throw  RuntimeException("Missing type parameter.")
            }
            val parameterizedType = superclass as ParameterizedType

            return `$Gson$Types`.canonicalize(parameterizedType.actualTypeArguments[0])
        }
    }


    open fun onRequestBefore(request: Request) {}
    open fun onFailure(request: Request, e: IOException) {}
    open fun onSuccess(response: Response, t: Any) {}
    open fun onError(response: Response, code: Int, e: Exception) {}
    open fun onResponse(response: Response) {}
    open fun onProgress(response: Response, total: Long, current: Long) {}

}