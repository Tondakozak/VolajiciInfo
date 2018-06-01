package cz.tondakozak.volajiciinfo;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;
import android.widget.Toast;

/**
 * Service for updating data
 * Download the json on the background and reschedule job
 */
public class UpdateService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        //Log.d("UpdateService", "Start Updating service");

        // Download data
        Util.downloadData(getApplicationContext());

        // reschedule the job
        Util.scheduleJob(getApplicationContext());
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
