package live.lilo.DeviceActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.TwoLineListItem;

import com.datecs.pinpad.DeviceInfo;
import com.paypad.cardreader.controllers.PinpadManager;
import com.paypad.cardreader.extras.ReferenceList;
import com.paypad.cardreader.facade.PinpadFacade;
import com.paypad.cardreader.utils.Globals;
import com.paypad.cardreader.utils.MiscUtils;

import java.io.IOException;

public class DeviceActivity extends Activity{

    private class BluetootDevicePair {
        String name;
        String addr;
    }

    private class BluetoothDeviceAdapter extends ArrayAdapter<BluetootDevicePair> {
        public BluetoothDeviceAdapter(Context context){

            super(context, android.R.layout.simple_list_item_2);

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TwoLineListItem v = (TwoLineListItem)convertView;

            if(v == null) {
                LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = (TwoLineListItem)inflater.inflate(android.R.layout.simple_list_item_2, null);
            }

            BluetootDevicePair device = getItem(position);
            v.getText1().setText(device.name);
            // v.getText2().setText(device.addr);

            return v;
        }
    }

    private PinpadManager mPinpadManager;
    private BluetoothDeviceAdapter mListAdapter;
    private ListView mListView;
    private Handler deviceActivityHandler;
    private String btAddress = "";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //if(!Globals.autoConnect){ //dont show layout while auto connecting
        setContentView(com.example.testpaypadjar.R.layout.screen_device);
        // }
        setResult(RESULT_CANCELED);

        mPinpadManager = PinpadManager.getInstance(this);
        deviceActivityHandler = new Handler();
        mPinpadManager.setOnConnectionEstablishedListener(new PinpadManager.OnConnectionEstablishedListener() {
            @Override
            public void OnConnectionEstablished() {
                Globals.isPinpadConnected = true;
                try{
                    DeviceInfo devInfo = mPinpadManager.getPinpad().getIdentification();
                    Globals.pinpadSerialNumber = devInfo.getDeviceSerialNumber();
                }catch(IOException ioe){
                    Globals.pinpadSerialNumber = "unknown";
                }

                //store device MAC address
                MiscUtils.initContext(getApplicationContext());
                String storedPinpadMacAddress = MiscUtils.getFromSharedPreferences(ReferenceList.preference, ReferenceList.pinpadMACAddress, "");

                if (!storedPinpadMacAddress.equals(btAddress)) {// new pinpad or not the current pinpad app recognises
                    Globals.loadKeys = true;
                    MiscUtils.storeInSharedPreferences(ReferenceList.preference, ReferenceList.pinpadMACAddress, btAddress);
                }
                runOnUiThread(new Runnable() {
                    public void run() {
                        showToast("Pinpad connected");
                        setResult(RESULT_OK);
                        finish();
                    }
                });
            }
        });

        if (Globals.autoConnect) {
            MiscUtils.initContext(getApplicationContext());
            String pinpadMacAddress = MiscUtils.getFromSharedPreferences(ReferenceList.preference, ReferenceList.pinpadMACAddress, "");
            btAddress = pinpadMacAddress;
            connectToDevice(btAddress);
        }else{//show dialog only when not auto connecting
            showConnectPinpadDialog();
        }

    }

    /** Called when the activity is destroyed. */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void showConnectPinpadDialog(){
        mListAdapter = new BluetoothDeviceAdapter(this);
        mListView = (ListView)findViewById(com.example.testpaypadjar.R.id.listView);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parentView, View view, int position, long id) {
                btAddress = mListAdapter.getItem(position).addr;
                connectToDevice(btAddress);



            }
        });
        mListView.setAdapter(mListAdapter);

        findViewById(com.example.testpaypadjar.R.id.cancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        updateDeviceList();
    }
    // Show toast notification, running in UI thread.
    private void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Populate list with paired bluetooth devices.
    @SuppressLint("MissingPermission")
    private void updateDeviceList() {
        BluetoothAdapter bthAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bthAdapter != null) {
            for (BluetoothDevice device: bthAdapter.getBondedDevices()) {
                BluetootDevicePair pair = new BluetootDevicePair();
                pair.name = device.getName();
                pair.addr = device.getAddress();
                mListAdapter.add(pair);
            }
        }

        mListAdapter.notifyDataSetChanged();
    }

    // Connect to specific bluetooth device.
    private void connectToDevice(final String btAddress) {
        String dialogMessage;
        // Construct a progress dialog to prevent user from actions until connection is finished.
        final ProgressDialog dialog = new ProgressDialog(DeviceActivity.this);
        if (Globals.autoConnect) {
            dialogMessage="Auto connecting pinpad";
        }else{
            dialogMessage = "Please Wait";
        }
        dialog.setMessage(dialogMessage);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return true;
            }
        });
        dialog.show();

        // Force connection to be execute in separate thread.
        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mPinpadManager.connect(btAddress, getApplicationContext());
                } catch (IOException e) {
                    e.printStackTrace();
                    showToast("Fialed to connect to Pinpad");
                    if (Globals.autoConnect) { //show dialog after auto connecting failed
                        deviceActivityHandler.post(new Runnable(){
                            @Override
                            public void run(){
                                Globals.autoConnect = false;
                                showConnectPinpadDialog();
                            }
                        });

                    }
                } finally {
                    dialog.dismiss();
                }
            }
        });
        t.start();
    }


}
