package swu.cs499.ndroidmap;

/**
 * Created by swu on 2/25/18.
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by swu on 4/15/17.
 */

// usually, subclasses of AsyncTask are declared inside the activity class.
// that way, you can easily modify the UI thread from here
public class DownloadFilesTask extends AsyncTask<URL, Integer, String>
{

    ProgressDialog mProgressDialog;
    private Context context;
    private PowerManager.WakeLock mWakeLock;
    private boolean downloadOnly = false;

    public DownloadFilesTask(Context context, ProgressDialog progressDialog, boolean downloadOnly)
    {
        this.context = context;
        this.mProgressDialog = progressDialog;
        this.downloadOnly = downloadOnly;
    }

    protected String doInBackground(URL... urls)
    {
        int totalUrlsSize = urls.length;

        for (int i = 0; i < totalUrlsSize; i++)
        {
            InputStream input = null;
            OutputStream output = null;
            HttpsURLConnection connection = null;
            URL url = urls[i];
            try
            {
                connection = (HttpsURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK)
                {
                    return "Server returned HTTP " + connection.getResponseCode()
                           + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                String filepath = "";

                switch (i)
                {
                    // nmap binary
                    case 0:
                        filepath = Environment.getExternalStorageDirectory() + File.separator +
                                   Environment.DIRECTORY_DOWNLOADS + File.separator +
                                   "nmap-binary.zip";
                        break;
                    // nmap file
                    case 1:
                        filepath = Environment.getExternalStorageDirectory() + File.separator +
                                   Environment.DIRECTORY_DOWNLOADS + File.separator + "nmap.zip";
                        break;

                }

                output = new FileOutputStream(filepath);

                byte data[] = new byte[4096];
                int count;
                long total = 0;
                while ((count = input.read(data)) != -1)
                {
                    // allow canceling with back button
                    if (isCancelled())
                    {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            }
            catch (Exception e)
            {
                return e.toString();
            }
            finally
            {
                try
                {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                }
                catch (IOException ignored)
                {
                }

                if (connection != null)
                    connection.disconnect();
            }

            // Escape early if cancel() is called
            if (isCancelled()) break;

        }

        return null;
    }

    @Override
    protected void onPreExecute()
    {
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
    protected void onProgressUpdate(Integer... progress)
    {
        super.onProgressUpdate(progress);
        // if we get here, length is known, now set indeterminate to false
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(100);
        mProgressDialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(String result)
    {
        mWakeLock.release();
        mProgressDialog.dismiss();
        if (result != null)
            Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
        if (!downloadOnly)
        {
            MainActivity.installNmap();
        }
    }
}
