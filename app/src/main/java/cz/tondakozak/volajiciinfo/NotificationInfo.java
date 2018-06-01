package cz.tondakozak.volajiciinfo;

import android.annotation.TargetApi;
import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * Holds information about current caller
 * Find the caller and start the Overlay Activity
 */
public class NotificationInfo extends Fragment{
    public static String callerName = "No name";
    public static String callerOrder = "No Info";

    /**
     * Show info about caller - find number in the db and start Overlay Activity
     * @param context
     * @param tel
     */
    public static void showCallerInfo(Context context, String tel) {
        // get info about caller form db
        PeopleDB peopleDB = new PeopleDB(context);
        Cursor caller = peopleDB.getMan(tel);

        if (caller.getCount() > 0) { // if the number is in DB
            caller.moveToFirst();
            String name = caller.getString(caller.getColumnIndex("firstname")) + " "+caller.getString(caller.getColumnIndex("surname"));
            String info = caller.getString(caller.getColumnIndex("orderInfo")) + "\n "
                    + caller.getString(caller.getColumnIndex("info"));

            callerName = name;
            callerOrder = info;


        } else {
            // if the number was not found
            callerName = "Neznámý volající";
            callerOrder = "";
        }

        // start activity for showing info
        Intent i = new Intent(context, OverlayActivity.class);
        //i.putExtras(intent);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        try {
            // Wait 1 second to ensure the activity will be over native phone app
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        context.startActivity(i);
    }

    /*
    public static void setCallerInfo(Context context, String tel) {
        PeopleDB peopleDB = new PeopleDB(context);

        Cursor caller = peopleDB.getMan(tel);

        if (caller.getCount() > 0) {
            caller.moveToFirst();
            String name = caller.getString(caller.getColumnIndex("firstname")) + " "+caller.getString(caller.getColumnIndex("surname"));
            String info = caller.getString(caller.getColumnIndex("orderInfo")) + "\n "
                    + caller.getString(caller.getColumnIndex("info"));
            notify(context, name, info);
        } else {
            notify(context, "Not found", "Info not found");
        }
    }

*/

/*
    public static void notify(Context context, String name, String info) {
        //dialog(context);

        int numberOfShowing = 5;
        for (int showId = 0; showId < numberOfShowing; showId++) {
            Toast.makeText(context, "Volá: "+name+" \n info: "+info,Toast.LENGTH_LONG).show();
        }
        createNotification(context, name, info);
    }


    public static void dialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Test dialog");
        builder.setIcon(R.drawable.ic_stat_info);
        builder.setMessage("Content");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //Do something
                        dialog.dismiss();
                    }
                    });

        builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alert.show();
    }*/

    /**
     * The unique identifier for this type of notification.
     */
    /*
    private static final String NOTIFICATION_TAG = "CallerInfo";
    private static final String CHANNEL_ID = "222";
*/


    /**
     * Shows the notification, or updates a previously shown notification of
     * this type, with the given parameters.
     * <p>
     * TODO: Customize this method's arguments to present relevant content in
     * the notification.
     * <p>
     * TODO: Customize the contents of this method to tweak the behavior and
     * presentation of info notifications. Make
     * sure to follow the
     * <a href="https://developer.android.com/design/patterns/notifications.html">
     * Notification design guidelines</a> when doing so.
     *
     * @see #cancel(Context)
     */
    /*
    public static void createNotification(final Context context,
                              final String name, final String info
                              ) {
        final Resources res = context.getResources();

        // This image is used as the notification's large icon (thumbnail).
        // TODO: Remove this if your notification has no relevant thumbnail.
        //final Bitmap picture = BitmapFactory.decodeResource(res, R.drawable.example_picture);


        final String ticker = name;
        final String title = res.getString(
                R.string.info_notification_title_template, name);
        final String text = res.getString(
                R.string.info_notification_placeholder_text_template, info);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getResources().getString(R.string.channel_id))

                // Set appropriate defaults for the notification light, sound,
                // and vibration.
                .setDefaults(Notification.DEFAULT_ALL)

                // Set required fields, including the small icon, the
                // notification title, and text.
                .setSmallIcon(R.drawable.ic_stat_info)
                .setContentTitle(title)
                .setContentText(text)

                // All fields below this line are optional.

                // Use a default priority (recognized on devices running Android
                // 4.1 or later)
                .setPriority(NotificationCompat.PRIORITY_MAX)

                // Provide a large icon, shown with the notification in the
                // notification drawer on devices running Android 3.0 or later.
                //.setLargeIcon(picture)

                // Set ticker text (preview) information for this notification.
                .setTicker(ticker)

                // Show a number. This is useful when stacking notifications of
                // a single type.
                //.setNumber(number)

                // If this notification relates to a past or upcoming event, you
                // should set the relevant time information using the setWhen
                // method below. If this call is omitted, the notification's
                // timestamp will by set to the time at which it was shown.
                // TODO: Call setWhen if this notification relates to a past or
                // upcoming event. The sole argument to this method should be
                // the notification timestamp in milliseconds.
                //.setWhen(...)

                // Set the pending intent to be initiated when the user touches
                // the notification.
               */
                /*.setContentIntent(
                        PendingIntent.getActivity(
                                context,
                                0,
                                new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com")),
                                PendingIntent.FLAG_UPDATE_CURRENT))
*/
                // Show expanded text content on devices running Android 4.1 or
                // later.
    /*
    .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(text)
                        .setBigContentTitle(title)
                        .setSummaryText(name+" "+info))

                // Example additional actions for this notification. These will
                // only show on devices running Android 4.1 or later, so you
                // should ensure that the activity in this notification's
                // content intent provides access to the same actions in
                // another way.
                */
                /*.addAction(
                        R.drawable.ic_action_stat_share,
                        res.getString(R.string.action_share),
                        PendingIntent.getActivity(
                                context,
                                0,
                                Intent.createChooser(new Intent(Intent.ACTION_SEND)
                                        .setType("text/plain")
                                        .putExtra(Intent.EXTRA_TEXT, "Dummy text"), "Dummy title"),
                                PendingIntent.FLAG_UPDATE_CURRENT))
                .addAction(
                        R.drawable.ic_action_stat_reply,
                        res.getString(R.string.action_reply),
                        null)
*/
                /*
                // Automatically dismiss the notification when it is touched.
                .setAutoCancel(true);

        notify(context, builder.build());
    }





    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private static void notify(final Context context, final Notification notification) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.notify(NOTIFICATION_TAG, 0, notification);
        } else {
            nm.notify(NOTIFICATION_TAG.hashCode(), notification);
        }
    }
*/
    /**
     * Cancels any notifications of this type previously shown using
     * {@link #notify(Context, String, int)}.
     */
    /*
    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public static void cancel(final Context context) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.cancel(NOTIFICATION_TAG, 0);
        } else {
            nm.cancel(NOTIFICATION_TAG.hashCode());
        }
    }*/
}
