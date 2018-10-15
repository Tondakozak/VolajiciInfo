package cz.tondakozak.volajiciinfo;

import android.content.*;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Activity with "dialog" about the caller
 */
public class OverlayActivity extends AppCompatActivity{

    // receiver for closing the activity from service
    LocalBroadcastManager mLocalBroadcastManager;
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            closeActivity();
        }
    };

    TextView callerInfo;
    TextView callerSimSlot;


    // Auto hide activity
    TimerTask autoHideTimerTask;
    Timer autoHideTimer;

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
        callerInfo = (TextView)findViewById(R.id.callerInfo);
        callerSimSlot = (TextView)findViewById(R.id.callerSimSlot);

        callerInfo.setText(NotificationInfo.callerOrderSpanned);
        callerSimSlot.setText(NotificationInfo.simSlot);


        // set background alpha
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Resources res = getApplicationContext().getResources();
        final int dialogAlpha = sharedPreferences.getInt(res.getString(R.string.shared_pref_dialog_alpha), res.getInteger(R.integer.def_dialog_alpha));
        View overlayDialog = findViewById(R.id.dialogOverlay);
        overlayDialog.getBackground().setAlpha(dialogAlpha);


        // show or hide dialog name
        final boolean hideDialogTitle = sharedPreferences.getBoolean(res.getString(R.string.shared_pref_hide_dialog_title), res.getBoolean(R.bool.def_hide_dialog_title));
        TextView dialogTitle = findViewById(R.id.dialogTitle);
        dialogTitle.setVisibility((hideDialogTitle)?View.GONE:View.VISIBLE);


        // listener for closing the activity
        ConstraintLayout mydialog = (ConstraintLayout)findViewById(R.id.dialogOverlay);
        mydialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeActivity();
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();


        // start timer for auto hide dialog
        startTimerForAutoHide();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister the receiver
        unregisterReceiver(mBroadcastReceiver);
    }


    /**
     * Close dialog
     */
    public void closeActivity() {
        stopTimerForAutoHide();
        finish();
        Log.d("Overlay", "zavíráme krám");
    }


    /**
     * Start timer for auto hide dialog
     */
    public void startTimerForAutoHide() {


        // if the auto-hiding is set to on
        if (NotificationInfo.autoHideDialog) {
            autoHideTimer = new Timer();

            initializeTimerTask();

            autoHideTimer.schedule(autoHideTimerTask, (long)NotificationInfo.autoHideDialogDelay);
        }


    }

    /**
     * Stop auto hide timer
     */
    public void stopTimerForAutoHide() {
        if (autoHideTimer != null) {
            autoHideTimer.cancel();
            autoHideTimer = null;
        }
    }


    /**
     * Initiate timer task for auto hide dialog
     */
    public void initializeTimerTask() {
        autoHideTimerTask = new TimerTask() {

            public void run() {
                closeActivity();
            }

        };

    }


}
