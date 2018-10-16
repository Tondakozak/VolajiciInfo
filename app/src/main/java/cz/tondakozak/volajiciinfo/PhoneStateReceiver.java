package cz.tondakozak.volajiciinfo;

import android.Manifest;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

/**
 * Receive state of the phone
 */
public class PhoneStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // handle phone state only if background job is allowed in setting
        if (sharedPreferences.getBoolean(context.getResources().getString(R.string.shared_pref_on_background), true)) {

            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

            // If the phone is ringing
            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                /*
                Bundle extra = intent.getExtras();
                Log.d("PhoneState", extra.toString());
                Log.d("PhoneStateSI", test(context));
                Log.d("PhoneStateSI2", test2(context));


                // send data
                if (sharedPreferences.getBoolean(context.getResources().getString(R.string.shared_pref_send_data), true)) {
                    Log.d("phoneReceiver", "budu posílat data");
                    Util.uploadData(context, extra.toString() + " ; " +test2(context));
                }
                */

                String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                //Toast.makeText(context,"Ringing State Number is -"+incomingNumber,Toast.LENGTH_SHORT).show();

                // notify about caller
                //NotificationInfo.setSimSlot(capturedSimSlot(extras));
                NotificationInfo.setCallerInfo(context, incomingNumber);
                NotificationInfo.showCallerInfo(context);
            } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {

                // If the phone is idle (end of call) close notification activity
                Intent intent1 = new Intent("closeActivity");
                context.sendBroadcast(intent1);
            }
        }
    }

 /*
    public String test(Context context) {
        SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

        try {
            final SubscriptionInfo activeSubscriptionInfoForSimSlotIndex = subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(0);

            activeSubscriptionInfoForSimSlotIndex.getSubscriptionId();

            return activeSubscriptionInfoForSimSlotIndex.toString();
        } catch (SecurityException e) {
            Log.e("test", e.toString());
        }

        return "";

    }
*/

    /*
    public String test2(Context context) {

        SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        TelephonyManager defaultTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String result = "";
        try {
            List<SubscriptionInfo> subscriptionInfos = subscriptionManager.getActiveSubscriptionInfoList();

            for (int subID = 0; subID < subscriptionInfos.size(); subID++) {
                int subscriptionID = subscriptionInfos.get(subID).getSubscriptionId();
                result += "; subscription id: "+subscriptionID;
                TelephonyManager telephonyManager = defaultTelephonyManager.createForSubscriptionId(subscriptionID);

                int callState = telephonyManager.getCallState();
                switch (callState) {
                    case TelephonyManager.CALL_STATE_RINGING:
                        result += ", call state ringing";
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        result += ", call state idle";
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        result += ", call state offhook";
                        break;

                }

                result += " -- "+telephonyManager.toString();
            }

        } catch (SecurityException e) {
            result = e.toString();
        }

        return result;
    }*/



}
