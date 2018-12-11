package sample.lyon.things.pithingsbluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sample.lyon.things.pithingsbluetooth.Bluetooth.BluetoothTool;
import sample.lyon.things.pithingsbluetooth.Volume.VolumeChangeObserver;
import sample.lyon.things.pithingsbluetooth.Volume.VolumeDialog;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class MainActivity extends Activity implements VolumeChangeObserver.VolumeChangeListener {
    String TAG = MainActivity.class.getName();
    BluetoothTool bluetoothTool;
    TextView BluetoothDeviceName;
    TextView version;
    ListView searchbluetoothDeviceListView;
    ListView haveConnectBluetoothListView;
    List<String> searchBluetoothDevice;
    ArrayAdapter searchAdapterlist;
    ArrayAdapter connectAdapterlist;
    List<String> connectBluetoothDevice;
    Button reSearch;
    TextView opentime;
    final int OPENBLUETOOTH = 0;
    final int REQUEST_ENABLE_BT = 100;
    HashMap<String, String> bluetoothDeviceName2 = new HashMap<>();
    private VolumeChangeObserver mVolumeChangeObserver;
    VolumeDialog volumeDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        volumeDialog= new VolumeDialog(MainActivity.this,AudioManager.STREAM_SYSTEM);
        //实例化对象并设置监听器
        mVolumeChangeObserver = new VolumeChangeObserver(this){
            public void onVolumeChanged(int volume){
                Log.e(TAG, "调节后的音乐音量大小为 volume = " + volume);
                showVolumeDialog(volume);
            }
        };
        mVolumeChangeObserver.setVolumeChangeListener(this);
        int initVolume = mVolumeChangeObserver.getCurrentMusicVolume();
        Log.e(TAG, "调节后的音乐音量大小为 initVolume = " + initVolume);

        setContentView(R.layout.activity_main);
        searchBluetoothDevice = new ArrayList<>();
        connectBluetoothDevice = new ArrayList<>();
        searchAdapterlist = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                searchBluetoothDevice);
        connectAdapterlist = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                connectBluetoothDevice);
        opentime = (TextView) findViewById(R.id.openTime);
        bluetoothTool = new BluetoothTool(this) {
            @Override
            public void getBluetoothDeviceName(HashMap<String, String> bluetoothDeviceName) {
                searchBluetoothDevice.clear();
                int i = 0;
                for (Map.Entry<String, String> entry : bluetoothDeviceName.entrySet()) {
                    searchBluetoothDevice.add("[" + i + "]:" + entry.getValue() + ", " + entry.getKey());
                    i++;
                }
                searchAdapterlist.notifyDataSetChanged();

            }
            @Override
            public void openBluetoothTime(int time) {
                Log.e(TAG, "openBluetoothTime:" + time);
                Message message = new Message();
                message.obj = time;
                message.what = OPENBLUETOOTH;
                handler.sendMessage(message);

            }

            @Override
            public void startBT(Intent intent) {
                Log.d(TAG, "startBT intent:" + intent.getAction());
                startActivityForResult(intent, REQUEST_ENABLE_BT);
            }

            @Override
            public void reSearchOldBluetoothdevice() {
                super.reSearchOldBluetoothdevice();
                searchOldBluetoothdevice();
            }
        };
        version = (TextView) findViewById(R.id.version);
        version.setText(Build.MODEL + getVersion());
        BluetoothDeviceName = (TextView) findViewById(R.id.bluetoothDeviceName);
        String blueDate = "bluetooth Name:" + bluetoothTool.getBluetoothName("Lyon Pi3_" + Build.MODEL) + ",   Mac:" + bluetoothTool.getBluetoothMac();
        BluetoothDeviceName.setText(blueDate);
        searchbluetoothDeviceListView = (ListView) findViewById(R.id.searchbluetoothDeviceListView);
        haveConnectBluetoothListView = (ListView) findViewById(R.id.haveConnectBluetoothListView);
        searchbluetoothDeviceListView.setAdapter(searchAdapterlist);
        haveConnectBluetoothListView.setAdapter(connectAdapterlist);
        bluetoothTool.findBuletoothDevice();
        bluetoothTool.openBlueTooth();
        searchbluetoothDeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "BluetoothDevice bluetoothDeviceListView [" + position + "] is click!");

            }
        });
        reSearch = (Button) findViewById(R.id.reSearch);
        reSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothTool.findBuletoothDevice();
                bluetoothTool.openBlueTooth();
                searchOldBluetoothdevice();
            }
        });
        reSearch.requestFocus();

        searchOldBluetoothdevice();



    }

    @Override
    protected void onResume() {
        //注册广播接收器
        mVolumeChangeObserver.registerReceiver();
        super.onResume();
    }

    @Override
    public void onVolumeChanged(int volume) {
        Log.d(TAG, "调节后的音乐音量大小为：" + volume);
        showVolumeDialog(volume);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //解注册广播接收器
        mVolumeChangeObserver.unregisterReceiver();
    }

    private void searchOldBluetoothdevice() {
        connectBluetoothDevice.clear();
        int i = 0;
        Set<BluetoothDevice> pairedDevices = bluetoothTool.getBluetoothAdapter().getBondedDevices();
        bluetoothDeviceName2 = new HashMap<>();
        for (BluetoothDevice bt : pairedDevices)
            bluetoothDeviceName2.put(bt.getAddress(), bt.getName());

        for (Map.Entry<String, String> entry : bluetoothDeviceName2.entrySet()) {
            connectBluetoothDevice.add("[" + i + "]:" + entry.getValue() + ", " + entry.getKey());
            i++;
        }
        connectAdapterlist.notifyDataSetChanged();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case OPENBLUETOOTH:
                    Log.d(TAG, "handler openBluetoothTime:" + message.obj + "s");
                    opentime.setText(message.obj + "s");
                    break;
            }
        }
    };


    private String getVersion() {
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return ", Ver:" + packageInfo.versionName + " " + packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "BluetoothTool requestCode:" + requestCode + " resultCode:" + resultCode);
        if (requestCode == REQUEST_ENABLE_BT) {
            Log.d(TAG, "BluetoothTool requestCode:" + requestCode);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothTool.onDestroy();
    }

    public void showVolumeDialog(int volume){
        volumeDialog.setVolume(volume);
        volumeDialog.show();
    }


}
