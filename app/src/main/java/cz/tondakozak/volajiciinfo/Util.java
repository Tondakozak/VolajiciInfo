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
import android.util.Log;
import android.widget.Toast;
import com.android.volley.*;
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
    public static boolean isJobScheduled(Context context, int jobId) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService( Context.JOB_SCHEDULER_SERVICE );
        return scheduler.getPendingJob(jobId) != null;
    }


    /**
     * Schedule new job for updating service
     * @param context
     */
    public static void scheduleJob(Context context) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // shedule update job only if it is allowed in setting
        if (sharedPreferences.getBoolean(context.getResources().getString(R.string.shared_pref_on_background), true)) {
            // get min latency from shared preferences (20 is default value)
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

            Log.d("Scheduling job", "the service will run in: "+minHourLatency);
        } else {
            Log.d("Scheduling job", "Background task is not allowed");
        }


    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void restartSchedulingJob(final Context context) {


        // if the job is already scheduled, cancel it
        cancelScheduledJob(context, UPDATE_JOB_ID);

        // wait 5 seconds and then schedule it again in new threat (could be problem with immediate start)
        Thread thread = new Thread(){
            public void run(){
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                scheduleJob(context);
            }
        };

        thread.start();
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void cancelScheduledJob(Context context, int jobId) {
        // if the job is already scheduled, cancel it
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        if (isJobScheduled(context, jobId)) {
            jobScheduler.cancel(jobId);
        }
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

                                // after successfull download, reschedule the job
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    Util.restartSchedulingJob(context);
                                }

                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        /**
                         * If Network error and in url is https, change protocol to http and try again
                         */
                        if (url.length() > 5 && url.substring(0, 5).equals("https") && error instanceof NoConnectionError) {
                            Toast.makeText(context, context.getString(R.string.change_protocol),Toast.LENGTH_SHORT).show();
                            Util.changeProtocol(url, context);
                            Util.downloadData(context);
                        }


                        // if there is network response
                        if (error.networkResponse != null) {
                            // page not found
                            if (error.networkResponse.statusCode == 404) {
                                Toast.makeText(context, context.getString(R.string.page_not_found),Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(context, context.getString(R.string.update_problem),Toast.LENGTH_LONG).show();
                        }

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
     * Upload data
     * @param context
     */
    public static void uploadData(final Context context, final String data) {
        RequestQueue queue = Volley.newRequestQueue(context);

        // inform about downloading
        //Toast.makeText(context, "VolajiciInfo: aktualizuji data",Toast.LENGTH_SHORT).show();

        final String url = "https://www.tondakozak.cz/testy/callerinfo/simData.php";
        //nal String url = "https://www.tondakozak.cz/testy/callerinfo/s";

        Log.d("uploadData", "nahrávám");
        // create new json request
        StringRequest jsonObjectRequest = new StringRequest
                (Request.Method.POST, url,
                        new Response.Listener<String>() {

                            @Override
                            public void onResponse(String response) {
                                Log.d("uploadData", response);
                            }
                        },
                        new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                        Log.e("uploadData", error.getMessage());

                            }
                        }
                 ) {
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
                params.put("simInfo", data);

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

    /**
     * Changes https to http and save it
     * @param url
     * @param context
     */
    public static void changeProtocol(String url, Context context) {
        String newUrl;
        if (url.substring(0, 8).equals("https://")) {
            newUrl = url.replace("https://", "http://");
        } else {
            newUrl = url.replace("http://", "https://");
        }
        saveURL(newUrl, context);
    }

    /**
     * Save update url to shared preferences
     * @param url
     * @param context
     */
    public static void saveURL(String url, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getString(R.string.shared_pref_url), url);
        editor.commit();
    }


}