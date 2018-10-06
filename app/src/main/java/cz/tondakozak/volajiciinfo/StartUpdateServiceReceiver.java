package cz.tondakozak.volajiciinfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Start service after reboot
 */
public class StartUpdateServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(context.getResources().getString(R.string.shared_pref_launch_on_start), true)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(context.getString(R.string.shared_pref_on_background), true);
            editor.commit();
        }

        Util.scheduleJob(context);
    }
}
