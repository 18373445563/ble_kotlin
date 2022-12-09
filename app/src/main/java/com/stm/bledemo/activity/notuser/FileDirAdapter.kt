package com.stm.bledemo.activity.notuser

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.*
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlintest.util.CommonDialog
import com.stm.bledemo.R
import com.stm.bledemo.activity.oplog.ParaOpLogActivity
import com.stm.bledemo.ble.BLEManager
import com.stm.bledemo.databinding.FileDirShowBinding
import com.stm.bledemo.util.ShareLogUtil
import com.stm.bledemo.util.ShareLogUtil.deleteLogFile
import com.stm.bledemo.util.ShareUtil.shareFile
import java.io.File


class FileDirAdapter(var paraList: List<File>, var ctx: Context) :
    RecyclerView.Adapter<FileDirAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: FileDirShowBinding) :
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
                                downY = event.y;
                            }
                            MotionEvent.ACTION_UP -> {
                                moveTime = System.currentTimeMillis() - currentMS

                                //判断是滑动还是点击操作、判断是否继续传递信号
                                return if (moveTime < 500 && moveX < 20 && moveY < 20) { //点击事件
                                    //                    var data = paraList[bindingAdapterPosition]
                                    var data = paraList[bindingAdapterPosition]
                                    var intent: Intent =
                                        Intent(binding.root.context, ParaOpLogActivity::class.java)
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
                            //更新list
                            paraList =
                                ShareLogUtil.eachFileRecurse(
                                    binding.root.context,
                                    BLEManager.currentBleName,
                                    "log"
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
                 * 文件分享
                 */
                dataShare.setOnClickListener {
                    var data = paraList[bindingAdapterPosition]
                    shareFile(binding.root.context, data.path)
                }
            }
        }
    }

    //重写的第一个方法，用来给制定加载那个类型的Recycler布局
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<FileDirShowBinding>(
            inflater,
            R.layout.file_dir_show,
            parent,
            false
        )
        return ViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            val paraEntity = paraList[position]
            fileName.text = paraEntity.name
            filePath.text = paraEntity.path
        }

    }

    override fun getItemCount(): Int {
        return paraList.size
    }

    /**
     * 使用
     *     //        var recyclerView=findViewById<RecyclerView>(R.id.fileList)
    //        var layoutManager = LinearLayoutManager(this)
    //        recyclerView.layoutManager=layoutManager
    //        var adapter=FileDirAdapter(list,this)
    //        recyclerView.adapter=adapter
     */



}
