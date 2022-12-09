package com.stm.bledemo.activity.parasetting

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.kotlintest.content.SysData
import com.example.kotlintest.entity.JsonManager
import com.example.kotlintest.entity.JsonManager.getParamUpdateData
import com.example.kotlintest.entity.JsonManager.paramDetail
import com.example.kotlintest.entity.JsonManager.paramRealValue
import com.example.kotlintest.entity.JsonManager.paramUpdateData
import com.example.kotlintest.util.CommonDialog
import com.example.kotlintest.util.DataDeal
import com.stm.bledemo.dataDeal.BaseInterface
import com.stm.bledemo.R
import com.stm.bledemo.activity.oplog.FileOpLogActivity
import com.stm.bledemo.activity.paraupload.UploadActivity
import com.stm.bledemo.ble.BLEManager
import com.stm.bledemo.ble.BLEManager.bAdapter
import com.stm.bledemo.ble.BLEManager.isParaUpload
import com.stm.bledemo.ble.BLEManager.isPswCheck
import com.stm.bledemo.databinding.ActivityParaSettingBinding
import com.stm.bledemo.extension.getNowDateTime
import com.stm.bledemo.util.CustomProgressDialog
import com.stm.bledemo.util.CustomUtils
import com.stm.bledemo.util.ShareLogUtil.setParaUpload
import kotlinx.android.synthetic.main.activity_para_setting.*
import kotlinx.android.synthetic.main.activity_para_setting.back

import kotlinx.coroutines.*

