package com.example.kotlintest.httpUtil

import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.stm.bledemo.httpUtil.DownLoadEntity
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

object OkHttpHelper {

    private val okHttpClient: OkHttpClient
    private val mHandler: Handler
    private val mGson: Gson
    private var mHttpMethodType: HttpMethodType? = null
    private var mParams: MutableMap<String, String>? = null

    init {
        okHttpClient = OkHttpClient().newBuilder()
            .readTimeout(60, TimeUnit.SECONDS)//设置读取超时时间
            .writeTimeout(60, TimeUnit.SECONDS)//设置写的超时时间
            .connectTimeout(60, TimeUnit.SECONDS)//设置连接超时时间
            //https支持
            .hostnameVerifier { hostname, session -> true }
            .sslSocketFactory(initSSLSocketFactory(), initTrustManager())
            .build()
        mHandler = Handler(Looper.getMainLooper())
        mGson = Gson()
    }

    private fun initSSLSocketFactory(): SSLSocketFactory {
        var sslContext: SSLContext? = null
        try {
            sslContext = SSLContext.getInstance("SSL")
            val xTrustArray = arrayOf(initTrustManager())
            sslContext.init(
                null,
                xTrustArray, SecureRandom()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }


        return sslContext!!.socketFactory
    }

    private fun initTrustManager(): X509TrustManager {
        return object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }
        }
    }

    private fun buildFormData(params: Map<String, String>?): RequestBody {
        val body = FormBody.Builder()
        params?.forEach {

            try {
                if (!TextUtils.isEmpty(it.key)) {
                    body.add(it.key, it.value)
                }
            } catch (e: Exception) {
            }
        }

        return body.build()
    }

    private fun buildFileData(params: Map<String, String>?): RequestBody {
        val builder = MultipartBody.Builder()
        builder.setType(MultipartBody.FORM)
        params?.forEach {
            try {
                if (!TextUtils.isEmpty(it.key)) {
                    if (it.key == "file") {
                        val file = File(it.value)
                        builder.addFormDataPart(
                            it.key, file.name,
                            RequestBody.create(null, file)
                        )
                    } else {
                        builder.addFormDataPart(it.key, "" + it.value)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        return builder.build()
    }

    private fun buildRequest(url: String, params: MutableMap<String, String>?, methodType: HttpMethodType): Request {
        val builder = Request.Builder()
        builder.url(url)

        when (methodType) {
            HttpMethodType.GET -> {
                mHttpMethodType = HttpMethodType.GET
                builder.get()
            }
            HttpMethodType.POST -> {
                mHttpMethodType = HttpMethodType.POST
                builder.post(buildFormData(params))
            }
            HttpMethodType.UPLOAD_FILE -> {
                mHttpMethodType = HttpMethodType.UPLOAD_FILE
                builder.post(buildFileData(params))
            }
            HttpMethodType.DOWNLOAD_FILE -> {
                mHttpMethodType = HttpMethodType.DOWNLOAD_FILE
                try {
                    this.mParams?.clear()
                    this.mParams = params
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return builder.build()

    }

    private fun callBackError(callback: BaseCallback<*>, response: Response, code: Int, e: Exception?) {
        if (mHttpMethodType != HttpMethodType.DOWNLOAD_FILE) {
            mHandler.post { callback.onError(response, code, e!!) }
        } else {
            callback.onError(response, code, e!!)
        }

    }

    private fun callBackSuccess(callback: BaseCallback<*>, response: Response, mObject: Any) {

        if (mHttpMethodType != HttpMethodType.DOWNLOAD_FILE) {
            mHandler.post {
                callback.onSuccess(response, mObject)
            }
        } else {
            callback.onSuccess(response, mObject)
        }

    }

    private fun callBackProgress(callback: BaseCallback<*>, response: Response, total: Long, current: Long) {
        mHandler.post { callback.onProgress(response, total, current) }

    }

    private fun doRequest(request: Request, callback: BaseCallback<*>) {
        callback.onRequestBefore(request)
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure(call.request(), e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (mHttpMethodType == HttpMethodType.DOWNLOAD_FILE) {
                    if (response.isSuccessful) {

                        val length = response.body!!.contentLength()
                        if (length == 0L) {
                            return
                        }
                        var inputS: InputStream? = null
                        var fos: FileOutputStream? = null
                        val buff = ByteArray(2048)
                        var len: Int = 0
                        var flag: Boolean = true

                        val localPath = mParams?.get("localPath")

                        val downEntity = DownLoadEntity(
                            "" + mParams?.get("localPath"),
                            "" + mParams?.get("serverPath")
                        )

                        try {
                            inputS = response.body!!.byteStream()
                            var current: Long = 0



                            if (TextUtils.isEmpty(localPath)) {
                                callback.onResponse(response)
                                throw RuntimeException("localPath is miss")
                            }
                            val file = File(localPath)

                            if (file.exists()) {
                                file.delete()
                            }
                            fos = FileOutputStream(file)

                            while (flag) {
                                len = inputS.read(buff)
                                flag = len != -1
                                if (flag) {
                                    callBackProgress(callback, response, length, current)
                                    current += len.toLong()
                                    fos.write(buff, 0, len)
                                }
                            }
                            fos.flush()

                        } catch (e: Exception) {
                            callback.onResponse(response)
                            e.printStackTrace()
                        } finally {
                            try {
                                inputS?.close()
                                fos?.close()
                            } catch (e: Exception) {
                                callback.onResponse(response)
                                e.printStackTrace()
                            }

                            callback.onResponse(response)
                            callBackSuccess(callback, response, downEntity)
                        }
                    } else {
                        callBackError(callback, response, response.code, null)
                    }
                } else {
                    callback.onResponse(response)

                    if (response.isSuccessful) {

                        if (callback.mType == String::class.java) {
                            val resultStr = response.body!!.string()

                            Log.i("wxf", "server_data->" + resultStr)

                            callBackSuccess(callback, response, resultStr)
                        } else if ("" + callback.mType == "byte[]") {
                            callBackSuccess(callback, response, response.body!!.bytes())
                        } else {
                            try {
                                val resultStr = response.body!!.string()

                                Log.i("wxf", "server_data->" + resultStr)

                                val mObject = Gson().fromJson<Any>(resultStr, callback.mType)
                                callBackSuccess(callback, response, mObject)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                callBackError(callback, response, response.code, e)
                            }

                        }
                    }
                }

            }


        })
    }

    fun get(url: String, baseCallback: BaseCallback<*>) {
        doRequest(buildRequest(url, null, HttpMethodType.GET), baseCallback)
    }

    fun post(url: String, params: MutableMap<String, String>?, baseCallback: BaseCallback<*>) {
        doRequest(buildRequest(url, params, HttpMethodType.POST), baseCallback)
    }

    /**
     *  this map must have localFilePath parma
     */
    fun upLoadFile(url: String, params: MutableMap<String, String>, callback: BaseCallback<*>) {
        doRequest(buildRequest(url, params, HttpMethodType.UPLOAD_FILE), callback)
    }

    /**
     *  this must have localPath parma
     */
    fun downLoadFile(url: String, params: MutableMap<String, String>?, callback: BaseCallback<*>) {

        if (params != null) {
            if (TextUtils.isEmpty(params.get("localPath"))) {
                throw RuntimeException("map localPath is miss")
            } else {
                mParams?.put("localPath", "" + params?.get("localPath"))
            }
            if (!TextUtils.isEmpty(url)) {
                mParams?.put("serverPath", url)
            }
        }

        doRequest(buildRequest(url, params, HttpMethodType.DOWNLOAD_FILE), callback)
    }

}

enum class HttpMethodType {
    GET, POST, UPLOAD_FILE, DOWNLOAD_FILE
}