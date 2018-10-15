package cz.tondakozak.volajiciinfo;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

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

                String slot = (String) intent.toString();

                Log.d("bundle", slot);
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    Log.d("SIM_SLOT"," Slot Number "+capturedSimSlot(extras));
                }
                if (sharedPreferences.getBoolean(context.getResources().getString(R.string.shared_pref_send_data), true)) {
                    Log.d("phoneReceiver", "budu posílat data");
                    Util.uploadData(context, extras.toString() + " ; " + capturedSimSlot(extras) + " ; " + test(context));
                }
                String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                //Toast.makeText(context,"Ringing State Number is -"+incomingNumber,Toast.LENGTH_SHORT).show();

                // notify about caller
                NotificationInfo.setSimSlot(capturedSimSlot(extras));
                NotificationInfo.setCallerInfo(context, incomingNumber);
                NotificationInfo.showCallerInfo(context);
            } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {

                // If the phone is idle (end of call) close notification activity
                Intent intent1 = new Intent("closeActivity");
                context.sendBroadcast(intent1);
            }
        }
    }



    public int capturedSimSlot(Bundle bundle){

        int whichSIM =-1;
        if (bundle.containsKey("subscription")) {
            whichSIM = bundle.getInt("subscription");
        }
        if(whichSIM >=0 && whichSIM < 5){
            /*In some device Subscription id is return as subscriber id*/
            //sim = ""+whichSIM;
            
        }else{
            if (bundle.containsKey("simId")) {
                whichSIM = bundle.getInt("simId");
            }else if (bundle.containsKey("com.android.phone.extra.slot")) {
                whichSIM = bundle.getInt("com.android.phone.extra.slot");
            }else{
                String keyName = "";
                for(String key : bundle.keySet()){
                    if(key.contains("sim"))
                        keyName =key;
                }
                if (bundle.containsKey(keyName)) {
                    whichSIM = bundle.getInt(keyName);
                }
            }
        }
        return whichSIM;
    }


    public String test(Context context) {
        SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

        try {
            final SubscriptionInfo activeSubscriptionInfoForSimSlotIndex = subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(0);

            return activeSubscriptionInfoForSimSlotIndex.getNumber();
        } catch (SecurityException e) {
            Log.e("test", e.toString());
        }

        return "";

    }

}
