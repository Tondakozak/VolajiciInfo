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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // receiver for closing the activity form service
    LocalBroadcastManager mLocalBroadcastManager;
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setInfoAboutUpdates();
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
            if (!Util.isJobSheduled(appContext, Util.UPDATE_JOB_ID)) {
                Util.scheduleJob(appContext);
            }
        }


        // if the db is empty, download the data now
        if (peopleDB.getNumberOfPeople() < 1) {
            Util.downloadData(appContext);
        }

       // set info about updates
        setInfoAboutUpdates();


        // min latency setting
        final EditText minLatencyInput = (EditText)findViewById(R.id.minLatencyInput);
        final int minHourLatency = sharedPreferences.getInt(res.getString(R.string.shared_pref_min_latency), 20);
        minLatencyInput.setText(minHourLatency+"");

        // manage updating min latency
        minLatencyInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    // save the new value to shared preferences
                    int newLatency = Integer.parseInt(s.toString());
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt(res.getString(R.string.shared_pref_min_latency), newLatency);
                    editor.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) { }
        });



        // Url for data update
        final TextView urlText = (TextView)findViewById(R.id.textUrl);
        final String urlForData = sharedPreferences.getString(
                res.getString(
                        R.string.shared_pref_url),
                res.getString(
                        R.string.url_default));
        urlText.setText(urlForData);

        Button editUrlButton = (Button)findViewById(R.id.buttonEditUrl);

        // Update the url
        editUrlButton.setOnClickListener(new View.OnClickListener() { // set onclick - show dialog with input
            @Override
            public void onClick(View v) {
                // get prompts view
                LayoutInflater li = LayoutInflater.from(MainActivity.this);
                View promptsView = li.inflate(R.layout.url_prompt, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        MainActivity.this);

                // set prompts to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                final EditText userInput = (EditText) promptsView
                        .findViewById(R.id.dialogUrl);

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        // save new URL
                                        String newUrl = userInput.getText().toString();

                                        // If the new url is the empty string, set it to the default value
                                        if (newUrl.equals("")) {
                                            newUrl = res.getString(R.string.url_default);
                                        }
                                        // save to shared preferences
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString(res.getString(R.string.shared_pref_url), newUrl);
                                        editor.commit();


                                        urlText.setText(newUrl);

                                        // start downloading the data from new url
                                        Util.downloadData(appContext);

                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show dialog
                alertDialog.show();
            }
        });
    }


    /**
     * Set info about updates to the screen
     */
    public void setInfoAboutUpdates() {
        // number of people in DB
        TextView numberOfPeopleText = (TextView)findViewById(R.id.peopleNumber);
        numberOfPeopleText.setText(res.getString(R.string.number_of_people_template, ""+peopleDB.getNumberOfPeople()));

        // Last update
        TextView lastUpdateText = (TextView)findViewById(R.id.lastUpdate2);
        final String lastUpdate = sharedPreferences.getString(
                res.getString(
                        R.string.shared_pref_last_update),
                res.getString(
                        R.string.last_update_default));
        lastUpdateText.setText(res.getString(R.string.last_update_template, lastUpdate));
    }

/*
    public void startNotifOnclick(View view) {
        Log.d("manin", "Start Service");

        Intent intent = new Intent(this, OverlayActivity.class);
        startActivity(intent);
    }
*/

    /**
     * Listener for button for deleting data
     * @param view
     */
    public void deleteDBOnclick(View view) {
        peopleDB.deleteData();

        Toast.makeText(appContext, "Data byla smazána",Toast.LENGTH_LONG).show();
        setInfoAboutUpdates();
    }

    /**
     * Listener for button for updating data manually
     * @param view
     */
    public void startDownloadOnclick(View view) {
        Util.downloadData(getApplicationContext());
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister the receiver
        unregisterReceiver(mBroadcastReceiver);
    }
}
