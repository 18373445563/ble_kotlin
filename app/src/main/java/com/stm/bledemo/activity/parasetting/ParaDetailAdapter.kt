package com.stm.bledemo.activity.parasetting

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlintest.entity.JsonManager.paramRealValue
import com.example.kotlintest.entity.ParaEntity
import com.example.kotlintest.util.DataDeal

import com.stm.bledemo.R
import com.stm.bledemo.ble.BLEManager
import com.stm.bledemo.databinding.RowParaShowBinding
import com.stm.bledemo.entity.ParaSettingOPBean
import com.stm.bledemo.extension.getNowDateTime
import com.stm.bledemo.util.ShareLogUtil.setLogOp
import com.stm.bledemo.util.ShareLogUtil.setMessageOP
import kotlinx.android.synthetic.main.view_param.view.*
import kotlinx.android.synthetic.main.view_param.view.cancelBtn
import kotlinx.android.synthetic.main.view_param.view.dialog_titileTxt
import kotlinx.android.synthetic.main.view_param.view.param_rangeTxt
import kotlinx.android.synthetic.main.view_param.view.param_readBtn
import kotlinx.android.synthetic.main.view_param.view.param_valueTxt
import kotlinx.android.synthetic.main.view_param.view.param_writeBtn
import kotlinx.android.synthetic.main.view_param_radio.view.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

