package zsx.volumebarplus

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ScreenOrientationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val foregroundServiceIntent = Intent(context, ForegroundService::class.java)
        context.startService(foregroundServiceIntent)
    }
}
