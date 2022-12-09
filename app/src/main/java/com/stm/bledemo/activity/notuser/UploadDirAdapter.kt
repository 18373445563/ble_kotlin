package com.stm.bledemo.activity.notuser

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.*
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlintest.entity.JsonManager
import com.example.kotlintest.util.CommonDialog
import com.example.kotlintest.util.DataDeal
import com.stm.bledemo.R
import com.stm.bledemo.activity.parasetting.ParaSettingActivity
import com.stm.bledemo.activity.paraupload.ParaUploadActivity
import com.stm.bledemo.activity.paraupload.UploadActivity
import com.stm.bledemo.ble.BLEManager
import com.stm.bledemo.databinding.ParaUploadShowBinding
import com.stm.bledemo.entity.ParaUploadBean
import com.stm.bledemo.util.CustomUtils
import com.stm.bledemo.util.ShareLogUtil
import com.stm.bledemo.util.ShareLogUtil.deleteLogFile
import com.stm.bledemo.util.ShareLogUtil.readParaUploadMap
import com.stm.bledemo.util.ShareUtil.shareFile
import kotlinx.coroutines.*
import java.io.File
import java.util.concurrent.ConcurrentHashMap


class UploadDirAdapter(var paraList: List<File>, var ctx: Context, var activity: UploadActivity) :
    RecyclerView.Adapter<UploadDirAdapter.ViewHolder>() {
//    companion object {
//        @Volatile
//        var SendMap=ConcurrentHashMap<Int,Int>()
//        var isParaDown=false
//    }

    inner class ViewHolder(val binding: ParaUploadShowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var downX = 0f
        var downY = 0f
        var moveX = 0f
        var moveY = 0f
        var currentMS: Long = 0
        var moveTime: Long = 0

        init {
            with(binding) {
                fileDirShow.setOnTouchListener(object : View.OnTouchListener {
                    @SuppressLint("ClickableViewAccessibility")
                    override fun onTouch(v: View?, event: MotionEvent): Boolean {
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                downX = event.x
                                downY = event.y
                                moveX = 0f;
                                moveY = 0f;
                                currentMS = System.currentTimeMillis();
                            }
                            MotionEvent.ACTION_MOVE -> {
                                moveX += Math.abs(event.x - downX);//X轴距离
                                moveY += Math.abs(event.y - downY);//y轴距离
                                downX = event.x;
                                downY = event.y;
                            }
                            MotionEvent.ACTION_UP -> {
                                moveTime = System.currentTimeMillis() - currentMS

                                //判断是滑动还是点击操作、判断是否继续传递信号
                                return if (moveTime < 500 && moveX < 20 && moveY < 20) { //点击事件
                                    //                    var data = paraList[bindingAdapterPosition]
                                    var data = paraList[bindingAdapterPosition]
                                    var intent: Intent =
                                        Intent(binding.root.context, ParaUploadActivity::class.java)
                                    intent.putExtra("filePath", data.path)
                                    binding.root.context.startActivity(intent)
                                    false
                                } else { //滑动事件
                                    true //返回true,表示不再执行后面的事件
                                }
                                run {
                                    moveY = 0f
                                    moveX = moveY //归零
                                }
                            }
                            else -> {}
                        }
                        return false
                    }
                })

                deleteBnt.setOnClickListener {
                    //显示弹框
                    var data = paraList[bindingAdapterPosition]
                    CommonDialog.Builder(binding.root.context).setTitle("是否删除文件" + data.name + "?")
                        .setPositiveButton("确定", DialogInterface.OnClickListener { p0, p1 ->
                            p0?.dismiss()
                            //删除文件
                            deleteLogFile(data.path)
                            paraList =
                                ShareLogUtil.eachFileRecurse(
                                    binding.root.context,
                                    BLEManager.currentBleName,
                                    "paraUpload"
                                )
                            notifyDataSetChanged()

                        })
                        //点击取消
                        .setNegativeButton(
                            "取消",
                            DialogInterface.OnClickListener { p0, p1 -> p0?.dismiss() })
                        .setWith(0.77f)
                        .create()
                        .show()

                }

                /**
                 * 数据下发
                 */
//                dataIssued.setOnClickListener {
//                    activity.progressDialog!!.show()
//                    activity.status=0
//                    var startTime = System.currentTimeMillis()
//                    //数据发送
//                    var job= BLEManager.scopeSetDeal.launch(start = CoroutineStart.LAZY) {
//                        while (SendMap.isNotEmpty() && System.currentTimeMillis() - startTime < 60000) {
//                            for ((k,v) in SendMap) {
//                                delay(200)
//                                DataDeal().dealSingleWriteData(
//                                    k,
//                                    v
//                                )
//                            }
//                        }
//                    }
//                    GlobalScope.launch() {
//                        var data = paraList[bindingAdapterPosition]
//                        //读取文件
//                        SendMap.clear()
//                        SendMap =readParaUploadMap(data.path)
//                        var dataSend=SendMap.size
//                        //获取数据
//                        isParaDown=true
//                        //不超过两分钟
//                        job.start()
//                        while (activity.status < 100 && System.currentTimeMillis() - startTime < 60000) {
//                            // 获取耗时操作的完成百分比
//                           delay(500)
//                            activity.status =
//                                (((dataSend-SendMap.size).toFloat() / dataSend) * 100).toInt()
//                            activity.mHandler.sendEmptyMessage(0x111)
//                        }
//                        if (activity.status >= 100) {
//                            launch(Dispatchers.Main) {
//                                activity.progressDialog!!.dismiss()
//                                //显示成功的弹框，数据写入
//                                SendMap.clear()
//                                isParaDown=false
//                                if (!activity.isFinishing) {
//                                    CustomUtils.showDialog(ctx, "数据写入成功！")
//                                }
//                            }
//
//                        } else {
//                            launch(Dispatchers.Main) {
//                                activity.progressDialog!!.dismiss()
//                                isParaDown=false
//                                //提示写入失败
//                                SendMap.clear()
//                                if (!activity.isFinishing) {
//                                    CustomUtils.showDialog(ctx, "数据写入失败！")
//                                }
//
//                            }
//                        }
//
//                    }
//
//                }

            }

        }
    }

    //重写的第一个方法，用来给制定加载那个类型的Recycler布局
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ParaUploadShowBinding>(
            inflater,
            R.layout.para_upload_show,
            parent,
            false
        )
        return ViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            val paraEntity = paraList[position]
            paraUploadName.text = paraEntity.name
            paraUploadPath.text = paraEntity.path
        }

    }

    override fun getItemCount(): Int {
        return paraList.size
    }


}
