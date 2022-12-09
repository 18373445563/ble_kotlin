package com.stm.bledemo.activity.parasetting

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kotlintest.content.SysConstant.Companion.READ_PARA_NUM
import com.example.kotlintest.content.SysData
import com.example.kotlintest.entity.JsonManager
import com.example.kotlintest.entity.ParaEntity
import com.example.kotlintest.util.DataDeal
import com.stm.bledemo.dataDeal.BaseInterface
import com.stm.bledemo.R
import com.stm.bledemo.ble.BLEManager
import com.stm.bledemo.ble.BLEManager.bAdapter
import com.stm.bledemo.ble.BLEManager.scopeSetDeal
import com.stm.bledemo.util.CustomUtils
import kotlinx.android.synthetic.main.activity_detail_para.*
import kotlinx.android.synthetic.main.activity_detail_para.back
import kotlinx.coroutines.*
import timber.log.Timber


class DetailParaActivity : AppCompatActivity(), View.OnClickListener,BaseInterface {
    var list =ArrayList<ParaEntity>()
    var adapter: ParaDetailAdapter? = null
    companion object{
        var settingAddress=-1
        var isSetFlag=false
        var isSuccess=false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_para)
        SysData.activities.add(this)
        BLEManager.baseInterface = this
        val keyStr = intent.getStringExtra("key")
        list = JsonManager.paramDetail[keyStr] as ArrayList<ParaEntity>
        //添加数据
        paraDetailRecyclerView.layoutManager= LinearLayoutManager(this)
        adapter=ParaDetailAdapter(list,this)
        paraDetailRecyclerView.adapter=adapter
        //读取某组数据---初次读取
        if(list.size>0){
            showData(list,adapter!!)
        }
        initBnt()
    }

    private fun initBnt(){
        back.setOnClickListener(this)
        para_flush.setOnClickListener(this)
        detail_ble_state.setOnClickListener(this)
    }
    override fun onClick(v: View?) {
        if (v == null) {
            return
        }
        when (v.id) {
            R.id.back -> {
                finish()
            }
            R.id.para_flush -> {
             showData(list,adapter!!)
            }
            R.id.detail_ble_state -> {
                if (BLEManager.isConnected) {
                    Toast.makeText(this, "当前已连接", Toast.LENGTH_SHORT).show()
                } else {
                    if (BLEManager.currentScanResult != null) {
                        BLEManager.connect(BLEManager.currentScanResult!!, this)
                    }
                    //隐藏状态，显示进度
                    detail_ble_state.visibility=View.GONE
                    detail_circle_set.visibility=View.VISIBLE
                }
            }
        }
    }

    private fun showData(list:ArrayList<ParaEntity>, pAdapter:ParaDetailAdapter){
        if(BLEManager.isConnected){
            runBlocking {
                var job=scopeSetDeal.launch(start = CoroutineStart.LAZY) {
                    //获取发送次数及余数
                    var loopNum=list.size/READ_PARA_NUM
                    var  remainder=list.size%READ_PARA_NUM
                    //获取起始位
                    var startIndex=list.get(0).address?.toInt()
                    //根据规则数据读取
                    if (startIndex != null) {

                        for (i in 0 until loopNum) {
                            //循环发送数据
                            Timber.e("startIndex-----------------"+(startIndex +READ_PARA_NUM* i))
                            DataDeal().readTimeData(startIndex +READ_PARA_NUM* i, 5)
                            //设置发送频率
                            delay(200)
                        }
                        if (remainder != 0) {
                            Timber.e("startIndex-----------------"+(startIndex+ READ_PARA_NUM* loopNum))
                            DataDeal().readTimeData(startIndex+ READ_PARA_NUM* loopNum, remainder)
                        }

                    }
                }
                //设置值
                job.join()
                delay(500)
                DataDeal().paraValueToList(list)
                pAdapter.notifyDataSetChanged()
            }
        }else{
            Toast.makeText(this, "蓝牙已断开！", Toast.LENGTH_SHORT).show()
        }


    }
    override fun onResume() {
        super.onResume()
        if (BLEManager.isConnected) {
            detail_ble_state.setImageResource(R.drawable.ic_bluetooth_on1)
        } else {
            detail_ble_state.setImageResource(R.drawable.ic_bluetooth_off1)
        }
        BLEManager.baseInterface = this
    }

    override fun showStatus(message: String, type: Int) {
        if(!isFinishing){
            if(bAdapter.isEnabled) {
                if (BLEManager.isConnected) {
                    //显示蓝牙在线状态,一秒检测一次
                    detail_ble_state.setImageResource(R.drawable.ic_bluetooth_on1)

                } else {
                    detail_ble_state.setImageResource(R.drawable.ic_bluetooth_off1)
                }
                if (type == 1) {
                    CustomUtils.showDialog(this, message)
                }
                if(!BLEManager.isConnecting){
                    //显示状态、隐藏加载
                    detail_ble_state.visibility=View.VISIBLE
                    detail_circle_set.visibility=View.GONE
                }
            }else{
                //断开连接，跳转扫描页面
                BLEManager.disconnect()
                CustomUtils.finishAllActivity()
            }
        }
    }
}


