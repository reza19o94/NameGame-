package com.example.namegame;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> devicesArrayAdapter;
    private ArrayList<String> deviceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnEnableBluetooth = findViewById(R.id.btnEnableBluetooth);
        Button btnDiscoverable = findViewById(R.id.btnDiscoverable);
        Button btnRefresh = findViewById(R.id.btnRefresh);
        ListView listViewDevices = findViewById(R.id.listViewDevices);

        devicesArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);
        listViewDevices.setAdapter(devicesArrayAdapter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnEnableBluetooth.setOnClickListener(v -> enableBluetooth());
        btnDiscoverable.setOnClickListener(v -> makeDiscoverable());
        btnRefresh.setOnClickListener(v -> refreshDevices());

        listViewDevices.setOnItemClickListener((parent, view, position, id) -> {
            String deviceInfo = devicesArrayAdapter.getItem(position);
            if (deviceInfo != null) {
                String macAddress = deviceInfo.substring(deviceInfo.length() - 17);
                connectToDevice(macAddress);
            }
        });

        refreshDevices();
    }

    private void enableBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
    }

    private void makeDiscoverable() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }

    private void refreshDevices() {
        deviceList.clear();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceList.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            deviceList.add("No paired devices");
        }
        devicesArrayAdapter.notifyDataSetChanged();
    }

    private void connectToDevice(String macAddress) {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAddress);
        Intent intent = new Intent(this, LobbyActivity.class);
        intent.putExtra("device", device);
        startActivity(intent);
    }

    public void onCreateGame(View view) {
        if (bluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(this, LobbyActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Please enable Bluetooth first", Toast.LENGTH_SHORT).show();
        }
    }
}
