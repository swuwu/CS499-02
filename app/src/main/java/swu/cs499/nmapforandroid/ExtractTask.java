package swu.cs499.nmapforandroid;

/**
 * Created by swu on 4/15/17.
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Created by swu on 4/15/17.
 */

// usually, subclasses of AsyncTask are declared inside the activity class.
// that way, you can easily modify the UI thread from here
public class ExtractTask extends AsyncTask<String, Integer, String> {

    private Context context;
    private PowerManager.WakeLock mWakeLock;
    ProgressDialog mProgressDialog;

    public ExtractTask (Context context, ProgressDialog progressDialog) {
        this.context = context;
        this.mProgressDialog = progressDialog;
    }

    @Override
    protected String doInBackground(String... files) {
        InputStream input = null;
        OutputStream output = null;
        try {
            File bz2file = new File(files[0]);
            // extract nmap
            String[] cmd = {"bzip2", "-d", bz2file.toString()};
            publishProgress(0);
            ProcessBuilder processBuilder = new ProcessBuilder(cmd);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            input = process.getInputStream();
            publishProgress(100);
        } catch (Exception e) {
            return e.toString();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // take CPU lock to prevent CPU from going off if the user
        // presses the power button during download
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getClass().getName());
        mWakeLock.acquire();
        mProgressDialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        // if we get here, length is known, now set indeterminate to false
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(100);
        mProgressDialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        mWakeLock.release();
        mProgressDialog.dismiss();
        if (result != null)
            Toast.makeText(context,"Extract error: "+result, Toast.LENGTH_LONG).show();
        else
            Toast.makeText(context,"Extract finished", Toast.LENGTH_SHORT).show();
    }
}

