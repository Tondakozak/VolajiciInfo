package cz.tondakozak.volajiciinfo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * Activity with "dialog" about the caller
 */
public class OverlayActivity extends AppCompatActivity{

    // receiver for closing the activity from service
    LocalBroadcastManager mLocalBroadcastManager;
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    TextView callerName;
    TextView callerInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register a receiver for closing the activity
        registerReceiver(mBroadcastReceiver, new IntentFilter("closeActivity"));

        // add flags allowing to show it on log screen
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        win.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);

        // set view
        setContentView(R.layout.activity_overlay);

        // Set info about the caller
        callerName = (TextView)findViewById(R.id.callerName);
        callerInfo = (TextView)findViewById(R.id.callerInfo);

        callerName.setText(NotificationInfo.callerName);
        callerInfo.setText(NotificationInfo.callerOrder);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister the receiver
        unregisterReceiver(mBroadcastReceiver);
    }
}
