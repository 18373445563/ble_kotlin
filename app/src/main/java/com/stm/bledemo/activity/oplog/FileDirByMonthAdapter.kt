package com.stm.bledemo.activity.oplog

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.kotlintest.util.CommonDialog
import com.stm.bledemo.R
import com.stm.bledemo.ble.BLEManager
import com.stm.bledemo.util.ShareLogUtil
import com.stm.bledemo.util.ShareUtil
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter
import java.io.File
import kotlin.math.abs


class FileDirByMonthAdapter(var context: Context) : BaseAdapter(),
    StickyListHeadersAdapter {
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
                .inflate(R.layout.item_month_body_list, parent, false)
            bodyHolder = BodyHolder(view)
            view.tag = bodyHolder
        } else {
            bodyHolder = view.tag as BodyHolder
        }
        //设置数据
        bodyHolder!!.bodyTv.text = monthList[position].name
        bodyHolder!!.pathTv.text = monthList[position].path

        bodyHolder!!.fileDirShow.setOnTouchListener(object : View.OnTouchListener {
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
                            var intent = Intent(context, ParaOpLogActivity::class.java)
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

        bodyHolder!!.deleteBnt.setOnClickListener {
            //显示弹框
            var data = monthList[position]
            CommonDialog.Builder(context).setTitle("是否删除文件" + data.name + "?")
                .setPositiveButton("确定", DialogInterface.OnClickListener { p0, p1 ->
                    p0?.dismiss()
                    //删除文件
                    ShareLogUtil.deleteLogFile(data.path)
                    //更新list
                   var  paraList =
                        ShareLogUtil.eachFileRecurse(
                            context,
                            BLEManager.currentBleName,
                            "log"
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
         * 文件分享
         */
        bodyHolder!!.dataShare.setOnClickListener {
            var data = monthList[position]
            ShareUtil.shareFile(context, data.path)
        }

        return view!!
    }

    //绑定头部的数据
    override fun getHeaderView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        var headHolder: HeadHolder? = null
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                .inflate(R.layout.item_month_header_list, parent, false)
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
            headTv = itemHeadView.findViewById<View>(R.id.item_head_tv) as TextView
        }
    }

    //内容的内部类
    internal inner class BodyHolder(itemBodyView: View) {
        var bodyTv: TextView
        var pathTv: TextView
        var fileDirShow: LinearLayout
        var dataShare: TextView
        var deleteBnt: TextView

        init {
            bodyTv = itemBodyView.findViewById<View>(R.id.fileName) as TextView
            pathTv = itemBodyView.findViewById<View>(R.id.filePath) as TextView
            fileDirShow = itemBodyView.findViewById<View>(R.id.file_dir_show) as LinearLayout
            dataShare= itemBodyView.findViewById<View>(R.id.data_share) as TextView
            deleteBnt= itemBodyView.findViewById<View>(R.id.delete_bnt) as TextView
        }
    }
}