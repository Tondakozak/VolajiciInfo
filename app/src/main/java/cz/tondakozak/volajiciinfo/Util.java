package cz.tondakozak.volajiciinfo;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;
import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Util {
    // id for update job
    public static final int UPDATE_JOB_ID = 1;

    /**
     * Check if the job with given ID is scheduled
     * @param context
     * @param jobId
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static boolean isJobSheduled(Context context, int jobId) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService( Context.JOB_SCHEDULER_SERVICE );
        return scheduler.getPendingJob(jobId) != null;
    }


    /**
     * Schedule new job for updating service
     * @param context
     */
    public static void scheduleJob(Context context) {
        // get min latency from shared preferences (20 is default value)
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int minHourLatency = sharedPreferences.getInt(context.getResources().getString(R.string.shared_pref_min_latency), 20);

        // create a job
        ComponentName serviceComponent = new ComponentName(context, UpdateService.class);
        JobInfo.Builder builder = new JobInfo.Builder(Util.UPDATE_JOB_ID, serviceComponent);

        // set requirements
        builder.setMinimumLatency(minHourLatency*60*60 * 1000); // wait at least
        //builder.setOverrideDeadline(30 * 1000); // maximum delay
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // require any network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        //builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.schedule(builder.build());
    }


    /**
     * Download JSON with data
     * @param context
     */
    public static void downloadData(final Context context) {
        RequestQueue queue = Volley.newRequestQueue(context);

        // inform about downloading
        Toast.makeText(context, "VolajiciInfo: aktualizuji data",Toast.LENGTH_SHORT).show();

        // Get url from shared preferences
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final String url = sharedPreferences.getString(
                context.getResources().getString(
                        R.string.shared_pref_url),
                context.getResources().getString(
                        R.string.url_default)); // default url
        //Log.d("Download", "Downloading");

        // create new json request
        StringRequest jsonObjectRequest = new StringRequest
                (Request.Method.POST, url,
                        new Response.Listener<String>() {

        @Override
                    public void onResponse(String response) {
                        PeopleDB peopleDB = new PeopleDB(context);
                        Log.d("Download", response.toString());
                        try {
                            // Convert response string to JSON
                            JSONObject toJson = new JSONObject(response);

                            // Update db in the phone
                            peopleDB.updateDBValues(toJson.getJSONArray("data"));
                        } catch (JSONException e) {
                            Toast.makeText(context, context.getString(R.string.json_wrong_format),Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }

                        // save time of downloading
                        Calendar c = Calendar.getInstance();
                        SimpleDateFormat df = new SimpleDateFormat("dd. MM. yyyy HH:mm:ss");
                        String formattedDate = df.format(c.getTime());

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(context.getResources().getString(R.string.shared_pref_last_update), formattedDate);
                        editor.commit();

                        // reload main activity - display current values
                        Intent intent1 = new Intent(context.getResources().getString(R.string.receiverReload));
                        context.sendBroadcast(intent1);

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("DownloadDataError", error.getMessage());
                        // Log.d("downloading", "Status code: "+(error.networkResponse.statusCode ));
                        Toast.makeText(context, context.getString(R.string.update_problem),Toast.LENGTH_LONG).show();


                    }
                }) {
            // add headers
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Secret-Key", context.getString(R.string.data_secret_key));

                return params;
            }

            // add post data
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("key", context.getString(R.string.data_secret_key));

                return params;
            }
        };

        // add request to the queue
        queue.add(jsonObjectRequest);
    }


    /**
     * Add https protocol in the beggining of the string if there is no protocol
     * @param url
     * @return
     */
    public static String addHTTPsProtocol(String url) {
        if (!url.substring(0, 7).equals("http://") && !url.substring(0, 8).equals("https://")) {
            return "https://"+url;
        } else {
            return url;
        }
    }

    public static String changeProtocol(String url) {
        if (url.substring(0, 8).equals("https://")) {
            return url.replace("https://", "http://");
        } else {
            return url.replace("http://", "https://");
        }
    }


}