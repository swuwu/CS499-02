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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by swu on 2/25/18.
 */

public class InstallFilesTask extends AsyncTask<File, Integer, String>
{

    ProgressDialog mProgressDialog;
    private Context context;
    private PowerManager.WakeLock mWakeLock;

    public InstallFilesTask(Context context, ProgressDialog progressDialog)
    {
        this.context = context;
        this.mProgressDialog = progressDialog;
    }

    @Override
    protected String doInBackground(File... zipFiles)
    {
        ZipInputStream zis;
        for (int i = 0; i < zipFiles.length; i++)
        {
            String outFilePath = "";
            switch (i)
            {
                // binary
                case 0:
                    String nmapBinaryDirPath = "/sdcard/opt/nmap-7.31/bin";
                    File nmapBinaryDir = new File(nmapBinaryDirPath);
                    if (!nmapBinaryDir.exists())
                    {
                        nmapBinaryDir.mkdirs();
                    }
                    outFilePath = nmapBinaryDirPath;
                    break;
                // data
                case 1:
                    String nmapDataDirPath = "/sdcard/opt/";
                    File nmapDataDir = new File(nmapDataDirPath);
                    if (!nmapDataDir.exists())
                    {
                        nmapDataDir.mkdirs();
                    }
                    outFilePath = nmapDataDirPath;
                    break;
            }

            long fileLength = zipFiles[i].length();

            try
            {
                zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFiles[i])));
                ZipEntry ze;
                long total = 0;
                int count;

                byte[] buffer = new byte[1024];
                while ((ze = zis.getNextEntry()) != null)
                {
                    File file = new File(outFilePath, ze.getName());
                    File dir = ze.isDirectory() ? file : file.getParentFile();
                    if (!dir.isDirectory() && !dir.mkdirs())
                        throw new FileNotFoundException("Failed to ensure directory: " +
                                                        dir.getAbsolutePath());
                    if (ze.isDirectory())
                        continue;

                    FileOutputStream fout = new FileOutputStream(file);

                    while ((count = zis.read(buffer)) != -1)
                    {
                        fout.write(buffer, 0, count);
                        total += 1024;
                        // publishing the progress....
                        if (fileLength > 0)
                            publishProgress((int) (total * 100 / fileLength));
                    }

                    fout.close();
                    zis.closeEntry();
                }

                zis.close();
            }
            catch (Exception e)
            {
                return e.toString();
            }

            // Escape early if cancel() is called
            if (isCancelled()) break;
        }

        // move binary to local storage
        String nmapExeDirPath = context.getFilesDir().getAbsolutePath() + File.separator + "nmap";
        File nmapExeDir = new File(nmapExeDirPath);
        if (!nmapExeDir.exists())
        {
            nmapExeDir.mkdirs();
        }

        String nmapExePath = context.getFilesDir().getAbsolutePath() + File.separator + "nmap" +
                             File.separator + "nmap";
        String nmapBin = "/sdcard/opt/nmap-7.31/bin/nmap";

        String[] cmd = {"sh", "-c", "cat " + nmapBin + " > " + nmapExePath};

        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        processBuilder.redirectErrorStream(true);
        try
        {
            processBuilder.start();
        }
        catch (Exception e)
        {
            return e.toString();
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
    }

    @Override
    protected void onProgressUpdate(Integer... progress)
    {
        mProgressDialog.show();
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
            Toast.makeText(context, "Extract error: " + result, Toast.LENGTH_LONG).show();
    }
}
