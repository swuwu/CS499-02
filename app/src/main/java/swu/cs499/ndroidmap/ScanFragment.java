package swu.cs499.ndroidmap;

/**
 * Created by swu on 2/25/18.
 */

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class ScanFragment extends Fragment implements View.OnClickListener
{
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    public static ArrayList<Host> hostsFound = new ArrayList<>();
    private View rootView;
    private TextView tvOutput;
    private Button btnRun;
    private Button btnClear;
    private Button btnStop;
    private StringBuilder output = new StringBuilder();
    private static Thread cmdThread = null;
    private static boolean interrupted = false;

    public ScanFragment()
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
    public static ScanFragment newInstance(int sectionNumber)
    {
        ScanFragment fragment = new ScanFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.scan, container, false);

        initObject();

        setListeners();

        btnStop.setEnabled(false);

        return rootView;
    }

    @Override
    public void onClick(View v)
    {
        if (MainActivity.needToDownload())
        {
            MainActivity.download();
            return;
        }
        final Button btn = (Button) v;
        final int id = v.getId();
        btn.setEnabled(false);

        switch (id)
        {
            case R.id.scan_btn_run:
                tvOutput.setText(" Running Nmap scan...\n\n" +
                                 " Note: large networks may take a while\n\n");
                output.setLength(0);
                btnStop.setEnabled(true);
                cmdThread = new Thread() {
                    @Override
                    public void run()
                    {
                        try
                        {
                            File nmapExe = new File(MainActivity.nmapCmd);
                            if (!nmapExe.canExecute())
                            {
                                nmapExe.setExecutable(true);
                            }

                            // get ip address
                            EditText etCmd = (EditText) rootView.findViewById(R.id.scan_et_cmd);
                            String cmd = etCmd.getText().toString();
                            String cmdList[];
                            String toRun[];
                            if (cmd.isEmpty())
                            {
                                cmd = "127.0.0.1";
                                toRun = new String[]{MainActivity.nmapCmd, cmd};
                            }
                            else
                            {
                                cmdList = cmd.split("\\s+");
                                toRun = new String[cmdList.length + 1];
                                toRun[0] = MainActivity.nmapCmd;
                                for (int i = 1; i < toRun.length; i++)
                                {
                                    toRun[i] = cmdList[i - 1];
                                }
                            }

                            ProcessBuilder processBuilder = new ProcessBuilder(toRun);
                            processBuilder.redirectErrorStream(true);

                            String ip = "";
                            ArrayList<Port> portList = new ArrayList<>();

                            boolean foundHost = false;

                            Process process = processBuilder.start();
                            BufferedReader br = new BufferedReader(
                                    new InputStreamReader(process.getInputStream()));
                            String line;
                            while ((line = br.readLine()) != null && !isInterrupted())
                            {
                                output.append(" " + line + "\n");

                                // parse output
                                if (line.contains("Nmap scan report for"))
                                {
                                    if (foundHost)
                                    {
                                        Host host = new Host(ip);
                                        hostsFound.add(host);

                                        ip = "";
                                        portList = new ArrayList<>();
                                        foundHost = false;
                                    }

                                    String toParse[] = line.split("\\s+");
                                    ip = toParse[toParse.length - 1];
                                    foundHost = true;
                                }
                                else if (line.contains("PORT"))
                                {
                                    while (!(line = br.readLine()).equals(""))
                                    {
                                        output.append(" " + line + "\n");
                                        String toParse[] = line.split("\\s+");

                                        if (toParse.length == 3)
                                        {
                                            String port = toParse[0];
                                            String state = toParse[1];
                                            String service = toParse[2];

                                            int portNum;
                                            String portType;
                                            String portToParse[] = port.split("/");

                                            if (portToParse.length == 2)
                                            {

                                                try
                                                {
                                                    portNum = Integer.parseInt(portToParse[0]);
                                                }
                                                catch (NumberFormatException e)
                                                {
                                                    e.printStackTrace();
                                                    ip = "";
                                                    portList = new ArrayList<>();
                                                    break;
                                                }
                                                portType = portToParse[1];
                                            }
                                            else
                                            {
                                                ip = "";
                                                portList = new ArrayList<>();
                                                break;
                                            }

                                            Port portToAdd =
                                                    new Port(portNum, portType, state, service);
                                            portList.add(portToAdd);
                                        }
                                        else
                                        {
                                            ip = "";
                                            portList = new ArrayList<>();
                                            break;
                                        }
                                    }

                                    Host host = new Host(ip, portList);
                                    hostsFound.add(host);
                                    foundHost = false;

                                    ip = "";
                                    portList = new ArrayList<>();

                                    output.append(line + "\n");
                                }
                            }

                            if (foundHost)
                            {
                                Host host = new Host(ip);
                                hostsFound.add(host);
                            }

                            if (isInterrupted())
                            {
                                interrupted = true;
                            }

                            getActivity().runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    if (interrupted)
                                    {
                                        tvOutput.setText(" " + output.toString() + "\n\n" + " Nmap Stopped...\n\n");
                                    }
                                    else
                                    {
                                        tvOutput.setText(output.toString());
                                    }
                                    btn.setEnabled(true);
                                    FoundFragment.updateDevice();
                                }
                            });
                        }
                        catch (Exception e)
                        {
                            Log.d("Nmap", e.toString());
                            tvOutput.setText(e.toString());
                            btn.setEnabled(true);
                            return;
                        }
                    }

                    @Override
                    public void interrupt()
                    {

                        super.interrupt();
                    }
                };
                cmdThread.start();
                break;
            case R.id.scan_btn_stop:
                if (cmdThread != null)
                {
                    cmdThread.interrupt();
                    cmdThread = null;
                }
                break;
            case R.id.scan_btn_clear:
                tvOutput.setText("");
                btn.setEnabled(true);
                break;
        }
    }

    public void initObject()
    {
        tvOutput = rootView.findViewById(R.id.scan_tv_output);
        btnRun = rootView.findViewById(R.id.scan_btn_run);
        btnClear = rootView.findViewById(R.id.scan_btn_clear);
        btnStop = rootView.findViewById(R.id.scan_btn_stop);
    }

    public void setListeners()
    {
        btnRun.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        btnStop.setOnClickListener(this);
    }
}