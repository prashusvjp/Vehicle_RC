package com.example.vehiclerc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class ControlsActivity extends AppCompatActivity {
    ImageView brakeImgView, leftIndicatorImgView, rightIndicatorImgView, powerImgView;
    boolean powerState = false, streamState = false, leftIndicatorState = false, rightIndicatorState = false;
    VideoView videoView;
    Spinner devicesListSpinner;
    static int REQUEST_ENABLE_BT = 1441;
    static int REQUEST_ENABLE_MBT = 1440;
    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket socket = null;
    Set<BluetoothDevice> pairedDevices;
    HashMap<String, BluetoothDevice> deviceMap = new HashMap<>();
    final static int FORWARD = 0, LEFT=4, RIGHT=1, BACKWARD=7, LEFT_INDICATOR=8, RIGHT_INDICATOR=9, AUTO_PILOT=10, STOP=11;

    //events
    View.OnClickListener leftIndicator = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            leftIndicatorState=!leftIndicatorState;
            rightIndicatorState=false;
            controlViews(LEFT_INDICATOR);
        }
    };

    View.OnClickListener rightIndicator = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            rightIndicatorState=!rightIndicatorState;
            leftIndicatorState=false;
            controlViews(RIGHT_INDICATOR);
        }
    };

    View.OnTouchListener moveForward = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return false;
        }
    };
    View.OnTouchListener moveBackward = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return false;
        }
    };
    View.OnTouchListener moveLeft = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controls);
        initializeViews();
        setUpBluetooth();

        devicesListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String seletedItem = adapterView.getItemAtPosition(i).toString();
                if (!seletedItem.equals("---None---")) {
                    if(socket!=null){
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH)
                            == PackageManager.PERMISSION_GRANTED)
                        try{
                            Toast.makeText(ControlsActivity.this, deviceMap.get(seletedItem).getAddress()+
                                    ", "+seletedItem, Toast.LENGTH_SHORT).show();
                            socket = deviceMap.get(seletedItem).createRfcommSocketToServiceRecord(UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee"));
                            socket.connect();
                            OutputStream mmOutputStream = socket.getOutputStream();
                            mmOutputStream.write("Hola mi estimoda".getBytes());

                        }catch(IOException e){
                            Toast.makeText(ControlsActivity.this, "Sorry, something went wrong.", Toast.LENGTH_SHORT).show();
                        }


                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setUpBluetooth() {
        bluetoothManager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter != null) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    String[] permissions = new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT};
                    ActivityCompat.requestPermissions(this,
                            permissions,
                            REQUEST_ENABLE_MBT);
                }else{
                    updateSpinner();
                }
            }else{
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.BLUETOOTH},
                            REQUEST_ENABLE_BT);
                }else{
                    updateSpinner();
                }
            }
        }else{
            //Device doesn't support bluetooth
            Toast.makeText(
                    this, "Sorry, no bluetooth device found",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @SuppressLint("MissingPermission")
    private void updateSpinner() {
        try {
            bluetoothAdapter.enable();
            pairedDevices = this.bluetoothAdapter.getBondedDevices();
            List<String> devicesList = new ArrayList<String>();
            devicesList.add("---None---");
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    @SuppressLint("MissingPermission") String deviceName = device.getName();
                    devicesList.add(deviceName);
                    deviceMap.put(deviceName, device);
                }
            }
            ArrayAdapter<String> arrayAdapter =
                    new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, devicesList);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            devicesListSpinner.setAdapter(arrayAdapter);
        }catch(Exception e) {
            Toast.makeText(this, "Sorry,something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setUpBluetooth();
            }else{
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }else if(requestCode == REQUEST_ENABLE_MBT){
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                setUpBluetooth();
            }else{
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void initializeViews() {
        getSupportActionBar().hide();
        brakeImgView = findViewById(R.id.brakeImgView);

        leftIndicatorImgView = findViewById(R.id.leftIndicatorImgView);
        leftIndicatorImgView.setOnClickListener(leftIndicator);

        rightIndicatorImgView = findViewById(R.id.rightIndicatorImgView);
        rightIndicatorImgView.setOnClickListener(rightIndicator);

        powerImgView = findViewById(R.id.powerImgView);
        devicesListSpinner = findViewById(R.id.devicesListSpinner);
    }

    private void handleIndicatorViews(ImageView v,int operation){
        Timer timer=new Timer();
        TimerTask task = new TimerTask() {
            boolean state=false;
            @Override
            public void run() {
                state=!state;
                v.setColorFilter(getResources().getColor(state?R.color.green:R.color.grey));
                if(!(operation==8?leftIndicatorState:rightIndicatorState)) {
                    v.setColorFilter(getResources().getColor(R.color.grey));
                    timer.cancel();
                }
            }
        };
        timer.scheduleAtFixedRate(task,0,1000);
    }

    private void controlViews(int id){

        switch(id){
            case LEFT_INDICATOR:
                if(!leftIndicatorState){
                    leftIndicatorImgView.setColorFilter(getResources().getColor(R.color.grey));
                }else{
                    handleIndicatorViews(leftIndicatorImgView,id);
                    rightIndicatorImgView.setColorFilter(getResources().getColor(R.color.grey));
                }
                break;
            case RIGHT_INDICATOR:
                if(!rightIndicatorState){
                    rightIndicatorImgView.setColorFilter(getResources().getColor(R.color.grey));
                }else{
                    rightIndicatorImgView.setColorFilter(getResources().getColor(R.color.green));
                    handleIndicatorViews(rightIndicatorImgView,id);
                    leftIndicatorImgView.setColorFilter(getResources().getColor(R.color.grey));
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + id);
        }
    }
}