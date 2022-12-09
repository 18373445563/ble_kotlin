package com.stm.bledemo.activity.scan

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.kotlintest.entity.JsonManager
import com.example.kotlintest.util.ClickUtil
//import com.stm.bledemo.BuildConfig
import com.stm.bledemo.dataDeal.BaseInterface
import com.stm.bledemo.R
import com.stm.bledemo.activity.monitor.MonitorActivity
import com.stm.bledemo.activity.otherinfo.DeviceInfoActivity
import com.stm.bledemo.activity.otherinfo.VersionInfoActivity
import com.stm.bledemo.activity.scan.fragment.RSSIFilterFragment
import com.stm.bledemo.ble.BLEManager
import com.stm.bledemo.ble.BLEManager.bAdapter
import com.stm.bledemo.ble.ENABLE_BLUETOOTH_REQUEST_CODE
import com.stm.bledemo.databinding.ActivityScanBinding
import kotlinx.android.synthetic.main.activity_scan.*
import kotlinx.android.synthetic.main.view_drawer.*


class ScanActivity : AppCompatActivity(), ScanAdapter.Delegate, ScanInterface, View.OnClickListener,
    BaseInterface {

    private lateinit var binding: ActivityScanBinding
    private var scanItem: MenuItem? = null

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_scan)

        setSupportActionBar(binding.toolbar)
        setupRecyclerView()

        BLEManager.scanInterface = this
        BLEManager.baseInterface = this
        //检查蓝牙是否开启
        if (bAdapter.isEnabled) {
            BLEManager.startScan(this)
        }
        drawerView_comple.initFragment(savedInstanceState)

        sideslipMenu.setOnClickListener { myDrawer.openDrawer(GravityCompat.START) }
        //设置用户名
        userName.text = intent.getStringExtra("KEY_LOGIN_NAME")
        //解析json
        JsonManager.getParamMenu(this, "paraSetting.json")
        initBnt()
    }

    private fun initBnt() {
        version_info_bnt.setOnClickListener(this)
        device_info_bnt.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        if (!bAdapter.isEnabled) {
            promptEnableBluetooth()
        }
        BLEManager.baseInterface = this
    }

    override fun onStop() {
        super.onStop()
        BLEManager.stopScan()
        scanItem?.setIcon(R.drawable.ic_play)
    }

    /** Permission & Bluetooth Requests */

    // Prompt to Enable BT
    override fun promptEnableBluetooth() {
        if (!bAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            ActivityCompat.startActivityForResult(
                this, enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE, null
            )
        }
    }

    // Request Runtime Permissions (Based on Android Version)
    @SuppressLint("ObsoleteSdkInt")
    override fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    // Rerequest Permissions if Not Given by User (Limit 2)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (BLEManager.hasPermissions(this)) {
            BLEManager.startScan(this)
        } else {
            requestPermissions()
        }
    }

    /** Toolbar Menu */

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)

        scanItem = menu.findItem(R.id.scanItem)
        val item = menu.findItem(R.id.searchItem)
        val searchView = item?.actionView as SearchView

        // Search Item on toolbar expanded/collapsed
        item.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                BLEManager.deviceNameFilter = "meg"
                return true
            }
        })

        // Text entered into searchView on toolbar
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if(newText.isNotEmpty()){
                    BLEManager.scanAdapter?.filter(newText, "name")
                }else{
                    BLEManager.scanAdapter?.filter("meg", "name")
                }
                return true
            }
        })

        return true
    }

    // Item on Toolbar Selected
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (!ClickUtil.isFastClick) {
            Toast.makeText(this, "点击过快！", Toast.LENGTH_SHORT).show()
            return false
        }
        when (item.itemId) {
            R.id.scanItem -> {
                if (BLEManager.isScanning) {
                    BLEManager.stopScan()
                    item.setIcon(R.drawable.ic_play)
                } else {
                    BLEManager.startScan(this)
                    item.setIcon(R.drawable.ic_pause)
                }
            }
            R.id.rssiFilterItem -> {
                RSSIFilterFragment().show(supportFragmentManager, "rssiFilterFragment")
            }
        }

        return false
    }

    /** Recycler View */

    // Sets Up the Recycler View for BLE Scan List
    private fun setupRecyclerView() {
        // Create & Set Adapter
        BLEManager.scanAdapter = ScanAdapter(BLEManager.scanResults, this)

        binding.scanResultsRecyclerView.apply {
            adapter = BLEManager.scanAdapter
            layoutManager = LinearLayoutManager(
                this@ScanActivity,
                RecyclerView.VERTICAL,
                false
            )
            isNestedScrollingEnabled = false
        }

        // Turns Off Update Animation
        val animator = binding.scanResultsRecyclerView.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
    }

    // Connect Button Clicked
    override fun onConnectButtonClick(result: ScanResult) {
        if (BLEManager.isScanning) {
            scanItem?.setIcon(R.drawable.ic_play)
        }

        BLEManager.connect(result, this)
    }

    /** Helper Functions */

    // Go to ConnectionInterface Activity
    override fun startIntent() {
    }

    override fun startToast(message: String) {
        runOnUiThread {
            if (!isFinishing) {
                //显示弹框
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onClick(v: View?) {
        if (v == null) {
            return
        }

        when (v.id) {
            R.id.version_info_bnt -> {
                Intent(this@ScanActivity, VersionInfoActivity::class.java).apply {
                    startActivity(this)
                }
            }
            R.id.device_info_bnt -> {
                Intent(this@ScanActivity, DeviceInfoActivity::class.java).apply {
                    startActivity(this)
                }
            }
        }
    }

    override fun showStatus(message: String, type: Int) {
        if (type == 0) {
            Intent(this@ScanActivity, MonitorActivity::class.java).apply {
                startActivity(this)
            }
        }

    }

}