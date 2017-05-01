package swu.cs499.nmapforandroid;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private static String NMAP_CMD = "";
    private static String CHECK_PATH = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the two
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // check download
        String path = getFilesDir().getAbsolutePath() + File.separator + "nmap" + File.separator + "nmap";
        File nmap = new File(path);
        if (!(nmap.exists())) {
            // get permission
            final int REQUEST_CODE = 157;
            if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE);
            }
            needToDownload(this);
        }

        CHECK_PATH = getFilesDir().getAbsolutePath() + File.separator + "nmap" + File.separator;

        NMAP_CMD = path;
    }

    public void needToDownload(Context context) {
        String string = "Nmap binary is not installed. Please go to settings and perform the following steps:" +
                "\n\t1. Download binary\n\t2. Unzip binary\n\t3. Move binary";
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        //set title
        builder.setTitle("Nmap Not Installed");
        builder
                .setMessage(string)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        settingsDialog();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        // Create the AlertDialog object and return it
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void aboutDialog(Context context) {
        String string = "Nmap Version:\t\t7.31\n\n" + "Architecture:\t\t\t" + System.getProperty("os.arch") + "\n\n";
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        //set title
        builder.setTitle("About");
        builder
                .setMessage(string)
                .setNegativeButton("OK" +
                        "", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        // Create the AlertDialog object and return it
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void settingsDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.activity_download, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        builder.setTitle("Settings\n");

        Button downloadButton = (Button) promptView.findViewById(R.id.download_button);
        Button extractButton = (Button) promptView.findViewById(R.id.extract_button);
        //Button untarButton = (Button) promptView.findViewById(R.id.untar_button);
        Button moveButton = (Button) promptView.findViewById(R.id.move_button);

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadNmap();
                downloadBinary();
                //download();
            }
        });

        extractButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //extract();
                unzipBinary();
                unzipNmap();
            }
        });
