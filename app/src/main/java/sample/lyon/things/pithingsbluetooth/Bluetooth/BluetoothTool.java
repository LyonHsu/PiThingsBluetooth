package sample.lyon.things.pithingsbluetooth.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sample.lyon.things.pithingsbluetooth.Device.GetMacAddress;

/*
http://tw.gitbook.net/android/android_bluetooth.html

https://developer.android.com/guide/topics/connectivity/bluetooth#java
 */
public abstract class BluetoothTool {
    static String TAG = BluetoothTool.class.getName();
    Context context;
    //蓝牙适配器
    static BluetoothAdapter adapter;
    //搜索状态的标示
    private boolean mScanning = true;
    //蓝牙适配器List
    private List<BluetoothDevice> mBlueList;
    //蓝牙的回调地址
    private BluetoothAdapter.LeScanCallback mLesanCall;
    HashMap<String,String> bluetoothDeviceName=new HashMap<>();
    BluetoothAdapter mBluetoothAdapter;
    Handler mHandler;
    final int openBluetoothTime=300;
    Boolean isSearchNow=false;
    int time =openBluetoothTime;
    public BluetoothTool(Context context){
        this.context=context;
        //首先获取BluetoothManager
        BluetoothManager bluetoothManager=(BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            mBluetoothAdapter = bluetoothManager.getAdapter();
            String mac = getBluetoothMac();
             Log.d(TAG,"\n====================  Bluetooth mac : "+mac+" =====================  \n");
        }
        adapter = BluetoothAdapter.getDefaultAdapter();
        isSupportBlue();
        // 打开蓝牙
        if (!adapter.isEnabled() )
        {
            openBlueTooth();
        }
        //初始化List
        mBlueList = new ArrayList<>();
        //实例化蓝牙回调
        mLesanCall = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
                //返回三个对象 分类别是 蓝牙对象 蓝牙信号强度 以及蓝牙的广播包
                if(!mBlueList.contains(bluetoothDevice)){
                    mBlueList.add(bluetoothDevice);
                }
            }
        };
    }

    public String getBluetoothName(String bluetoothName){
        if(adapter==null)
            adapter = BluetoothAdapter.getDefaultAdapter();

        adapter.setName(bluetoothName);
        return adapter.getName();//获取本地蓝牙名称
    }

    public String getBluetoothMac(){
        if(adapter==null)
            adapter = BluetoothAdapter.getDefaultAdapter();
        String mac=GetMacAddress.getBluetoothMacAddress();
        return mac;

//        return adapter.getAddress();//获取本地蓝牙地址
    }

    public boolean isDiscovering(){
        if(adapter==null)
            adapter = BluetoothAdapter.getDefaultAdapter();
        return adapter.isDiscovering();//判断当前是否正在查找设备，是返回true
    }

    //判断是否支持蓝牙
    public boolean isSupportBlue(){
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.d(TAG,"设备不支持蓝牙");
            return true;
        }else {
            Log.d(TAG,"设备支持蓝牙");
            return false;
        }
    }

    public void findBuletoothDevice(){
        Log.d(TAG,"findBuletoothDevice");
// 设置广播信息过滤
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
// 注册广播接收器，接收并处理搜索结果
        context.registerReceiver(receiver, intentFilter);
// 寻找蓝牙设备，android会将查找到的设备以广播形式发出去
        adapter.startDiscovery();
        mScanning = true;
        bluetoothDeviceName=new HashMap<>();
        getBluetoothDeviceName(bluetoothDeviceName);
    }
    int bluetoothDeviceNameSize=0;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"BluetoothDevice onReceive "+intent.getAction());
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action) && isSearchNow) {
                // 获取查找到的蓝牙设备
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG,"BluetoothDevice 获取查找到的蓝牙设备 :"+device.getName()+":"+device.getAddress()+" mScanning:"+mScanning+ " "+device.getType());
                bluetoothDeviceName.put(device.getAddress(),device.getName());
                if(bluetoothDeviceName.size()!=bluetoothDeviceNameSize){
                    bluetoothDeviceNameSize = bluetoothDeviceName.size();
                    getBluetoothDeviceName(bluetoothDeviceName);
                }

            }//状态改变时
            else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDING://正在配对
                        Log.d( "BluetoothDevice BlueToothTestActivity", "正在配对......");
                        Toast.makeText(context,"正在配对......",Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothDevice.BOND_BONDED://配对结束
                        Log.d("BluetoothDevice BlueToothTestActivity", "完成配对");
                        Toast.makeText(context,"完成配对......",Toast.LENGTH_SHORT).show();
                        reSearchOldBluetoothdevice();
                        break;
                    case BluetoothDevice.BOND_NONE://取消配对/未配对
                        Log.d("BluetoothDevice BlueToothTestActivity", "取消配对");
                        Toast.makeText(context,"取消配对......",Toast.LENGTH_SHORT).show();
                    default:
                        break;
                }
            }
        }
    };

    public void reSearchOldBluetoothdevice(){

    }

    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public abstract void getBluetoothDeviceName(HashMap<String, String> bluetoothDeviceName);
    public abstract void openBluetoothTime(int time);

    public void onDestroy() {
        Log.e(TAG,"onDestroy()");
        context.unregisterReceiver(receiver);
        if (mBluetoothAdapter != null)
            mBluetoothAdapter.cancelDiscovery();
    }


    final Runnable runnable = new Runnable() {
        public void run() {
            if (time>0){
                time--;
                Log.d(TAG,"openBluetoothTime:"+time);

                openBluetoothTime(time);
                mHandler.postDelayed(runnable,1000);
            }else {
                time=0;
                openBluetoothTime(time);
                isSearchNow = false;
                isDiscovering();
            }
        }
    };

    public void openBlueTooth(){
        Log.d(TAG,"openBlueTooth");
        if(isSearchNow)
            return;
        if(mBluetoothAdapter==null)
            return;
        time=openBluetoothTime;
        if (!mBluetoothAdapter.isEnabled() ) {
            Log.d(TAG, "打开蓝牙");
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            // 设置蓝牙可见性，最多300秒
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, openBluetoothTime);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startBT(intent);
//            context.startActivity(intent);
            Toast.makeText(context,"Turned on",Toast.LENGTH_LONG).show();
            mHandler = new Handler();
            mHandler.post(runnable);
            openBluetoothTime(time);
            isSearchNow = true;
        }else{
            Log.d(TAG, "蓝牙可被搜尋");
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            // 设置蓝牙可见性，最多300秒
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, openBluetoothTime);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startBT(intent);
//            context.startActivity(intent);
            Toast.makeText(context,"Turned on",Toast.LENGTH_LONG).show();
            mHandler = new Handler();
            mHandler.post(runnable);
            openBluetoothTime(time);
            isSearchNow = true;
            Toast.makeText(context,"Already on", Toast.LENGTH_LONG).show();
        }
    }

    public abstract void startBT( Intent intent);





}
