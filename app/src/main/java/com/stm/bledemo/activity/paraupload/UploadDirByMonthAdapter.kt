package com.stm.bledemo.activity.paraupload

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
import com.stm.bledemo.activity.oplog.ParaOpLogActivity
import com.stm.bledemo.activity.parasetting.ParaSettingActivity
import com.stm.bledemo.ble.BLEManager
import com.stm.bledemo.databinding.ParaUploadShowBinding
import com.stm.bledemo.entity.ParaUploadBean
import com.stm.bledemo.util.CustomUtils
import com.stm.bledemo.util.ShareLogUtil
import com.stm.bledemo.util.ShareLogUtil.deleteLogFile
import com.stm.bledemo.util.ShareLogUtil.readParaUploadMap
import com.stm.bledemo.util.ShareUtil
import com.stm.bledemo.util.ShareUtil.shareFile
import kotlinx.coroutines.*
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs


class UploadDirByMonthAdapter(var context: Context, var activity: UploadActivity) : BaseAdapter(),
    StickyListHeadersAdapter {
    companion object {
        @Volatile
        var SendMap=ConcurrentHashMap<Int,Int>()
        var isParaDown=false
    }
    private var monthList: List<File> = ArrayList()
    fun setItems(monthList: List<File>) {
        this.monthList = monthList
        notifyDataSetChanged()
    }

    //设置数据的个数
    override fun getCount(): Int {
        return monthList.size
    }

    //设置item的条数
    override fun getItem(i: Int): Any {
        return monthList[i]
    }

    //获得相应数据集合中特定位置的数据项
    override fun getItemId(i: Int): Long {
        return i.toLong()
    }


    //获得头部相应数据集合中特定位置的数据项
    override fun getHeaderId(position: Int): Long {
        var str = monthList[position].name.toString().split("-")
        var yearmonth = str[0] + str[1]
        return yearmonth.toLong()
    }

    //    //绑定内容的数据
    @SuppressLint("ClickableViewAccessibility")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var downX = 0f
        var downY = 0f
        var moveX = 0f
        var moveY = 0f
        var currentMS: Long = 0
        var moveTime: Long = 0
        var view = convertView
        var bodyHolder: BodyHolder? = null
        if (view == null) {
            view = LayoutInflater.from(context)
                .inflate(R.layout.item_upload_month_body_list, parent, false)
            bodyHolder = BodyHolder(view)
            view.tag = bodyHolder
        } else {
            bodyHolder = view.tag as BodyHolder
        }
        //设置数据
        bodyHolder!!.paraUploadName.text = monthList[position].name
        bodyHolder!!.paraUploadPath.text = monthList[position].path

        bodyHolder!!.fileUploadShow.setOnTouchListener(object : View.OnTouchListener {
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
                        moveX += abs(event.x - downX);//X轴距离
                        moveY += abs(event.y - downY);//y轴距离
                        downX = event.x;
                        downY = event.y;
                    }
                    MotionEvent.ACTION_UP -> {
                        moveTime = System.currentTimeMillis() - currentMS

                        //判断是滑动还是点击操作、判断是否继续传递信号
                        return if (moveTime < 500 && moveX < 20 && moveY < 20) { //点击事件
                            //                    var data = paraList[bindingAdapterPosition]
                            var data = monthList[position]
                            var intent: Intent =
                                Intent(context, ParaUploadActivity::class.java)
                            intent.putExtra("filePath", data.path)
                            context.startActivity(intent)
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
        bodyHolder!!.deleteUploadBnt.setOnClickListener {
            //显示弹框
            var data =monthList[position]
            CommonDialog.Builder(context).setTitle("是否删除文件" + data.name + "?")
                .setPositiveButton("确定", DialogInterface.OnClickListener { p0, p1 ->
                    p0?.dismiss()
                    //删除文件
                    deleteLogFile(data.path)
                    var paraList =
                        ShareLogUtil.eachFileRecurse(
                            context,
                            BLEManager.currentBleName,
                            "paraUpload"
                        )
                   setItems(paraList)

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
        bodyHolder!!.dataIssued.setOnClickListener {
            activity.progressDialog!!.show()
            activity.status=0
            var startTime = System.currentTimeMillis()
            //数据发送
            var job= BLEManager.scopeSetDeal.launch(start = CoroutineStart.LAZY) {
                while (SendMap.isNotEmpty() && System.currentTimeMillis() - startTime < 60000) {
                    for ((k,v) in SendMap) {
                        delay(200)
                        DataDeal().dealSingleWriteData(
                            k,
                            v
                        )
                    }
                }
            }
            GlobalScope.launch() {
                var data =  monthList[position]
                //读取文件
                SendMap.clear()
               SendMap =readParaUploadMap(data.path)
                var dataSend= SendMap.size
                //获取数据
                isParaDown =true
                //不超过两分钟
                job.start()
                while (activity.status < 100 && System.currentTimeMillis() - startTime < 60000) {
                    // 获取耗时操作的完成百分比
                    delay(500)
                    activity.status =
                        (((dataSend- SendMap.size).toFloat() / dataSend) * 100).toInt()
                    activity.mHandler.sendEmptyMessage(0x111)
                }
                if (activity.status >= 100) {
                    launch(Dispatchers.Main) {
                        activity.progressDialog!!.dismiss()
                        //显示成功的弹框，数据写入
                        SendMap.clear()
                        isParaDown =false
                        if (!activity.isFinishing) {
                            CustomUtils.showDialog(context, "数据写入成功！")
                        }
                    }

                } else {
                    launch(Dispatchers.Main) {
                        activity.progressDialog!!.dismiss()
                        isParaDown =false
                        //提示写入失败
                        SendMap.clear()
                        if (!activity.isFinishing) {
                            CustomUtils.showDialog(context, "数据写入失败！")
                        }

                    }
                }

            }

        }

        return view!!
    }

    //绑定头部的数据
    override fun getHeaderView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        var headHolder: HeadHolder? = null
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                .inflate(R.layout.item_upload_month_header_list, parent, false)
            headHolder = HeadHolder(convertView)
            convertView.tag = headHolder
        } else {
            headHolder = convertView.tag as HeadHolder
        }
        //设置数据
        var str = monthList[position].name.toString().split("-")
        var yearmonth = str[0] + str[1]
        headHolder!!.headTv.text = yearmonth
        return convertView!!
    }

    //头部的内部类
    internal inner class HeadHolder(itemHeadView: View) {
        var headTv: TextView

        init {
            headTv = itemHeadView.findViewById<View>(R.id.item_upload_head_tv) as TextView
        }
    }

    //内容的内部类
    internal inner class BodyHolder(itemBodyView: View) {
        var paraUploadName: TextView
        var paraUploadPath: TextView
        var fileUploadShow: LinearLayout
        var dataIssued: TextView
        var deleteUploadBnt: TextView

        init {
            paraUploadName = itemBodyView.findViewById<View>(R.id.paraUploadName) as TextView
            paraUploadPath = itemBodyView.findViewById<View>(R.id.paraUploadPath) as TextView
            fileUploadShow = itemBodyView.findViewById<View>(R.id.file_upload_show) as LinearLayout
            dataIssued= itemBodyView.findViewById<View>(R.id.data_Issued) as TextView
            deleteUploadBnt= itemBodyView.findViewById<View>(R.id.delete_upload_bnt) as TextView
        }
    }
}