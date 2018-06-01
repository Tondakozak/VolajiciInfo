package cz.tondakozak.volajiciinfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Receive state of the phone
 */
public class PhoneStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        //Toast.makeText(context,"New Call",Toast.LENGTH_LONG).show();

        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

        // If the phone is ringing
        if(state.equals(TelephonyManager.EXTRA_STATE_RINGING)){
            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            //Toast.makeText(context,"Ringing State Number is -"+incomingNumber,Toast.LENGTH_SHORT).show();

            // notify about caller
            NotificationInfo.showCallerInfo(context, incomingNumber);
        } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {

            // If the phone is idle (end of call) close notification activity
            Intent intent1 = new Intent("closeActivity");
            context.sendBroadcast(intent1);
        }

    }
}