/*
        untarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                untar();
            }
        });
*/
        moveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                move();
            }
        });

        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        builder.setView(promptView);
        builder.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.about_settings) {
            aboutDialog(this);
            return true;
        } else if (id == R.id.download_settings) {

            // get permission
            final int REQUEST_CODE = 157;
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE);
            }

            settingsDialog();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class ScanFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public ScanFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static ScanFragment newInstance(int sectionNumber) {
            ScanFragment fragment = new ScanFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.activity_scan, container, false);

            // dropdown menu
            final Spinner scanType = (Spinner) rootView.findViewById(R.id.scan_type);
            String[] types = {"host", "port"};
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_dropdown_item, types);
            scanType.setAdapter(adapter);

            // scan button
            final Button scanButton = (Button) rootView.findViewById(R.id.scan_button);
            scanButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                String type = "";
                switch(scanType.getSelectedItemPosition()) {
                    case 0:
                        type = "host";
                        break;
                    case 1:
                        type = "port";
                        break;
                }

                File nmapExe = new File(NMAP_CMD);
                // make executable
                if (!nmapExe.canExecute()) {
                    nmapExe.setExecutable(true);
                }


                // get ip address
                EditText ipAdressInput = (EditText) rootView.findViewById(R.id.ip_address_input);
                String ipAddress = ipAdressInput.getText().toString();
                if (ipAddress.equals("")) {
                    ipAddress = "127.0.0.1";
                }

                String[] cmd;
                StringBuilder output = new StringBuilder();
                ProcessBuilder processBuilder = null;
                if (type.equals("host")) {
                    cmd = new String[] {NMAP_CMD, "-sn", ipAddress};

                } else {
                    cmd = new String[]{NMAP_CMD, "-sV", ipAddress};
                }

                // run nmap in background

                try {
                    processBuilder = new ProcessBuilder(cmd);
                    Process process = processBuilder.start();
                    processBuilder.redirectErrorStream(true);
                    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    String line = "";
                    TextView scanOutput = (TextView) rootView.findViewById(R.id.scan_output);
                    setText(scanOutput, "");
                    Log.i("scan", "start");
                    while ((line = br.readLine()) != null) {
                        Log.i("scan", line);
                        line = scanOutput.getText().toString() + "\n" + line;
                        setText(scanOutput, line);
                    }
                } catch (IOException e) {

                }

                }
            });

            // clear button
            Button clearButton = (Button) rootView.findViewById(R.id.clear_scan_button);
            clearButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView scanOutput = (TextView) rootView.findViewById(R.id.scan_output);
                    setText(scanOutput, "");
                }
            });

            return rootView;
        }
    }

    public static void setText(TextView tv, String output) {
        tv.setText(output);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DeviceFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public DeviceFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static DeviceFragment newInstance(int sectionNumber) {
            DeviceFragment fragment = new DeviceFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.activity_device, container, false);
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return ScanFragment.newInstance(position + 1);
                case 1:
                    return DeviceFragment.newInstance(position + 1);
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Scan";
                case 1:
                    return "Topology";
            }
            return null;
        }
    }

    public void downloadBinary() {
        // declare the dialog as a member field of your activity
        ProgressDialog mProgressDialog;

        // instantiate it within the onCreate method
        mProgressDialog = new ProgressDialog(MainActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        mProgressDialog.setMessage("Downloading...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);

        // execute this when the downloader must be fired
        final DownloadTask binaries = new DownloadTask(MainActivity.this, mProgressDialog, true);
        HashMap<String, String> urls = new HashMap<String, String>();
        urls.put("arch64", "https://github.com/kost/nmap-android/releases/download/v7.31/nmap-7.31-binaries-arm64-v8a.zip");
        urls.put("arm", "https://github.com/kost/nmap-android/releases/download/v7.31/nmap-7.31-binaries-armeabi.zip");
        urls.put("i686", "https://github.com/kost/nmap-android/releases/download/v7.31/nmap-7.31-binaries-x86.zip");
        urls.put("mips64", "https://github.com/kost/nmap-android/releases/download/v7.31/nmap-7.31-binaries-mips64el.zip");
        urls.put("mipsel", "https://github.com/kost/nmap-android/releases/download/v7.31/nmap-7.31-binaries-mips.zip");
        urls.put("x86_64", "https://github.com/kost/nmap-android/releases/download/v7.31/nmap-7.31-binaries-x86_64.zip");

        // get os
        String os = System.getProperty("os.arch");
        String url = "";
        if (os.contains("arch64")) {
            url = urls.get("arch64");
        } else if (os.contains("arm")) {
            url = urls.get("arm");
        } else if (os.contains("i686")) {
            url = urls.get("i686");
        } else if (os.contains("mips64el")) {
            url = urls.get("mips64el");
        } else if (os.contains("mipsel")) {
            url = urls.get("mipsel");
        } else if (os.contains("x86_64")) {
            url = urls.get("x86_64");
        } else {
            return;
        }
        binaries.execute(url);
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                binaries.cancel(true);
            }
        });
    }

    public void downloadNmap() {
        // declare the dialog as a member field of your activity
        ProgressDialog mProgressDialog;

        // instantiate it within the onCreate method
        mProgressDialog = new ProgressDialog(MainActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        mProgressDialog.setMessage("Downloading...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);

        final DownloadTask data = new DownloadTask(MainActivity.this, mProgressDialog, false);
        data.execute("https://github.com/kost/nmap-android/releases/download/v7.31/nmap-7.31-data.zip");
    }

    /*
    public void download() {
        // declare the dialog as a member field of your activity
        ProgressDialog mProgressDialog;

        // instantiate it within the onCreate method
        mProgressDialog = new ProgressDialog(MainActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        mProgressDialog.setMessage("Downloading...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);

        // execute this when the downloader must be fired
        final DownloadTask binaries = new DownloadTask(MainActivity.this, mProgressDialog, true);
        HashMap<String, String> urls = new HashMap<String, String>();
        urls.put("arch64", "https://github.com/kost/nmap-android/releases/download/v7.31/nmap-7.31-android-aarch64-bin.tar.bz2");
        urls.put("arm", "https://github.com/kost/nmap-android/releases/download/v7.31/nmap-7.31-android-arm-bin.tar.bz2");
        urls.put("i686", "https://github.com/kost/nmap-android/releases/download/v7.31/nmap-7.31-android-i686-bin.tar.bz2");
        urls.put("mips64", "https://github.com/kost/nmap-android/releases/download/v7.31/nmap-7.31-android-mips64el-bin.tar.bz2");
        urls.put("mipsel", "https://github.com/kost/nmap-android/releases/download/v7.31/nmap-7.31-android-mipsel-bin.tar.bz2");
        urls.put("x86_64", "https://github.com/kost/nmap-android/releases/download/v7.31/nmap-7.31-android-x86_64-bin.tar.bz2");

        // get os
        String os = System.getProperty("os.arch");
        String url = "";
        if (os.contains("arch64")) {
            url = urls.get("arch64");
        } else if (os.contains("arm")) {
            url = urls.get("arm");
        } else if (os.contains("i686")) {
            url = urls.get("i686");
        } else if (os.contains("mips64el")) {
            url = urls.get("mips64el");
        } else if (os.contains("mipsel")) {
            url = urls.get("mipsel");
        } else if (os.contains("x86_64")) {
            url = urls.get("x86_64");
        } else {
            return;
        }
        binaries.execute(url);
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
        @Override
            public void onCancel(DialogInterface dialog) {
                binaries.cancel(true);
            }
        });

    }

        public void extract() {
            // declare the dialog as a member field of your activity
            ProgressDialog mProgressDialog;

            // instantiate it within the onCreate method
            mProgressDialog = new ProgressDialog(MainActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
            mProgressDialog.setMessage("Extracting...");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(true);
            final ExtractTask extractTask = new ExtractTask(MainActivity.this, mProgressDialog);
            String path = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DOWNLOADS + "/nmap.tar.bz2";
            extractTask.execute(path);
        }
        public void untar() {
            // declare the dialog as a member field of your activity
            ProgressDialog mProgressDialog;

            // instantiate it within the onCreate method
            mProgressDialog = new ProgressDialog(MainActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
            mProgressDialog.setMessage("Untarring...");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(true);
            final UntarTask untarTask = new UntarTask(MainActivity.this, mProgressDialog);
            String path = Environment.getExternalStorageDirectory().toString() + File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator + "nmap.tar";
            untarTask.execute(path);
        }
    */
    public void move() {
        // declare the dialog as a member field of your activity
        ProgressDialog mProgressDialog;

        // instantiate it within the onCreate method
        mProgressDialog = new ProgressDialog(MainActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        mProgressDialog.setMessage("Moving...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);
        final MoveTask moveTask = new MoveTask(MainActivity.this, mProgressDialog);
        String path = getFilesDir().getAbsolutePath() + File.separator + "nmap" + File.separator + "nmap";
        moveTask.execute(path);
    }

    public void unzipNmap() {
        // declare the dialog as a member field of your activity
        ProgressDialog mProgressDialog;

        // instantiate it within the onCreate method
        mProgressDialog = new ProgressDialog(MainActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        mProgressDialog.setMessage("Extracting...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);
        final UnzipTask unzipNmap = new UnzipTask(MainActivity.this, mProgressDialog, false);
        String nmap = Environment.getExternalStorageDirectory().toString() + File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator + "nmap.zip";
        unzipNmap.execute(nmap);
    }

    public void unzipBinary() {
        // declare the dialog as a member field of your activity
        ProgressDialog mProgressDialog;

        // instantiate it within the onCreate method
        mProgressDialog = new ProgressDialog(MainActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        mProgressDialog.setMessage("Extracting...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);
        String bin = Environment.getExternalStorageDirectory().toString() + File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator + "nmap-binary.zip";
        final UnzipTask unzipBin = new UnzipTask(MainActivity.this, mProgressDialog, true);
        unzipBin.execute(bin);
    }

}

