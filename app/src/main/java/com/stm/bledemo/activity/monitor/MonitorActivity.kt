package com.stm.bledemo.activity.monitor


import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.TimeInterpolator
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.kotlintest.content.SysConstant
import com.example.kotlintest.content.SysData.activities
import com.example.kotlintest.entity.JsonManager
import com.example.kotlintest.entity.JsonManager.paramMonitorValue
import com.example.kotlintest.entity.JsonManager.realValueArr
import com.example.kotlintest.util.DataDeal
import com.stm.bledemo.dataDeal.BaseInterface
import com.stm.bledemo.R
import com.stm.bledemo.activity.parasetting.ParaSettingActivity
import com.stm.bledemo.ble.BLEManager
import com.stm.bledemo.ble.BLEManager.bAdapter
import com.stm.bledemo.ble.BLEManager.isPswCheck
import com.stm.bledemo.databinding.ActivityBlueMonitorBinding
import com.stm.bledemo.util.CustomUtils
import com.stm.bledemo.util.CustomUtils.finishAllActivity
import kotlinx.android.synthetic.main.activity_blue_monitor.*
import kotlinx.android.synthetic.main.activity_blue_monitor.back
import kotlinx.android.synthetic.main.show_monitor.*
import kotlinx.coroutines.*
import timber.log.Timber
import java.sql.Time
import java.util.*

class MonitorActivity : AppCompatActivity(), View.OnClickListener, BaseInterface {

    private lateinit var binding: ActivityBlueMonitorBinding
    // 动画
    private var mSetAnim: AnimatorSet? = null
    //按钮设置
    private var isInitView = false

    var isStop = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_blue_monitor)
        BLEManager.baseInterface = this
        activities.add(this)
        back.setOnClickListener(this)
        main_blue_states.setOnClickListener(this)
    }

    /** Connection Interface */

    // Adds Discovered Services & Characteristics
    @SuppressLint("SuspiciousIndentation")
    private fun bleDeal() {

        var job=GlobalScope.launch(start = CoroutineStart.LAZY ) {
            while ((BLEManager.writeCharacteristic == null || BLEManager.notifyGattCharacteristic == null || BLEManager.isServiceFinish.compareAndSet(false, true)) && !isStop) {
                BLEManager.writeCharacteristic =
                    BLEManager.bGatt?.getService(SysConstant.SERVER_CHARACT_UUID)
                        ?.getCharacteristic(
                            UUID.fromString(SysConstant.WRITE_CHARACT_UUID)
                        )
                BLEManager.notifyGattCharacteristic =
                    BLEManager.bGatt?.getService(SysConstant.SERVER_CHARACT_UUID)
                        ?.getCharacteristic(
                            UUID.fromString(SysConstant.NOTIFY_CHARACT_UUID)
                        )
                if (BLEManager.notifyGattCharacteristic != null) {
                    BLEManager.enableNotifications(BLEManager.notifyGattCharacteristic)
                }
            }
            if (!isInitView) {
                initButton()
                isInitView = true
            }
        }


        //设置关注
        BLEManager.scopeRealTime.launch {
            job.join()
            while (!isStop && BLEManager.isConnected) {
//                for (i in 0 until paramMonitorValue.size) {
//                    if (!isStop) {
//                        DataDeal().readData(paramMonitorValue[i], 1)
//                        delay(200)
//                    }
//                }
                //发送广播帧
                DataDeal().dealSingleWriteData(45057, 100)
                delay(3000)
            }
        }


        GlobalScope.launch(Dispatchers.Main) {
            //更新界面
            //显示蓝牙状态
            while (!isStop && BLEManager.isConnected) {
                showView()
                delay(300)
            }
            if (!BLEManager.isConnected) {
                //界面初始化
                initView()
            }
        }
    }

    //初始化按钮
    private fun initButton() {
        close.setOnClickListener(this)
        stop.setOnClickListener(this)
        open.setOnClickListener(this)
        paraSetting.setOnClickListener(this)
    }

    //初始化界面
    private fun initView() {
        main_blue_states.setImageResource(R.drawable.ic_bluetooth_off1)
        fault_code.text = ""
        out_frequency.text = "0Hz"
        run_times.text = "0"
        open_door_cmd.setImageResource(R.drawable.test_icon)
        close_door_cmd.setImageResource(R.drawable.test_icon)
        open_door_reach.setImageResource(R.drawable.test_icon)
        close_door_reach.setImageResource(R.drawable.test_icon)
    }

    private fun showView(){
        //设置故障码
        if (realValueArr[2]!=0) {
            fault_code.text = realValueArr[2].toString()
        }
        //设置频率
        var fre = realValueArr[1]?.toFloat()
        out_frequency.text = (fre?.div((100.toFloat()))).toString() + "Hz"
        //运行次数
        var highRunTime = realValueArr[4]
        var lowRunTime = realValueArr[3]
        run_times.text = (highRunTime* 10000 + lowRunTime!!).toString()
        //开关门命令
        var doorCmd = JsonManager.paramRealValue[282]!!.realValue
        //开门命令有效
//
        //开关门命令

        //开门到位有效
        /**
         * 0：门停止
        1：开门中
        2：开门到位
        3：关门中
        4：关门到位
         */
       if(realValueArr[0]== 1){
            doorAnim(
                2000L,
                DecelerateInterpolator(),
                (door_left_img.width * -1).toFloat(),
                (door_right_img.width).toFloat()
            )
        }else if(realValueArr[0]== 2){
            open_door_reach.setImageResource(R.drawable.test_icon_on)
        }else if(realValueArr[0]== 3){
             doorAnim(2000L, DecelerateInterpolator(), 0f, 0f)
        }else if(realValueArr[0]== 4){
            close_door_reach.setImageResource(R.drawable.test_icon)
        } else {
            close_door_reach.setImageResource(R.drawable.test_icon)
            open_door_reach.setImageResource(R.drawable.test_icon)
            doorAnim(2000L, DecelerateInterpolator(), 0f, 0f)
        }

    }



    //界面数据显示
