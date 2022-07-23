package ceneax.app.locationx

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import ceneax.app.lib.locationx.LocationX
import ceneax.app.locationx.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val mLocation = LocationX.create()

    private val mPermissionReq = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    @SuppressLint("ClickableViewAccessibility")
    private val onTouchListener: View.OnTouchListener = View.OnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 父节点不拦截子节点
                v.parent.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                // 父节点不拦截子节点
                v.parent.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_UP -> {
                // 父节点拦截子节点
                v.parent.requestDisallowInterceptTouchEvent(false)
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        initView()
        bindEvent()

        mPermissionReq.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))

        LocationX.setLogger {
            runOnUiThread {
                binding.tvInfo.append("$it\n")
                val offset = getTextHintViewHeight(binding.tvInfo)
                if (offset > binding.tvInfo.height) {
                    binding.tvInfo.scrollTo(0, offset - binding.tvInfo.height)
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        binding.tvInfo.movementMethod = ScrollingMovementMethod.getInstance()
        binding.tvInfo.setOnTouchListener(onTouchListener)
    }

    private fun bindEvent() {
        binding.btSingleLoc.setOnClickListener {
            if (!mLocation.isLocationEnabled) {
                showToast("请先打开 GPS 开关")
                return@setOnClickListener
            }
            mLocation.getLocation { loc ->
                if (loc == null) {
                    showToast("未获取到定位信息")
                    return@getLocation
                }
                showToast("定位信息获取成功")
            }
        }
    }

    private fun showToast(content: String) {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show()
    }

    private fun getTextHintViewHeight(view: TextView): Int {
        val layout = view.layout
        val desired = layout.getLineTop(view.lineCount)
        val padding = view.compoundPaddingTop + view.compoundPaddingBottom
        return desired + padding
    }
}