package swu.cs499.nmapforandroid;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by swu on 5/1/17.
 */

public class UnzipTask extends AsyncTask<String, Integer, String> {

    private Context context;
    private PowerManager.WakeLock mWakeLock;
    ProgressDialog mProgressDialog;
    private boolean binary;

    public UnzipTask(Context context, ProgressDialog progressDialog, boolean binary) {
        this.context = context;
        this.mProgressDialog = progressDialog;
        this.binary = binary;
    }

    @Override
    protected String doInBackground(String ...files) {
        InputStream input = null;
        OutputStream output = null;
        try {
            if (binary) {
                File bin = new File("/sdcard/opt/nmap-7.31/bin");
                if (!bin.exists()) {
                    bin.mkdirs();
                }

                // extract
                publishProgress(0);
                File unzip = new File(files[0]);
                publishProgress(33);
                unzip(unzip, bin);
                publishProgress(66);
                unzip.delete();
                publishProgress(100);
            } else {
                String nmapString = "/sdcard/opt/";
                File nmap = new File(nmapString);
                if (!nmap.exists()) {
                    nmap.mkdirs();
                }

                // extract nmap
                publishProgress(0);
                File unzip = new File(files[0]);
                publishProgress(33);
                unzip(unzip, nmap);
                publishProgress(66);
                unzip.delete();
                publishProgress(100);
            }
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

    protected void unzip(File zipFile, File targetDirectory) throws IOException {
        ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream (new FileInputStream(zipFile)));
        try {
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs())
                    throw new FileNotFoundException("Failed to ensure directory: " +
                            dir.getAbsolutePath());
                if (ze.isDirectory())
                    continue;
                FileOutputStream fout = new FileOutputStream(file);
                try {
                    while ((count = zis.read(buffer)) != -1)
                        fout.write(buffer, 0, count);
                } finally {
                    fout.close();
                }
            /* if time should be restored as well
            long time = ze.getTime();
            if (time > 0)
                file.setLastModified(time);
            */
            }
        } finally {
            zis.close();
        }
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
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        mProgressDialog.show();
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
    }
}