class ParaDetailAdapter(var paraList: ArrayList<ParaEntity>, var ctx: Context) :
    RecyclerView.Adapter<ParaDetailAdapter.ViewHolder>() {
    //参数显示对话框
    var paramDialog: Dialog? = null


    //在内部类里面获取到item里面的组件
    @RequiresApi(Build.VERSION_CODES.O)
    inner class ViewHolder(val binding: RowParaShowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            with(binding) {
                paraLayout.setOnClickListener {
                    var paraDate = paraList[bindingAdapterPosition]
                    //判断是否可以写
                    if (paraDate.isChange.equals("Y") || paraDate.isChange.equals("E")) {
                        if (paraDate.paraType.equals("B")) {
                            showParamDialogOther(paraDate);
                        } else if (paraDate.paraType.equals("C")) {
                            //显示
                            showParamDialog(paraDate, 1)
                            //显示描述
                        } else {
                            showParamDialog(paraDate, 2)
                            //显示描述
                        }

                    } else {
                        showParamDialog(paraDate, 3)
                    }
                }

            }
        }
    }


    //重写的第一个方法，用来给制定加载那个类型的Recycler布局
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<RowParaShowBinding>(
            inflater,
            R.layout.row_para_show,
            parent,
            false
        )
        return ViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            val paraEntity = paraList[position]
            paraDes.text = paraEntity.paraDes
            paraName.text = paraEntity.paraName + "：" + paraEntity.paraNameDes
            paraDefaultValue.text = paraEntity.paraDefaultValue
            paraRangeDes.text = paraEntity.paraRangeDes
            isChange.text = paraEntity.isChange
            paraType.text = paraEntity.paraType
            address.text = paraEntity.address
            //部分数据加上单位等
            if (paraEntity.accuracy.equals("")) {
                currentValue.text = paraEntity.realValue + paraEntity.unit
            } else {
                var data1 = paraEntity.realValue?.toFloat()
                var data2 = paraEntity.accuracy?.toFloat()
                if (data1 != null && data2 != null) {
                    currentValue.text = (data1 / data2).toString() + paraEntity.unit
                }
            }
            realRange.text = paraEntity.realRange
        }

    }

    override fun getItemCount(): Int {
        return paraList.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SuspiciousIndentation")
    fun showParamDialog(paraDate: ParaEntity, type: Int) {
        try {
            paramDialog = Dialog(ctx, R.style.DialogStyle)
            val view = LayoutInflater.from(ctx).inflate(R.layout.view_param, null)
            paramDialog!!.setContentView(view)
            paramDialog!!.setCanceledOnTouchOutside(false)
            val des = view.findViewById<LinearLayout>(R.id.des)
            val writLayout = view.findViewById<RelativeLayout>(R.id.write_param_layout)
            if (type == 1) {
                des.visibility = View.VISIBLE
            } else {
                des.visibility = View.GONE
            }
            if (type == 3) {
                writLayout.visibility = View.GONE
            } else {
                writLayout.visibility = View.VISIBLE
            }
            with(view) {
                //读取参数
                dialog_titileTxt.text = paraDate.paraName + ":" + paraDate.paraNameDes
                param_rangeTxt.text = paraDate.paraRangeDes
                param_desTxt.text = paraDate.paraDes
                //设置值
                //部分数据加上单位等
                param_valueTxt.setText(getShowValue(paraDate,paraDate.realValue?.toInt()))
                para_real_range.text = paraDate.realRange
                para_address.text = paraDate.address

                //数据读取
                param_readBtn.setOnClickListener {
                    if (BLEManager.isConnected) {
                        var job = BLEManager.scopeSetDeal.launch(start = CoroutineStart.LAZY) {
                            //设置值
                            DetailParaActivity.isSetFlag = true
                            DetailParaActivity.settingAddress = para_address.text.toString().toInt()
                        }
                        GlobalScope.launch(Dispatchers.Main) {
                            job.join()
                            DataDeal().readData(para_address.text.toString().toInt(), 1)
                            delay(800)
                            if (DetailParaActivity.isSuccess) {
                                param_hint.text = "读取成功";
                                var realData = paramRealValue[para_address.text.toString().toInt()]!!.realValue
                                //部分数据加上单位等
                                param_valueTxt.setText(getShowValue(paraDate,realData?.toInt()))
                                setLogOp(
                                    paraDate,
                                    ctx,
                                    BLEManager.currentBleName,
                                    "1",
                                    param_valueTxt.text.toString(),
                                    "1"
                                )
                            } else {
                                param_hint.text = "读取失败或超时";
                                setLogOp(
                                    paraDate,
                                    ctx,
                                    BLEManager.currentBleName,
                                    "1",
                                    "--",
                                    "0"
                                )
                            }
                            //数据复原
                            initSendData()
                        }
                    } else {
                        param_hint.text = "蓝牙离线读取失败"
                    }

                }
                //设置参数
                param_writeBtn.setOnClickListener(View.OnClickListener {
                    var rangeList = para_real_range.text.toString().split(";")
                    var inputData = if(param_writeEdit.text.toString().contains(".") &&paraDate.accuracy!=null){
                        (param_writeEdit.text.toString().toFloat()* paraDate.accuracy!!.toFloat()).toInt()
                    }else{
                        param_writeEdit.text.toString().toInt()
                    }
                    param_writeEdit.text.toString().replace(".", "").toInt()
                    if (param_writeEdit.text.toString().toFloat() < rangeList[0].toFloat() || param_writeEdit.text.toString().toFloat() > rangeList[1].toFloat()) {
                        Toast.makeText(ctx, "设置范围错误", Toast.LENGTH_SHORT).show()
                    } else {
                        if (BLEManager.isConnected) {
                            BLEManager.scopeSetDeal.launch {
                                //发送参数设置
                                //读取参数看是否设置成功
                                //显示结果
                                DataDeal().dealSingleWriteData(
                                    para_address.text.toString().toInt(),
                                    inputData
                                )
                                launch(Dispatchers.Main) {

                                    //判断设置值是否相等
                                    delay(400)
                                    //读取一次
                                    DataDeal().readData(para_address.text.toString().toInt(), 1)
                                    delay(400)
                                    var currentData = paramRealValue[para_address.text.toString().toInt()]!!.realValue
                                    if (!currentData.equals(inputData.toString())) {
                                        param_hint.text = "设置失败或超时";
                                        setLogOp(
                                            paraDate,
                                            ctx,
                                            BLEManager.currentBleName,
                                            "2",
                                            param_writeEdit.text.toString(),
                                            "0"
                                        )
                                    } else {
                                        var res=paramRealValue[para_address.text.toString().toInt()]!!.realValue
                                        if(res!=null){
                                            param_valueTxt.setText(
                                                getShowValue(paraDate,res?.toInt())

                                            )
                                        }

                                        param_hint.text = "设置成功";
                                        setLogOp(
                                            paraDate,
                                            ctx,
                                            BLEManager.currentBleName,
                                            "2",
                                            param_writeEdit.text.toString(),
                                            "1"
                                        )
                                    }

                                }
                            }
                        } else {
                            param_hint.text = "蓝牙离线设置失败"
                        }


                    }

                })
                //取消按钮
                cancelBtn.setOnClickListener {
                    flushData()
                }
                paramDialog!!.setOnDismissListener(DialogInterface.OnDismissListener {
                    paramDialog = null
                })
                paramDialog!!.show()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            if (paramDialog != null) {
                paramDialog!!.dismiss()
            }
            paramDialog = null
        }
    }

    private fun flushData() {
        //更新一下list,关闭弹框
        DataDeal().paraValueToList(paraList)
        notifyDataSetChanged()
        if(paramDialog!=null){
            paramDialog!!.dismiss()
        }
    }

    private fun initSendData() {
        //数据复原
        DetailParaActivity.isSetFlag = false
        DetailParaActivity.settingAddress = -1
        DetailParaActivity.isSuccess = false
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun showParamDialogOther(paraDate: ParaEntity) {
        try {
            var radioSet = -1
            paramDialog = Dialog(ctx, R.style.DialogStyle)
            val view = LayoutInflater.from(ctx).inflate(R.layout.view_param_radio, null)
            paramDialog!!.setContentView(view)
            paramDialog!!.setCanceledOnTouchOutside(false)
            with(view) {
                val des = paraDate.paraDes?.replace(":", "：")
                val radioList = des?.split("\n");
                for (i in radioList?.indices!!) {
                    if (!radioList[i].contains("备注")) {
                        val tempButton = RadioButton(ctx)
                        tempButton.setButtonDrawable(R.drawable.radiobutton) // 设置按钮的样式

                        tempButton.setPadding(20, 0, 0, 0) // 设置文字距离按钮四周的距离
                        tempButton.text = radioList[i]
                        radioGroup.addView(
                            tempButton,
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    }
                }
                radioGroup.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
                    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
                        // 通过RadioGroup的findViewById方法，找到ID为checkedID的RadioButton
                        val mRadioButton = view.findViewById<RadioButton>(checkedId)
                        if (mRadioButton.text.contains("：")) {
                            radioSet = mRadioButton.text.split("：")[0].toInt()
                        }
                    }
                })

                dialog_titileTxt.text = paraDate.paraName + ":" + paraDate.paraNameDes
                param_rangeTxt.text = paraDate.paraRangeDes
                param_valueTxt.setText(paraDate.realValue)
                para_address_radio.text = paraDate.address
                //读取参数
                param_readBtn.setOnClickListener {
                    var job = BLEManager.scopeSetDeal.launch(start = CoroutineStart.LAZY) {
                        //设置值
                        DetailParaActivity.isSetFlag = true
                        DetailParaActivity.settingAddress =
                            para_address_radio.text.toString().toInt()
                    }
                    GlobalScope.launch(Dispatchers.Main) {
                        job.join()
                        DataDeal().readData(para_address_radio.text.toString().toInt(), 1)
                        //设置值
                        delay(800)
                        if (DetailParaActivity.isSuccess) {
                            param_hint_radio.text = "读取成功";
                            var realData = paramRealValue[para_address_radio.text.toString().toInt()]!!.realValue
                            //param_valueTxt.setText()
                            //部分数据加上单位等
                            param_valueTxt.setText(getShowValue(paraDate,realData?.toInt()))
                            setLogOp(
                                paraDate,
                                ctx,
                                BLEManager.currentBleName,
                                "1",
                                param_valueTxt.text.toString(),
                                "1"
                            )
                        } else {
                            param_hint.text = "读取失败或超时";
                            setLogOp(
                                paraDate,
                                ctx,
                                BLEManager.currentBleName,
                                "1",
                                "--",
                                "0"
                            )
                            //数据复原
                            initSendData()
                        }
                    }
                }
                //设置参数
                param_writeBtn.setOnClickListener {
                    if (radioSet == -1) {
                        Toast.makeText(ctx, "请选择参数！", Toast.LENGTH_SHORT).show()
                    }else{
                        BLEManager.scopeSetDeal.launch {
                            //发送参数设置
                            //读取参数看是否设置成功
                            //显示结果
                            if (BLEManager.isConnected) {
                                param_value_radio.text = radioSet.toString()
                                DataDeal().dealSingleWriteData(
                                    para_address_radio.text.toString().toInt(),
                                    radioSet
                                )
                                launch(Dispatchers.Main) {

                                    //判断设置值是否相等
                                    delay(400)
                                    //读取一次
                                    DataDeal().readData(
                                        para_address_radio.text.toString().toInt(),
                                        1
                                    )
                                    delay(400)
                                    var currentData = paramRealValue.get(
                                        para_address_radio.text.toString().toInt()
                                    )!!.realValue
                                    if (!currentData.equals(param_value_radio.text.toString())) {
                                        param_hint_radio.text = "设置失败";
                                        setLogOp(paraDate, ctx, BLEManager.currentBleName, "2", param_value_radio.text.toString(), "0")
                                    } else {
                                        param_valueTxt.setText(
                                            paramRealValue[para_address_radio.text.toString().toInt()]!!.realValue
                                        )
                                        param_hint_radio.text = "设置成功";
                                        setLogOp(paraDate, ctx, BLEManager.currentBleName, "2", param_value_radio.text.toString(), "1")
                                    }
                                }
                            } else {
                                param_hint_radio.text = "蓝牙离线，设置失败";
                            }
                        }
                    }
                }
                //取消按钮
                cancelBtn.setOnClickListener {
                    flushData()
                }
                paramDialog!!.setOnDismissListener(DialogInterface.OnDismissListener {
                    paramDialog = null

                })
                paramDialog!!.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (paramDialog != null) {
                paramDialog!!.dismiss()
            }
            paramDialog = null
        }
    }

    //返回带单位参数
    private fun getShowValue(paraDate:ParaEntity,value:Int?) :String{
        return if (!paraDate.accuracy.equals("")) {
            var data1 = value?.toFloat()
            var data2 = paraDate.accuracy?.toFloat()
            if (data1 != null && data2 != null) {
                ((data1 / data2).toString() + paraDate.unit)
            }else{
                value.toString() + paraDate.unit
            }
        } else {
            value.toString() + paraDate.unit
        }
    }

}