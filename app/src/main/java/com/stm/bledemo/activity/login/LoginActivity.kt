package com.stm.bledemo.activity.login

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlintest.content.SysConstant
import com.example.kotlintest.httpUtil.BaseCallback
import com.example.kotlintest.httpUtil.FileCallBack
import com.example.kotlintest.httpUtil.OkHttpHelper
import com.stm.bledemo.R
import com.stm.bledemo.activity.connection.ConnectionActivity
import com.stm.bledemo.activity.scan.ScanActivity
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.Request
import okhttp3.Response
import java.io.IOException


class LoginActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initEditText()
        initClickListener()
    }


    private fun initEditText() {
        etLoginName.isFocusable = true
        etLoginName.addTextChangedListener(nameWatcher)
        etLoginPsd.addTextChangedListener(psdWatcher)
    }

    private fun initClickListener() {
        etLoginName.setOnClickListener(this)
        etLoginPsd.setOnClickListener(this)
        btGoLogin.setOnClickListener(this)
    }

    private val nameWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable) {
            //获取当前输入内容
//            val input = s.toString()
//            Log.v(TAG, "psdWatcher_input=$input")

        }
    }

    private val psdWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable) {
//            //获取当前输入内容
//            val input = s.toString()
//            //校验规则
//            Log.v(TAG, "psdWatcher_input=$input")
        }
    }

    override fun onClick(v: View?) {
        if (v == null) {
            return
        }
        when (v.id) {
            R.id.btGoLogin -> {
                //校验填写的数据
                if (etLoginName.text.toString() == "" || etLoginPsd.text.toString() == ""
                ) {
                    Toast.makeText(
                        this,
                        "登录失败，请输入相关信息!",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                //http请求校验
                val requestParams = mutableMapOf(
                    "username" to etLoginName.text.toString(),
                    "password" to etLoginPsd.text.toString()
                )
                OkHttpHelper.post(
                    SysConstant.REQUEST_URL1 + "loginCheck",
                    requestParams,
                    object : BaseCallback<Int>() {
                        override fun onSuccess(response: Response, t: Any) {
                            super.onSuccess(response, t)
                            if (t == 1) {
                                //跳转到登录成功页面
                                val intent = Intent(this@LoginActivity, ScanActivity::class.java)
                                intent.putExtra("KEY_LOGIN_NAME", etLoginName.text.toString())
                                intent.putExtra("KEY_LOGIN_PSD", etLoginPsd.text.toString())
                                startActivity(intent)
                            } else {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "登录失败，用户名或密码错误!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return
                            }
                        }

                        override fun onFailure(request: Request, e: IOException) {
                            Toast.makeText(
                                this@LoginActivity,
                                "服务器请求失败!",
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }
                    }
                )
            }
        }
    }
}


