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
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class ControlsActivity extends AppCompatActivity {
    ImageView brakeImgView, leftIndicatorImgView, rightIndicatorImgView, powerImgView, refreshImgView;
    ImageButton moveForwardImgView, moveBackwardImgView, moveLeftImgView, moveRightImgView;
    CompoundButton autopilotSwitch;
    boolean powerState = false, autoPilotState = false, leftIndicatorState = false,
            rightIndicatorState = false;
    VideoView videoView;
    Spinner devicesListSpinner;
    static int REQUEST_ENABLE_BT = 1441;
    static int REQUEST_ENABLE_MBT = 1440;
    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket socket = null;
    OutputStream bOutputStream=null;
    Set<BluetoothDevice> pairedDevices;
    HashMap<String, BluetoothDevice> deviceMap = new HashMap<>();
    final static int FORWARD = 15, LEFT=6, RIGHT=3, BACKWARD=7, LEFT_INDICATOR=9, RIGHT_INDICATOR=10,
            AUTO_PILOT=11, STOP=8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controls);
        initializeViews();

    }

    @Override
    protected void onStart() {
        super.onStart();
        setUpBluetooth();
    }

    private void setUpBluetooth() {
        bluetoothManager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter != null) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.BLUETOOTH_SCAN,
                                    Manifest.permission.BLUETOOTH_CONNECT},
                            REQUEST_ENABLE_MBT);
                }else{
                    bluetoothAdapter.enable();
                    updateSpinner();
                }
            }else{
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.BLUETOOTH},
                            REQUEST_ENABLE_BT);
                }else{
                    bluetoothAdapter.enable();
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

        moveForwardImgView = findViewById(R.id.arrowForwardImgView);
        moveForwardImgView.setOnTouchListener(movements);

        moveBackwardImgView =findViewById(R.id.arrowBackwardImgView);
        moveBackwardImgView.setOnTouchListener(movements);

        moveLeftImgView = findViewById(R.id.arrowLeftImgView);
        moveLeftImgView.setOnTouchListener(movements);

        moveRightImgView=findViewById(R.id.arrowRightImgView);
        moveRightImgView.setOnTouchListener(movements);

        devicesListSpinner = findViewById(R.id.devicesListSpinner);
        devicesListSpinner.setOnItemSelectedListener(listDevices);

        autopilotSwitch = findViewById(R.id.autoPilotSwitch);
        autopilotSwitch.setOnCheckedChangeListener(switchToAuto);

        powerImgView = findViewById(R.id.powerImgView);
        powerImgView.setOnClickListener(power);

        refreshImgView=findViewById(R.id.refreshImgView);
        refreshImgView.setOnClickListener(refresh);
    }

    private void handleIndicatorViews(ImageView v,int operation){
        Timer timer=new Timer();
        TimerTask task = new TimerTask() {
            boolean state=false;
            @Override
            public void run() {
                state=!state;
                v.setColorFilter(getResources().getColor(state?R.color.green:R.color.grey));
                if(!(operation==9?leftIndicatorState:rightIndicatorState)) {
                    v.setColorFilter(getResources().getColor(R.color.grey));
                    timer.cancel();
                }
            }
        };
        timer.scheduleAtFixedRate(task,0,500);
    }

    private void controlViews(int id){

        switch(id){
            case LEFT_INDICATOR:
                if(!leftIndicatorState){
                    leftIndicatorImgView
                            .setColorFilter(getResources().getColor(R.color.grey));
                }else{
                    leftIndicatorImgView
                            .setColorFilter(getResources().getColor(R.color.green));
                    handleIndicatorViews(leftIndicatorImgView,id);
                    rightIndicatorImgView
                            .setColorFilter(getResources().getColor(R.color.grey));
                }
                break;
            case RIGHT_INDICATOR:
                if(!rightIndicatorState){
                    rightIndicatorImgView
                            .setColorFilter(getResources().getColor(R.color.grey));
                }else{
                    rightIndicatorImgView
                            .setColorFilter(getResources().getColor(R.color.green));
                    handleIndicatorViews(rightIndicatorImgView,id);
                    leftIndicatorImgView
                            .setColorFilter(getResources().getColor(R.color.grey));
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + id);
        }
    }

    //events
    View.OnClickListener leftIndicator = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (powerState) {
                leftIndicatorState = !leftIndicatorState;
                rightIndicatorState = false;
                controlViews(LEFT_INDICATOR);
                try {
                    bOutputStream.write(LEFT_INDICATOR);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else
                Toast.makeText(ControlsActivity.this,
                        "Please turn on the device", Toast.LENGTH_SHORT).show();
        }
    };

    View.OnClickListener rightIndicator = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (powerState) {
                rightIndicatorState = !rightIndicatorState;
                leftIndicatorState = false;
                try {
                    bOutputStream.write(RIGHT_INDICATOR);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                controlViews(RIGHT_INDICATOR);
            }else{
                Toast.makeText(ControlsActivity.this,
                        "Please turn on the device", Toast.LENGTH_SHORT).show();
            }
        }
    };

    View.OnClickListener power = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(bOutputStream == null){
                Toast.makeText(ControlsActivity.this, "No device connected", Toast.LENGTH_SHORT).show();
                devicesListSpinner.performClick();
            }else{
                powerState=!powerState;
                powerImgView.setColorFilter(powerState?Color.GREEN:Color.RED);
            }

        }
    };

    ImageButton.OnTouchListener movements = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (powerState) {
                int data = STOP;
                int id = view.getId();
                data = id == moveForwardImgView.getId() ? FORWARD : id == moveBackwardImgView.getId()
                        ? BACKWARD : id == moveLeftImgView.getId() ? LEFT : RIGHT;
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:
                        try {
                            view.setBackgroundColor(getColor(R.color.transparent));
                            bOutputStream.write(STOP);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        enableArrows();
                        break;
                    case MotionEvent.ACTION_DOWN:
                        try {
                            view.setBackgroundColor(getColor(R.color.grey));
                            bOutputStream.write(data);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        disableArrows(view);
                        break;
                }
            }else{
                Toast.makeText(ControlsActivity.this, "Please turn on the device", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
    };

    CompoundButton.OnCheckedChangeListener switchToAuto = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if(powerState)
                try {
                    if(bOutputStream != null) {
                        bOutputStream.write(AUTO_PILOT);
                        autoPilotState=!autoPilotState;
                        if(!autoPilotState)
                            bOutputStream.write(STOP);
                    }else {
                        Toast.makeText(ControlsActivity.this, "No device connected", Toast.LENGTH_SHORT).show();
                        autoPilotState=false;
                        autopilotSwitch.setChecked(false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    autopilotSwitch.setChecked(false);
                }
            else {
                Toast.makeText(ControlsActivity.this, "Please turn on the device", Toast.LENGTH_SHORT).show();
                autopilotSwitch.setChecked(false);
            }
        }
    };

    AdapterView.OnItemSelectedListener listDevices = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            String seletedItem = adapterView.getItemAtPosition(i).toString();
            if (!seletedItem.equals("---None---")) {
                if(socket!=null){
                    try {
                        socket.close();
                        socket=null;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH)
                        == PackageManager.PERMISSION_GRANTED)
                    try{
                        socket = deviceMap.get(seletedItem)
                                .createRfcommSocketToServiceRecord(UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee"));
                        bluetoothAdapter.cancelDiscovery();
                        socket.connect();
                        Toast.makeText(ControlsActivity.this,
                                "Connected to " + deviceMap.get(seletedItem).getName() +" Successfully!",
                                Toast.LENGTH_SHORT).show();
                        bOutputStream = socket.getOutputStream();

                    }catch(Exception e){
                        Toast.makeText(ControlsActivity.this, "Sorry, something went wrong while connecting to bluetooth", Toast.LENGTH_SHORT).show();
                    }
            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    View.OnClickListener refresh = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            updateSpinner();
            powerState=false;
            powerImgView.setColorFilter(Color.RED);
            bOutputStream=null;
            socket=null;
        }
    };

    private void enableArrows(){
        moveForwardImgView.setEnabled(true);
        moveBackwardImgView.setEnabled(true);
        moveLeftImgView.setEnabled(true);
        moveRightImgView.setEnabled(true);
    }

    private void disableArrows(View v){
        moveForwardImgView.setEnabled(false);
        moveBackwardImgView.setEnabled(false);
        moveLeftImgView.setEnabled(false);
        moveRightImgView.setEnabled(false);
        v.setEnabled(true);
    }
}