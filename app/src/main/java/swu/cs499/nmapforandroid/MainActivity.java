package swu.cs499.nmapforandroid;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TabHost;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{
    public static String nmapCmd;
    private static Context context;
    private static DownloadFilesTask downloadFilesTask;
    private static InstallFilesTask installFilesTask;
    private final int WRITE_REQUEST_CODE_DOWNLOAD = 1;
    private final int WRITE_REQUEST_CODE_DOWNLOAD_ONLY = 2;
    private final int WRITE_REQUEST_CODE_INSTALL = 3;
    private final String arch64UrlString =
            "https://github.com/kost/nmap-android/releases/download/v7.31/nmap-7.31-binaries" +
            "-arm64-v8a.zip";
    private final String armUrlString =
            "https://github.com/kost/nmap-android/releases/download/v7.31/nmap-7.31-binaries" +
            "-armeabi.zip";
    private final String mipsUrlString =
            "https://github.com/kost/nmap-android/releases/download/v7.31/nmap-7.31-binaries-mips" +
            ".zip";
    private final String mips64elUrlString =
            "https://github.com/kost/nmap-android/releases/download/v7.31/nmap-7.31-binaries" +
            "-mips64el.zip";
    private final String x86UrlString =
            "https://github.com/kost/nmap-android/releases/download/v7.31/nmap-7.31-binaries" +
            "-x86.zip";
    private final String x86_64UrlString =
            "https://github.com/kost/nmap-android/releases/download/v7.31/nmap-7.31-binaries" +
            "-x86_64.zip";
    private final String nmapDataUrlString =
            "https://github.com/kost/nmap-android/releases/download/v7.31/nmap-7.31-data.zip";
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     private ViewPager mViewPager;
     private URL arch64Url;
     private URL armUrl;
     private URL mipsUrl;
     private URL mips64elUrl;
     private URL x86Url;
     private URL x86_64Url;
     private URL nmapDataURL;
     private DownloadF
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private URL arch64Url;
    private URL armUrl;
    private URL mipsUrl;
    private URL mips64elUrl;
    private URL x86Url;
    private URL x86_64Url;
    private static URL nmapDataURL;
    private static HashMap<String, URL> urls;
    private TabLayout tabs;

    @Override
    public void onConfigurationChanged(Configuration config)
    {
        super.onConfigurationChanged(config);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Create the adapter that will return a fragment for each of the two
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        context = this;
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        tabs = findViewById(R.id.tabs);

        initObjects();

        // check if we need to download nmap
        if (needToDownload())
        {
            // if permission is requested is needed then download is handled by
            // onRequestsPermissionResult
            if (checkPermissions(WRITE_REQUEST_CODE_DOWNLOAD))
            {
                download();
            }
        }
    }

    public void initObjects()
    {
        try
        {
            arch64Url = new URL(arch64UrlString);
            armUrl = new URL(armUrlString);
            mipsUrl = new URL(mipsUrlString);
            mips64elUrl = new URL(mips64elUrlString);
            x86Url = new URL(x86UrlString);
            x86_64Url = new URL(x86_64UrlString);

            nmapDataURL = new URL(nmapDataUrlString);
            nmapCmd =
                    getFilesDir().getAbsolutePath() + File.separator + "nmap" + File.separator +
                    "nmap";
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        urls = new HashMap();
        urls.put("arch64", arch64Url);
        urls.put("arm", armUrl);
        urls.put("mips", mipsUrl);
        urls.put("mips64el", mips64elUrl);
        urls.put("x86", x86Url);
        urls.put("x86_64", x86_64Url);
    }

    public static boolean needToDownload()
    {
        // path = /data/user/0/com.example.swu.ndroidmap/files
        File nmap = new File(nmapCmd);
        if (!nmap.exists())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean checkPermissions(int requestCode)
    {
        switch (requestCode)
        {
            case WRITE_REQUEST_CODE_DOWNLOAD:
                if (ContextCompat
                            .checkSelfPermission(this,
                                                 Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                      WRITE_REQUEST_CODE_DOWNLOAD);
                    return false;
                }
                else
                {
                    return true;
                }
            case WRITE_REQUEST_CODE_DOWNLOAD_ONLY:
                if (ContextCompat
                            .checkSelfPermission(this,
                                                 Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(this, new String[]{
                                                              Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                      WRITE_REQUEST_CODE_DOWNLOAD);
                    return false;
                }
                else
                {
                    return true;
                }
            case WRITE_REQUEST_CODE_INSTALL:
                if (ContextCompat
                            .checkSelfPermission(this,
                                                 Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(this, new String[]{
                                                              Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                      WRITE_REQUEST_CODE_DOWNLOAD);
                    return false;
                }
                else
                {
                    return true;
                }
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults)
    {
        switch (requestCode)
        {
            case WRITE_REQUEST_CODE_DOWNLOAD:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    download();
                }
                else
                {
                    permissionNotGranted();
                }
                break;
            case WRITE_REQUEST_CODE_DOWNLOAD_ONLY:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    downloadNmap(true);
                }
                else
                {
                    permissionNotGranted();
                }
                break;
            case WRITE_REQUEST_CODE_INSTALL:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    installNmap();
                }
                else
                {
                    permissionNotGranted();
                }
                break;
        }
    }

    public void permissionNotGranted()
    {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(context, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        String string = "WRITE_EXTERNAL_STORAGE permissions are required to download and install nmap.\n\n" +
                        "If you don't want to grant permissions, you can manually download and install " +
                        "nmap. \nFor instructions on how to do so:\n   www.github.com/SamWu157/CS499-02";
        builder.setTitle("Missing Permissions");
        builder.setMessage(string);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void download()
    {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(context, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        String string = "Nmap is not installed. Please press OK to start download and install.";
        builder.setTitle("Missing Nmap");
        builder.setMessage(string);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                downloadNmap(false);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
//                downloadInstructions();
            }
        });
        // Create the AlertDialog object and return it
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void downloadNmap(boolean downloadOnly)
    {
        ProgressDialog progressDialog =
                new ProgressDialog(context, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setMessage("Downloading Nmap...");
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(true);

        downloadFilesTask = new DownloadFilesTask(context, progressDialog, downloadOnly);

        // get architecture and choose nmap binary to download based on phone's architecture
        // list of architecture values???
        String os = System.getProperty("os.arch");
        URL binaryUrl;
        if (os.contains("aarch64") || (os.contains("arm") && os.contains("64")))
        {
            binaryUrl = urls.get("arch64");
        }
        else if (os.contains("arm"))
        {
            binaryUrl = urls.get("arm");
        }
        else if (os.contains("mips64"))
        {   //"i686")) {
            binaryUrl = urls.get("mips64");
        }
        else if (os.contains("mips"))
        {
            binaryUrl = urls.get("mips");
        }
        else if (os.contains("x86_64"))
        {
            binaryUrl = urls.get("x86_64");
        }
        else if (os.contains("x86"))
        {
            binaryUrl = urls.get("x86_64");
        }
        else
        {
            return;
        }

        downloadFilesTask.execute(binaryUrl, nmapDataURL);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                downloadFilesTask.cancel(true);
            }
        });
    }

    public static void installNmap()
    {
        // instantiate it within the onCreate method
        ProgressDialog progressDialog =
                new ProgressDialog(context, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        progressDialog.setMessage("Installing Nmap...");
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(true);

        installFilesTask = new InstallFilesTask(context, progressDialog);
        String nmapExtractPath =
                Environment.getExternalStorageDirectory().toString() + File.separator +
                Environment.DIRECTORY_DOWNLOADS + File.separator;
        String nmapBinaryPath = nmapExtractPath + "nmap-binary.zip";
        String nmapDataPath = nmapExtractPath + "nmap.zip";

        File nmapBinary = new File(nmapBinaryPath);
        File nmapData = new File(nmapDataPath);

        installFilesTask.execute(nmapBinary, nmapData);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                installFilesTask.cancel(true);
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        String msg;

        switch (id)
        {
            case R.id.action_help:
                msg = " - if scan takes too long try stopping the scan and scanning a smaller address space\n\n" +
                      " - if output is cut off try, hiding/showing the appbar or toolbar\n\n" +
                      " - More info: www.github.com/SamWu157/CS499-02";
                dialog("Help", msg);
                break;
            case R.id.action_about:
                msg = "Nmap Version:\t\t7.31\n\n" + "Architecture:\t\t\t" + System.getProperty("os.arch") + "\n\n";
                dialog("About", msg);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void dialog(String title, String msg)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id)
        {
            case R.id.nav_download:
                if (checkPermissions(WRITE_REQUEST_CODE_DOWNLOAD_ONLY))
                {
                    downloadNmap(true);
                }
                break;

            case R.id.nav_install:
                if (checkPermissions(WRITE_REQUEST_CODE_INSTALL))
                {
                    installNmap();
                }
                break;
            case R.id.nav_download_install:
                if (checkPermissions(WRITE_REQUEST_CODE_DOWNLOAD))
                {
                    downloadNmap(false);
                }
                break;
            case R.id.nav_appbar_hide:
                getSupportActionBar().hide();
                break;
            case R.id.nav_appbar_show:
                getSupportActionBar().show();
                break;
            case R.id.nav_toolbar_hide:
                tabs.setVisibility(View.GONE);
                break;
            case R.id.nav_toolbar_show:
                tabs.setVisibility(View.VISIBLE);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter
    {

        public SectionsPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int position)
        {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position)
            {
                case 0:
                    return ScanFragment.newInstance(position + 1);
                case 1:
                    return FoundFragment.newInstance(position + 1);
            }
            return null;
        }

        @Override
        public int getCount()
        {
            // Show total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            switch (position)
            {
                case 0:
                    return "Scan";
                case 1:
                    return "Found";
            }
            return null;
        }
    }
}

