package zsx.volumebarplus

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat

class MainActivity : AppCompatActivity() {

    private lateinit var audioManager: AudioManager
    private var isPortrait: Boolean = true
    private var isNormalPortrait: Boolean = true
    private var isNormalLandscape: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        updateOrientation()

        // 启动前台服务
        startForegroundService()

        // 请求必要的权限
        requestPermissions()

        // 创建透明悬浮窗
        createOverlayWindow()
    }

    private fun startForegroundService() {
        val serviceIntent = Intent(this, ForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun requestPermissions() {
        // 请求通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                AlertDialog.Builder(this)
                    .setTitle("权限请求")
                    .setMessage("请授予通知权限以确保应用程序正常运行。")
                    .setPositiveButton("确定") { _, _ ->
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                        startActivity(intent)
                    }
                    .setNegativeButton("取消", null)
                    .show()
            }
        }

        // 请求悬浮窗权限
        if (!Settings.canDrawOverlays(this)) {
            AlertDialog.Builder(this)
                .setTitle("权限请求")
                .setMessage("请授予悬浮窗权限以确保应用程序正常运行。")
                .setPositiveButton("确定") { _, _ ->
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                }
                .setNegativeButton("取消", null)
                .show()
        }
    }

    private fun createOverlayWindow() {
        if (Settings.canDrawOverlays(this)) {
            val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val params = WindowManager.LayoutParams(
                1, 1,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                -3
            )

            val overlayView = LinearLayout(this).apply {
                layoutParams = ViewGroup.LayoutParams(1, 1)
            }

            wm.addView(overlayView, params)
        }
    }

    // 更新屏幕方向信息
    private fun updateOrientation() {
        val rotation = windowManager.defaultDisplay.rotation
        val orientation = resources.configuration.orientation

        isPortrait = orientation == Configuration.ORIENTATION_PORTRAIT
        isNormalPortrait = rotation == 0
        isNormalLandscape = rotation == 1
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateOrientation()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                handleVolumeKey(isVolumeUp = true)
                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                handleVolumeKey(isVolumeUp = false)
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun handleVolumeKey(isVolumeUp: Boolean) {
        when {
            isPortrait && isNormalPortrait -> {
                if (isVolumeUp) {
                    increaseVolume()
                } else {
                    decreaseVolume()
                }
            }
            isPortrait && !isNormalPortrait -> {
                if (isVolumeUp) {
                    decreaseVolume()
                } else {
                    increaseVolume()
                }
            }
            !isPortrait && isNormalLandscape -> {
                if (isVolumeUp) {
                    decreaseVolume()
                } else {
                    increaseVolume()
                }
            }
            !isPortrait && !isNormalLandscape -> {
                if (isVolumeUp) {
                    increaseVolume()
                } else {
                    decreaseVolume()
                }
            }
        }
    }

    private fun increaseVolume() {
        audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
    }

    private fun decreaseVolume() {
        audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
    }
}
