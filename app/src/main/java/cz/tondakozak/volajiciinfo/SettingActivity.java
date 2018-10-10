package cz.tondakozak.volajiciinfo;

import android.content.*;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

public class SettingActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_setting);

        appContext = getApplicationContext();
        res = appContext.getResources();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        peopleDB = new PeopleDB(appContext);




        // set info about updates
        setInfoAboutUpdates();


        // min latency setting
        final EditText minLatencyInput = (EditText)findViewById(R.id.minLatencyInput);
        final int minHourLatency = sharedPreferences.getInt(res.getString(R.string.shared_pref_min_latency), res.getInteger(R.integer.def_min_hour_latency));
        minLatencyInput.setText(minHourLatency+"");

        minLatencyInput.clearFocus();

        // manage updating min latency
        minLatencyInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                int newLatency = 1;
                try {
                    // save the new value to shared preferences
                    newLatency = Integer.parseInt(s.toString());

                } catch (Exception e) {
                    e.printStackTrace();

                }
                // ensure min latency is 1
                if (newLatency < 1) {
                    newLatency = 1;
                    minLatencyInput.setText(""+newLatency);
                }
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(res.getString(R.string.shared_pref_min_latency), newLatency);
                editor.commit();

                // reschedule job
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Util.restartSchedulingJob(appContext);
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
                LayoutInflater li = LayoutInflater.from(SettingActivity.this);
                View promptsView = li.inflate(R.layout.url_prompt, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        SettingActivity.this);

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

                                        // add protocol
                                        newUrl = Util.addHTTPsProtocol(newUrl);
                                        // save to shared preferences
                                        Util.saveURL(newUrl, appContext);


                                        // show url on the screen
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

        Switch switchLaunchOnStart =  findViewById(R.id.switchLauchOnStart);
        Switch switchOnBackground = findViewById(R.id.switchOnBackground);
        Switch switchHideDialog = findViewById(R.id.switchHideDialog);
        Switch switchSendData = findViewById(R.id.switchSendData);

        RadioGroup radioGroupNumberNotFound = findViewById(R.id.radioGroupNotFound);

        EditText delayInput = findViewById(R.id.hideDialogDelayInput);

        setSwitchLaunchOnStart(switchLaunchOnStart);

        setSwitchOnBackground(switchOnBackground, switchLaunchOnStart);

        setSwitchHideDialog(switchHideDialog);
        setHideDialogDelay(delayInput);

        setNumberNotFound(radioGroupNumberNotFound);
        setSwitchSendData(switchSendData);
    }


    /**
     * Set info about updates to the screen
     */
    public void setInfoAboutUpdates() {
        // number of people in DB
        TextView numberOfPeopleText = findViewById(R.id.peopleNumberText);
        numberOfPeopleText.setText(res.getString(R.string.number_of_people_template, ""+peopleDB.getNumberOfPeople()));

        // Last update
        TextView lastUpdateText = (TextView)findViewById(R.id.lastUpdate2);
        final String lastUpdate = sharedPreferences.getString(
                res.getString(
                        R.string.shared_pref_last_update),
                res.getString(
                        R.string.last_update_default));
        lastUpdateText.setText(res.getString(R.string.last_update_template, lastUpdate));

        // set URL
        final TextView urlText = (TextView)findViewById(R.id.textUrl);
        final String urlForData = sharedPreferences.getString(
                res.getString(
                        R.string.shared_pref_url),
                res.getString(
                        R.string.url_default));
        urlText.setText(urlForData);
    }



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



    private void setSwitchLaunchOnStart(Switch mySwitch) {

        // set value
        final boolean onBackground = sharedPreferences.getBoolean(res.getString(R.string.shared_pref_on_background), true);
        /*if (onBackground) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(appContext.getString(R.string.shared_pref_launch_on_start), false);
            editor.commit();
        }*/

        //  set switch disabled if the background process is enabled
        mySwitch.setEnabled(!onBackground);

        final boolean launchOnStart = sharedPreferences.getBoolean(res.getString(R.string.shared_pref_launch_on_start), true);
        mySwitch.setChecked(launchOnStart);

        // set listener
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(appContext.getString(R.string.shared_pref_launch_on_start), isChecked);
                editor.commit();

                if (isChecked) {
                    // nastavit spouštění při startu
                } else {
                    // zastavit spouštění při startu
                }

            }
        });
    }

    private void setSwitchOnBackground(Switch mySwitch, final Switch switchLaunchOnStart) {
        // set value
        final boolean onBackground = sharedPreferences.getBoolean(res.getString(R.string.shared_pref_on_background), true);
        mySwitch.setChecked(onBackground);

        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(appContext.getString(R.string.shared_pref_on_background), isChecked);
                editor.commit();

                if (isChecked) {
                    // schedule update job
                    Util.scheduleJob(appContext);
                } else {
                    // cancel scheduled job
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Util.cancelScheduledJob(appContext, Util.UPDATE_JOB_ID);
                    }
                }

                // set switch for launch on start false if background process is enabled and disable the switch
                editor.putBoolean(appContext.getString(R.string.shared_pref_launch_on_start), !isChecked);
                editor.commit();


                //  set switch disabled if the background process is enabled
                switchLaunchOnStart.setEnabled(!isChecked);
                switchLaunchOnStart.setChecked(!isChecked);
            }
        });
    }

    private void setSwitchHideDialog(Switch mySwitch) {
        // set value
        final boolean hideDialog = sharedPreferences.getBoolean(res.getString(R.string.shared_pref_auto_hide_dialog), false);
        mySwitch.setChecked(hideDialog);

        final LinearLayout linearLayoutHideDelay = (LinearLayout) findViewById(R.id.linearLayoutHideDelay);
        linearLayoutHideDelay.setVisibility((hideDialog)?View.VISIBLE:View.GONE);

        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(appContext.getString(R.string.shared_pref_auto_hide_dialog), isChecked);
                editor.commit();


                if (isChecked) {
                    linearLayoutHideDelay.setVisibility(View.VISIBLE);
                } else {
                    linearLayoutHideDelay.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setSwitchSendData(Switch mySwitch) {
        // set value
        final boolean sendData = sharedPreferences.getBoolean(res.getString(R.string.shared_pref_send_data), true);
        mySwitch.setChecked(sendData);


        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(appContext.getString(R.string.shared_pref_send_data), isChecked);
                editor.commit();
            }
        });
    }

    private void setHideDialogDelay(EditText delayInput) {
        // min latency setting

        final int delay = sharedPreferences.getInt(res.getString(R.string.shared_pref_auto_hide_dialog_delay), res.getInteger(R.integer.def_auto_hide_dialog_delay));
        delayInput.setText(delay+"");

        delayInput.clearFocus();

        // manage updating min latency
        delayInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                int newDelay = 1;
                try {
                    // save the new value to shared preferences
                    newDelay = Integer.parseInt(s.toString());

                } catch (Exception e) {
                    e.printStackTrace();

                }

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(res.getString(R.string.shared_pref_auto_hide_dialog_delay), newDelay);
                editor.commit();

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) { }
        });
    }

    private void setNumberNotFound(RadioGroup rGroup) {

        // set saved value
        final int numberNotFoundValue= sharedPreferences.getInt(res.getString(R.string.shared_pref_number_not_found), 0);
        RadioButton checkedRadioButton = null;
        switch (numberNotFoundValue) {
            case Util.DONT_DISPLAY_DIALOG:
                checkedRadioButton = findViewById(R.id.radioButtonDontDisplayDialog);
                break;
            case Util.SHOW_NOT_FOUND_MESSAGE:
                checkedRadioButton = findViewById(R.id.radioButtonDialogNotFoundMessage);
                break;
        }

        checkedRadioButton.setChecked(true);

        rGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // This will get the radiobutton that has changed in its check state
                RadioButton checkedRadioButton = (RadioButton)group.findViewById(checkedId);
                // This puts the value (true/false) into the variable
                boolean isChecked = checkedRadioButton.isChecked();
                // If the radiobutton that has changed in check state is now checked...
                if (isChecked) {

                    int newNumberNotFound = -1;
                    if (checkedId == R.id.radioButtonDialogNotFoundMessage) {
                        newNumberNotFound = Util.SHOW_NOT_FOUND_MESSAGE;
                    } else if (checkedId == R.id.radioButtonDontDisplayDialog) {
                        newNumberNotFound = Util.DONT_DISPLAY_DIALOG;
                    }

                    Log.d("settingRadio", ""+newNumberNotFound);
                    if (newNumberNotFound != -1) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt(res.getString(R.string.shared_pref_number_not_found), newNumberNotFound);
                        editor.commit();
                    }
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister the receiver
        unregisterReceiver(mBroadcastReceiver);
    }
}
