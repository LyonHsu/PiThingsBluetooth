package sample.lyon.things.pithingsbluetooth;

import android.Manifest;
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
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sample.lyon.things.pithingsbluetooth.Bluetooth.BluetoothTool;
import sample.lyon.things.pithingsbluetooth.Volume.VolumeChangeObserver;
import sample.lyon.things.pithingsbluetooth.Volume.VolumeDialog;

import static android.bluetooth.BluetoothClass.Device.Major.AUDIO_VIDEO;
import static android.bluetooth.BluetoothClass.Device.Major.COMPUTER;
import static android.bluetooth.BluetoothClass.Device.Major.MISC;
import static android.bluetooth.BluetoothClass.Device.Major.NETWORKING;
import static android.bluetooth.BluetoothClass.Device.Major.PHONE;

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
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    BluetoothTool bluetoothTool;
    TextView BluetoothDeviceName;
    TextView version;
    ListView searchbluetoothDeviceListView;
    ListView haveConnectBluetoothListView;
    ArrayAdapter searchAdapterlist;
    ArrayAdapter connectAdapterlist;
    List<String> searchBluetoothDevice;
    List<String> connectBluetoothDevice;
    List<BluetoothDevice> connectBluetoothDeviceD;
    List<BluetoothDevice> searchBluetoothDeviceD;
    Button reSearch;
    TextView opentime;
    final int OPENBLUETOOTH = 0;
    final int REQUEST_ENABLE_BT = 100;
    private VolumeChangeObserver mVolumeChangeObserver;
    VolumeDialog volumeDialog;
    private static final int REQUEST_CODE = 2; // 请求码
    public static int OVERLAY_PERMISSION_REQ_CODE = 1234;
    // 所需的全部权限
    static final String[] PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private PermissionsChecker mPermissionsChecker; // 权限检测器
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPermissionsChecker = new PermissionsChecker(this);
        if (mPermissionsChecker.lacksPermissions(PERMISSIONS)) {
            startPermissionsActivity();
            Log.d(TAG,"20170601 versionCode:");
        }
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
        connectBluetoothDeviceD = new ArrayList<>();
        searchBluetoothDeviceD = new ArrayList<>();
        searchAdapterlist = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                searchBluetoothDevice);
        connectAdapterlist = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                connectBluetoothDevice);
        opentime = (TextView) findViewById(R.id.openTime);
        bluetoothTool = new BluetoothTool(this) {
            @Override
            public void getBluetoothDeviceName(HashMap<String, String> bluetoothDeviceName,BluetoothDevice device) {
                searchBluetoothDevice.clear();
                searchBluetoothDeviceD.clear();
                int i = 0;
                for (Map.Entry<String, String> entry : bluetoothDeviceName.entrySet()) {
                    searchBluetoothDevice.add("[" + i + "]:" + entry.getValue() + ", " + entry.getKey());
                    searchBluetoothDeviceD.add(device);
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

        String bluetoothType = bluetoothTool.getBlueToothType(bluetoothTool.getBluetoothClass());
        version.setText(Build.MODEL + getVersion() +bluetoothType);
        BluetoothDeviceName = (TextView) findViewById(R.id.bluetoothDeviceName);
        String blueDate = "bluetooth Name:" + bluetoothTool.getBluetoothName("Lyon Pi3_" + Build.MODEL) + ",   Mac:" + bluetoothTool.getBluetoothMac();
        BluetoothDeviceName.setText(blueDate);
        searchbluetoothDeviceListView = (ListView) findViewById(R.id.searchbluetoothDeviceListView);
        haveConnectBluetoothListView = (ListView) findViewById(R.id.haveConnectBluetoothListView);
        searchbluetoothDeviceListView.setSelector(getDrawable(R.drawable.btn_light_blue));
        haveConnectBluetoothListView.setSelector(getDrawable(R.drawable.btn_light_blue));
        searchbluetoothDeviceListView.setAdapter(searchAdapterlist);
        haveConnectBluetoothListView.setAdapter(connectAdapterlist);
        bluetoothTool.findBuletoothDevice();
        bluetoothTool.openBlueTooth();
        searchbluetoothDeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "BluetoothDevice bluetoothDeviceListView [" + position + "] is click!");
                try {
                    Log.d(TAG, "Have Connect BluetoothDevice bluetoothDeviceListView [" + position + "] is click!");
                    String name[] = searchBluetoothDevice.get(position).split(",");
                    String deviceName=name[0];
                    String address=name[1].replace(" ","");
                    Log.d(TAG, "Have Connect name : " + deviceName + "address:" + address + " is click!");
                    bluetoothTool.stopSearthBltDevice();
                    BluetoothDevice device = connectBluetoothDeviceD.get(position);//bluetoothTool.getBluetoothAdapter().getRemoteDevice(name[1]);
                    bluetoothTool.connectDevice(device);
                }catch (Exception e){
                    Log.e(TAG,"Have Connect onItemClick Exception:"+e);
                }

            }
        });

        haveConnectBluetoothListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    Log.d(TAG, "Have Connect BluetoothDevice bluetoothDeviceListView [" + position + "] is click!");
                    String name[] = connectBluetoothDevice.get(position).split(",");
                    String deviceName=name[0];
                    String address=name[1].replace(" ","");
                    Log.d(TAG, "Have Connect name : " + deviceName + "address:" + address + " is click!");
                    bluetoothTool.stopSearthBltDevice();
                    BluetoothDevice device = connectBluetoothDeviceD.get(position);//bluetoothTool.getBluetoothAdapter().getRemoteDevice(name[1]);
                    bluetoothTool.connectDevice(device);
                }catch (Exception e){
                    Log.e(TAG,"Have Connect onItemClick Exception:"+e);
                }
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

    public void searchOldBluetoothdevice() {
        connectBluetoothDevice.clear();
        connectBluetoothDeviceD.clear();
        int i = 0;
        Set<BluetoothDevice> pairedDevices = bluetoothTool.getBluetoothAdapter().getBondedDevices();
        // https://blog.csdn.net/jasonwang18/article/details/70210319
        for (BluetoothDevice bt : pairedDevices) {
            int classint = bt.getBluetoothClass().getMajorDeviceClass();
            String type=bluetoothTool.getBlueToothType(classint);
            connectBluetoothDevice.add("[" + i + "]:" + bt.getName()+"("+type + "), " + bt.getAddress());
            connectBluetoothDeviceD.add(bt);
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
        Log.d(TAG, "onActivityResult requestCode:" + requestCode + " resultCode:" + resultCode);
        if (requestCode == REQUEST_ENABLE_BT) {
            Log.d(TAG, "BluetoothTool Enable requestCode:" + requestCode);
        }
        // 拒绝时, 关闭页面, 缺少主要权限, 无法运行
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                // SYSTEM_ALERT_WINDOW permission not granted...
                Toast.makeText(this, "Permission Denieddd by user.Please Check it in Settings", Toast.LENGTH_SHORT).show();
                finish();
            }
        }else if (requestCode == REQUEST_CODE && resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
            Toast.makeText(getBaseContext(),"I am GG!!",Toast.LENGTH_SHORT).show();
            finish();
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

    private void startPermissionsActivity() {
        PermissionsActivity.startActivityForResult(this, REQUEST_CODE, PERMISSIONS);
    }


}
