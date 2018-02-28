package swu.cs499.nmapforandroid;

/**
 * Created by swu on 2/25/18.
 */

import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class FoundFragment extends Fragment
{
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static View rootView;

    private Button btnClear;
    private static LinearLayout llDisplay;

    public FoundFragment()
    {
    }

    @Override
    public void onConfigurationChanged(Configuration config)
    {
        super.onConfigurationChanged(config);
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static FoundFragment newInstance(int sectionNumber)
    {
        FoundFragment fragment = new FoundFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public static void updateDevice()
    {
        llDisplay.removeAllViews();

        ArrayList<Host> hosts = ScanFragment.hostsFound;
        for (Host h : hosts)
        {
            TextView ip = new TextView(rootView.getContext());
            ip.setText(" " + h.getIp());
            ip.setTypeface(Typeface.MONOSPACE);
            ip.setFreezesText(true);
            llDisplay.addView(ip);

            if (h.getPortList().size() != 0)
            {
                TextView port = new TextView(rootView.getContext());
                String portStr = String.format(" %-5s%-10s%-10s%-10s",
                                               "PORT",
                                               "TYPE",
                                               "STATE",
                                               "SERVICE");
                port.setText(portStr);
                port.setTypeface(Typeface.MONOSPACE);
                port.setFreezesText(true);
                llDisplay.addView(port);

                for (Port p : h.getPortList())
                {
                    port = new TextView(rootView.getContext());
                    portStr = String.format(" %-5d%-10s%-10s%-10s",
                                                   p.getNum(),
                                                   p.getType(),
                                                   p.getState(),
                                                   p.getService());
                    port.setText(portStr);
                    port.setTypeface(Typeface.MONOSPACE);
                    port.setFreezesText(true);
                    llDisplay.addView(port);
                }
            }
            else
            {
                TextView noPorts = new TextView(rootView.getContext());
                noPorts.setText(" Ports not found/scanned");
                noPorts.setTypeface(Typeface.MONOSPACE);
                noPorts.setFreezesText(true);
                llDisplay.addView(noPorts);
            }

            TextView empty = new TextView(rootView.getContext());
            empty.setText("\n");
            empty.setFreezesText(true);
            llDisplay.addView(empty);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.found, container, false);

        initObjects();

        btnClear.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ScanFragment.hostsFound = new ArrayList<>();
                llDisplay.removeAllViews();
            }
        });

        return rootView;
    }

    public void initObjects()
    {
        btnClear = rootView.findViewById(R.id.found_btn_clear);
        llDisplay = rootView.findViewById(R.id.found_ll_display);
    }

}

