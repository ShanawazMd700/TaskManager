package com.example.taskmanager;

import android.content.*;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            // You could re-register alarms here if needed in future
            Log.d("BootReceiver", "Boot completed detected");
        }
    }
}