//    private fun showView(){
//        //设置故障码
//        if (!JsonManager.paramRealValue[283]!!.realValue.equals("0")) {
//            fault_code.text = JsonManager.paramRealValue[283]!!.realValue
//        }
//        //设置频率
//        var fre = JsonManager.paramRealValue[4355]!!.realValue?.toInt()?.toFloat()
//        out_frequency.text = (fre?.div((100.toFloat()))).toString() + "Hz"
//        //运行次数
//        var highRunTime = JsonManager.paramRealValue[6414]!!.realValue?.toInt()
//        var lowRunTime = JsonManager.paramRealValue[6413]!!.realValue?.toInt()
//        run_times.text = (highRunTime!! * 10000 + lowRunTime!!).toString()
//        //开关门命令
//        var doorCmd = JsonManager.paramRealValue[282]!!.realValue
//        //开门命令有效
//        if (doorCmd.equals("1")) {
//            open_door_cmd.setImageResource(R.drawable.test_icon_on)
//            close_door_cmd.setImageResource(R.drawable.test_icon)
//        } else if (doorCmd.equals("2")) {
//            close_door_cmd.setImageResource(R.drawable.test_icon_on)
//            open_door_cmd.setImageResource(R.drawable.test_icon)
//        } else if (doorCmd.equals("3")) {
//            open_door_cmd.setImageResource(R.drawable.test_icon_on)
//            close_door_cmd.setImageResource(R.drawable.test_icon_on)
//        } else {
//            open_door_cmd.setImageResource(R.drawable.test_icon)
//            close_door_cmd.setImageResource(R.drawable.test_icon)
//        }
//
//        //开关门命令
//        var doorSignal = JsonManager.paramRealValue.get(258)!!.realValue.toString()
//        //截取后两位,获取长度
//        var ge = doorSignal.substring(doorSignal.length - 1, doorSignal.length).toInt()
//        var shi = 0
//        if (doorSignal.length > 1) {
//            shi = doorSignal.substring(doorSignal.length - 2, doorSignal.length - 1)
//                .toInt()
//        }
//
//        //开门到位有效
//        if (ge == 1) {
//            open_door_reach.setImageResource(R.drawable.test_icon_on)
//            doorAnim(
//                2000L,
//                DecelerateInterpolator(),
//                (door_left_img.width * -1).toFloat(),
//                (door_right_img.width).toFloat()
//            )
//        } else {
//            open_door_reach.setImageResource(R.drawable.test_icon)
//        }
//
//        //关门到位有效
//        if (shi == 1) {
//            close_door_reach.setImageResource(R.drawable.test_icon_on)
//            doorAnim(2000L, DecelerateInterpolator(), 0f, 0f)
//        } else {
//            close_door_reach.setImageResource(R.drawable.test_icon)
//        }
//    }

    override fun onClick(v: View?) {
        if (v == null) {
            return
        }
        when (v.id) {
            R.id.close -> {
                /** 关门*/
                DataDeal().dealSingleWriteData(45056, 2)
            }
            R.id.stop -> {
                /**停机*/
                //暂用于界面演示
                //DataDeal().dealSingleWriteData(45056, 4)
            }
            R.id.open -> {
                /*** 开门*/
                DataDeal().dealSingleWriteData(45056, 1)
            }
            R.id.back -> {
                finish()
            }

            R.id.main_blue_states -> {
                if (BLEManager.isConnected) {
                    Toast.makeText(this, "当前已连接", Toast.LENGTH_SHORT).show()
                } else {
                    if(BLEManager.currentScanResult != null && bAdapter.isEnabled){
                        BLEManager.connect(BLEManager.currentScanResult!!, this)
                        //隐藏状态，显示进度
                        main_blue_states.visibility=View.GONE
                        circle_set.visibility=View.VISIBLE
                    }else{
                        Toast.makeText(this, "请返回扫描界面，重新连接", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            R.id.paraSetting -> {
                //跳转参数设置
                Intent(this@MonitorActivity, ParaSettingActivity::class.java).apply {
                    startActivity(this)
                }
            }
        }
    }

    //动画类
    open fun doorAnim(
        duration: Long,
        interpolator: TimeInterpolator?,
        left: Float,
        right: Float,
    ) {
        mSetAnim = AnimatorSet()
        door_left_img.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        door_right_img.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        mSetAnim!!.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })

        // Animating the 2 parts away from each other
        val anim1: Animator = ObjectAnimator.ofFloat(
            door_left_img,
            "translationX", left
        )
        val anim2: Animator = ObjectAnimator.ofFloat(
            door_right_img,
            "translationX", right
        )
        if (interpolator != null) {
            anim1.interpolator = interpolator
            anim2.interpolator = interpolator
        }
        mSetAnim!!.duration = duration
        mSetAnim!!.playTogether(anim1, anim2)
        mSetAnim!!.start()
    }


    override fun onDestroy() {
        super.onDestroy()
        BLEManager.disconnect()
    }

    override fun onResume() {
        super.onResume()
        BLEManager.baseInterface = this
        isPswCheck=false
        if(BLEManager.isConnected){
            isStop = false
            bleDeal()
        }
    }

    override fun onStop() {
        super.onStop()
        isStop = true
    }


    override fun showStatus(message: String,type:Int) {
        runOnUiThread {
            if(!isFinishing){
                if(bAdapter.isEnabled){
                    if (BLEManager.isConnected) {
                        main_blue_states.setImageResource(R.drawable.ic_bluetooth_on1)
                        if(type==0){
                            isStop = false
                            bleDeal()
                        }
                    } else {
                        main_blue_states.setImageResource(R.drawable.ic_bluetooth_off1)
                        isStop=true
                    }
                    if(type==1){
                        CustomUtils.showDialog(this, message)
                    }
                    if(!BLEManager.isConnecting){
                        main_blue_states.visibility=View.VISIBLE
                        circle_set.visibility=View.GONE
                    }
                }else{
                    //断开连接，跳转扫描页面
                    Timber.e("Monitor------------------"+"断开连接，跳转扫描页面!！！！！")
                    BLEManager.disconnect()
                    finishAllActivity()
                }
            }
        }
    }

}