package ceneax.app.locationx

import android.Manifest
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import ceneax.app.lib.locationx.LXLog
import ceneax.app.lib.locationx.LocationX
import ceneax.app.locationx.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val mLocationX = LocationX.create()

    private val mPermissionReq = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        bindEvent()

        mPermissionReq.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
    }

    private fun bindEvent() {
        binding.btSingleLoc.setOnClickListener {
            if (!mLocationX.isLocationEnabled) {
                showToast("请先打开 GPS 开关")
                return@setOnClickListener
            }
            mLocationX.getLocation(10000) { loc ->
                if (loc == null) {
                    showToast("未获取到定位信息")
                    return@getLocation
                }
                LXLog.i(loc.toString())
                showToast("定位信息获取成功")
            }
        }
    }

    private fun showToast(content: String) {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show()
    }
}