import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ParaSettingActivity : AppCompatActivity(), View.OnClickListener, BaseInterface {

    private lateinit var binding: ActivityParaSettingBinding
    var progressDialog: CustomProgressDialog? = null

    @Volatile
    var status = 0
    private var expandableListView: ExpandableListView? = null
    internal var adapter: ExpandableListAdapter? = null
    private var titleList: List<String>? = null

    //参数显示对话框
    var paramDialog: Dialog? = null

    //进度显示框
    class MyHandler(private val activity: WeakReference<ParaSettingActivity>) : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == 0X111) {
                activity.get()?.progressDialog?.setProgress(activity.get()?.status!!)

            }
        }
    }

    private val mHandler = MyHandler(WeakReference(this))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_para_setting)
        SysData.activities.add(this)
        BLEManager.baseInterface = this
        expandableListView = findViewById(R.id.expandableListView)
        if (expandableListView != null) {
            //val listData = data
            val listData = JsonManager.paramMenu
            titleList = ArrayList(listData.keys)
            adapter = CustomExpandableListAdapter(this, titleList as ArrayList<String>, listData)
            expandableListView!!.setAdapter(adapter)
            expandableListView!!.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
                if (isPswCheck) {
                    var key =
                        listData[(titleList as ArrayList<String>)[groupPosition]]!![childPosition]
                    var intent= Intent(this@ParaSettingActivity, DetailParaActivity::class.java)
                    intent.putExtra("key", key)
                    startActivity(intent)
                } else {
                    Toast.makeText(binding.root.context, "请先开通调试模式！", Toast.LENGTH_SHORT).show()
                }
                false
            }
        }
        progressDialog = CustomProgressDialog(this, 1)
        initButton()

    }

    //初始化按钮点击事件
    private fun initButton() {
        back.setOnClickListener(this)
        log_record.setOnClickListener(this)
        para_upload.setOnClickListener(this)
        para_download.setOnClickListener(this)
        set_ble_state.setOnClickListener(this)
        sw_status.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
            if (b && !isPswCheck) {
                //显示弹框
                showParamDialogDebug()
            } else {
                isPswCheck = false
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        if (v == null) {
            return
        }
        if (!isPswCheck && v.id!=R.id.back) {
            Toast.makeText(binding.root.context, "请先开通调试模式！", Toast.LENGTH_SHORT).show()
            return
        }
        when (v.id) {
            R.id.back -> {
                finish()
            }
            R.id.log_record -> {
                Intent(this@ParaSettingActivity, FileOpLogActivity::class.java).apply {
                    startActivity(this)
                }

            }
            //参数上传:机制：全部读取成功
            R.id.para_upload -> {
                status = 0
                //开启协程发送
                progressDialog!!.show()
                var startTime = System.currentTimeMillis()
                //数据发送
                var job = BLEManager.scopeSetDeal.launch(start = CoroutineStart.LAZY) {
                    var startTime = System.currentTimeMillis()
                    while (paramUpdateData.isNotEmpty() && System.currentTimeMillis() - startTime < 60000) {
                        for ((k, v) in paramUpdateData) {
                            delay(250)
                            DataDeal().readData(k, v)
                        }
                    }
                }

                GlobalScope.launch() {
                    //获取数据
                    isParaUpload = true
                    getParamUpdateData()
                    var listLength = paramUpdateData.size
                    job.start()
                    if (isParaUpload) {
                        mHandler.sendEmptyMessage(0x111)
                        while (status < 100 && isParaUpload && System.currentTimeMillis() - startTime < 60000) {
                            // 获取耗时操作的完成百分比
                            Thread.sleep(500)
                            status =
                                (((listLength - paramUpdateData.size).toFloat() / listLength) * 100).toInt()
                            mHandler.sendEmptyMessage(0x111)
                        }
                        if (status >= 100 && isParaUpload) {
                            runOnUiThread {
                                progressDialog!!.dismiss()
                                //显示成功的弹框，数据写入
                                isParaUpload = false
                                paramUpdateData.clear()
                                status = 0
                                showDialog(this@ParaSettingActivity, "读取成功，是否保存？")
                            }

                        } else if (isParaUpload) {
                            runOnUiThread {
                                progressDialog!!.dismiss()
                                isParaUpload = false
                                status = 0
                                showDialog(this@ParaSettingActivity, "部分数据读取失败，是否保存？")
                            }
                        } else {
                            status = 0
                        }
                    }
                }


            }
            //参数下载
            R.id.para_download -> {
                    Intent(this@ParaSettingActivity, UploadActivity::class.java).apply {
                        startActivity(this)
                    }
            }

            R.id.set_ble_state -> {
                if (BLEManager.isConnected) {
                    Toast.makeText(this, "当前已连接!", Toast.LENGTH_SHORT).show()
                } else {
                    if (BLEManager.currentScanResult != null) {
                        BLEManager.connect(BLEManager.currentScanResult!!, this)
                    }
                    //隐藏状态，显示进度
                    set_ble_state.visibility = View.GONE
                    set_circle_set.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showParamDialogDebug() {
        try {
            paramDialog = Dialog(this, R.style.DialogStyle)
            val view = LayoutInflater.from(this).inflate(R.layout.view_debug_psw, null)
            paramDialog!!.setContentView(view)
            paramDialog!!.setCanceledOnTouchOutside(false)
            var debugSubmit = view.findViewById<Button>(R.id.debug_submit)
            var debugBack = view.findViewById<Button>(R.id.debug_back)
            var pswRes = view.findViewById<TextView>(R.id.psw_res)
            var debugPsw = view.findViewById<EditText>(R.id.debug_psw)
            debugSubmit.setOnClickListener {
                //  参数发送获取参数结果
                //  paramDialog!!.dismiss()  获取Y组0000
                BLEManager.scope.launch {
                    if (BLEManager.isConnected && BLEManager.writeCharacteristic != null) {
                        //读取数据
                        DataDeal().readData(32768, 1)
                        delay(500)

                        launch(Dispatchers.Main) {
                            //获取数据
                            if ((debugPsw.text.toString()).equals(paramRealValue.get(32768)?.realValue)) {
                                pswRes.text = "成功"
                                isPswCheck = true
                            } else {
                                pswRes.text = "密码错误或响应失败！"
                                isPswCheck = false
                            }
                        }
                    } else {
                        launch(Dispatchers.Main) {
                            pswRes.text = "请检查蓝牙连接状态！"
                        }
                    }
                }
            }
            //返回按钮
            debugBack.setOnClickListener {
                sw_status.isChecked = pswRes.text.toString().equals("成功")
                paramDialog!!.dismiss()
            }
            paramDialog!!.show()

        } catch (e: Exception) {
            e.printStackTrace()
            if (paramDialog != null) {
                paramDialog!!.dismiss()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showDialog(context: Context, title: String) {
        CommonDialog.Builder(context).setTitle(title)
            .setPositiveButton("确定", DialogInterface.OnClickListener { p0, _ ->
                p0?.dismiss()
                //文件数据写入
                //若有失败的获取失败的
                var resList = HashMap<String, String>()
                //获取时间
                var currentTime = Date().getNowDateTime()
                if (paramUpdateData.isNotEmpty()) {
                    for ((k, v) in paramUpdateData) {
                        var temp = k
                        for (i in 0 until v) {
                            resList[temp.toString()] = temp.toString()
                            temp += 1
                        }
                    }

                }
                for ((k, v) in paramDetail) {
                    var list = paramDetail[k]
                    for (i in list!!.indices) {
                        if (resList.isNotEmpty()) {

                            if (resList[list[i].address!!] != null) {
                                setParaUpload(list[i], this@ParaSettingActivity, "0", currentTime)
                            } else {
                                setParaUpload(list[i], this@ParaSettingActivity, "1", currentTime)
                            }

                        } else {
                            setParaUpload(list[i], this@ParaSettingActivity, "1", currentTime)
                        }
                    }
                }
            })
            //点击取消
            .setNegativeButton(
                "取消",
                DialogInterface.OnClickListener { p0, p1 ->
                    p0?.dismiss()
                    paramUpdateData.clear()
                })
            .setWith(0.77f)
            .create()
            .show()
    }

    override fun onResume() {
        super.onResume()
        if (BLEManager.isConnected) {
            set_ble_state.setImageResource(R.drawable.ic_bluetooth_on)
        } else {
            set_ble_state.setImageResource(R.drawable.ic_bluetooth_off)
        }
        BLEManager.baseInterface = this
    }

    override fun onStop() {
        super.onStop()
        isParaUpload = false

    }


    override fun showStatus(message: String, type: Int) {
        runOnUiThread {
            if (!isFinishing) {
                if (bAdapter.isEnabled) {
                    if (BLEManager.isConnected) {
                        //显示蓝牙在线状态,一秒检测一次
                        set_ble_state.setImageResource(R.drawable.ic_bluetooth_on)

                    } else {
                        set_ble_state.setImageResource(R.drawable.ic_bluetooth_off)
                        //关闭调试模式
                        sw_status.isChecked =false
                        isPswCheck=false
                    }
                    if (type == 1) {
                        CustomUtils.showDialog(this, message)
                    }
                    if (!BLEManager.isConnecting) {
                        //显示状态、隐藏加载
                        set_ble_state.visibility = View.VISIBLE
                        set_circle_set.visibility = View.GONE
                    }
                } else {
                    //断开连接，跳转扫描页面
                    BLEManager.disconnect()
                    CustomUtils.finishAllActivity()
                }
            }
        }
    }

}