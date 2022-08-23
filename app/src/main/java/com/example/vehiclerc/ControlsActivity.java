package com.example.vehiclerc;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ControlsActivity extends AppCompatActivity {
    ImageView brakeImgView, leftIndicatorImgView, rightIndicatorImgView, powerImgView;
    boolean powerState = false, streamState = false, leftIndicatorState = false, rightIndicatorState = false;
    VideoView videoView;
    Spinner devicesListSpinner;
    static int REQUEST_ENABLE_BT = 1441;
    BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
    BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controls);
        initializeViews();
        setUpBluetooth();
    }

    private void setUpBluetooth() {
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            }
            try {
                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                List<String> devicesList = new ArrayList<String>();
                if (pairedDevices.size() > 0) {
                    // There are paired devices. Get the name and address of each paired device.
                    for (BluetoothDevice device : pairedDevices) {
                        String deviceName = device.getName();
                        String deviceHardwareAddress = device.getAddress(); // MAC address
                        devicesList.add(deviceName);
                    }
                }
                ArrayAdapter<String> arrayAdapter =
                        new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,devicesList);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                devicesListSpinner.setAdapter(arrayAdapter);
            }catch(Exception e){
                Toast.makeText(this, "Sorry,something went wrong!", Toast.LENGTH_SHORT).show();
            }
        }else{
            //Device doesn't support bluetooth
            Toast.makeText(
                    this, "Sorry, no bluetooth device found",
                    Toast.LENGTH_SHORT
            ).show();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission denied to use bluetooth", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initializeViews() {
        getSupportActionBar().hide();
        brakeImgView = findViewById(R.id.brakeImgView);
        leftIndicatorImgView = findViewById(R.id.leftIndicatorImgView);
        rightIndicatorImgView = findViewById(R.id.rightIndicatorImgView);
        powerImgView = findViewById(R.id.powerImgView);
        devicesListSpinner = findViewById(R.id.devicesListSpinner);
    }
}