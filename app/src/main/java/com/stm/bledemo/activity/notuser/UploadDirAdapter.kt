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
                                moveX += Math.abs(event.x - downX);//X?????????
                                moveY += Math.abs(event.y - downY);//y?????????
                                downX = event.x;
                                downY = event.y;
                            }
                            MotionEvent.ACTION_UP -> {
                                moveTime = System.currentTimeMillis() - currentMS

                                //??????????????????????????????????????????????????????????????????
                                return if (moveTime < 500 && moveX < 20 && moveY < 20) { //????????????
                                    //                    var data = paraList[bindingAdapterPosition]
                                    var data = paraList[bindingAdapterPosition]
                                    var intent: Intent =
                                        Intent(binding.root.context, ParaUploadActivity::class.java)
                                    intent.putExtra("filePath", data.path)
                                    binding.root.context.startActivity(intent)
                                    false
                                } else { //????????????
                                    true //??????true,?????????????????????????????????
                                }
                                run {
                                    moveY = 0f
                                    moveX = moveY //??????
                                }
                            }
                            else -> {}
                        }
                        return false
                    }
                })

                deleteBnt.setOnClickListener {
                    //????????????
                    var data = paraList[bindingAdapterPosition]
                    CommonDialog.Builder(binding.root.context).setTitle("??????????????????" + data.name + "?")
                        .setPositiveButton("??????", DialogInterface.OnClickListener { p0, p1 ->
                            p0?.dismiss()
                            //????????????
                            deleteLogFile(data.path)
                            paraList =
                                ShareLogUtil.eachFileRecurse(
                                    binding.root.context,
                                    BLEManager.currentBleName,
                                    "paraUpload"
                                )
                            notifyDataSetChanged()

                        })
                        //????????????
                        .setNegativeButton(
                            "??????",
                            DialogInterface.OnClickListener { p0, p1 -> p0?.dismiss() })
                        .setWith(0.77f)
                        .create()
                        .show()

                }

                /**
                 * ????????????
                 */
//                dataIssued.setOnClickListener {
//                    activity.progressDialog!!.show()
//                    activity.status=0
//                    var startTime = System.currentTimeMillis()
//                    //????????????
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
//                        //????????????
//                        SendMap.clear()
//                        SendMap =readParaUploadMap(data.path)
//                        var dataSend=SendMap.size
//                        //????????????
//                        isParaDown=true
//                        //??????????????????
//                        job.start()
//                        while (activity.status < 100 && System.currentTimeMillis() - startTime < 60000) {
//                            // ????????????????????????????????????
//                           delay(500)
//                            activity.status =
//                                (((dataSend-SendMap.size).toFloat() / dataSend) * 100).toInt()
//                            activity.mHandler.sendEmptyMessage(0x111)
//                        }
//                        if (activity.status >= 100) {
//                            launch(Dispatchers.Main) {
//                                activity.progressDialog!!.dismiss()
//                                //????????????????????????????????????
//                                SendMap.clear()
//                                isParaDown=false
//                                if (!activity.isFinishing) {
//                                    CustomUtils.showDialog(ctx, "?????????????????????")
//                                }
//                            }
//
//                        } else {
//                            launch(Dispatchers.Main) {
//                                activity.progressDialog!!.dismiss()
//                                isParaDown=false
//                                //??????????????????
//                                SendMap.clear()
//                                if (!activity.isFinishing) {
//                                    CustomUtils.showDialog(ctx, "?????????????????????")
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

    //???????????????????????????????????????????????????????????????Recycler??????
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
