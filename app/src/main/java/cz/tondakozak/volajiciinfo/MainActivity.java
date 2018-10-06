package cz.tondakozak.volajiciinfo;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    // receiver for closing the activity form service
    LocalBroadcastManager mLocalBroadcastManager;
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //setInfoAboutUpdates();
        }
    };


    Context appContext;
    Resources res;
    SharedPreferences sharedPreferences;
    PeopleDB peopleDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // register receiver for reload
        registerReceiver(mBroadcastReceiver, new IntentFilter(getApplicationContext().getResources().getString(R.string.receiverReload)));

        // set layout
        setContentView(R.layout.activity_main);

        appContext = getApplicationContext();
        res = appContext.getResources();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        peopleDB = new PeopleDB(appContext);

        // Create channel for notification
        createNotificationChannel();

        // check and ask for permission
        checkPermission();

        // If update job is not scheduled, schedule it
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!Util.isJobScheduled(appContext, Util.UPDATE_JOB_ID)) {
                Util.scheduleJob(appContext);
            }
        }


        // if the db is empty, download the data now
        if (peopleDB.getNumberOfPeople() < 1) {
            Util.downloadData(appContext);
        }



    }




    /**
     * Create notification channel
     */
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "mujChannel";
            String description = "Popis channelu";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(getApplicationContext().getResources().getString(R.string.channel_id), name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Check if all required permissions were granted and if not, ask for them
     * @return
     */
    public boolean checkPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.DISABLE_KEYGUARD) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED
                ){

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE,Manifest.permission.DISABLE_KEYGUARD,Manifest.permission.WAKE_LOCK},
                    123);

            return false;
        } else {
            return true;
        }
    }

    /**
     * Result of getting permissions - if not granted, ask again
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 123:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    // permission wasn't granted, ask again
                    checkPermission();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void clickSettingButton(View view) {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }
}
