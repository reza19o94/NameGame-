package com.example.namegame;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class LobbyActivity extends AppCompatActivity {

    private TextView tvStatus;
    private ListView listViewPlayers;
    private Button btnStartGame;
    private ArrayAdapter<String> playersAdapter;
    private ArrayList<String> playersList = new ArrayList<>();

    private BluetoothConnectionService connectionService;
    private boolean isHost = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        tvStatus = findViewById(R.id.tvStatus);
        listViewPlayers = findViewById(R.id.listViewPlayers);
        btnStartGame = findViewById(R.id.btnStartGame);

        playersAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, playersList);
        listViewPlayers.setAdapter(playersAdapter);

        Intent intent = getIntent();
        BluetoothDevice device = intent.getParcelableExtra("device");

        connectionService = new BluetoothConnectionService(this, new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case Constants.MESSAGE_STATE_CHANGE:
                        switch (msg.arg1) {
                            case BluetoothConnectionService.STATE_CONNECTED:
                                tvStatus.setText("Connected");
                                break;
                            case BluetoothConnectionService.STATE_CONNECTING:
                                tvStatus.setText("Connecting...");
                                break;
                            case BluetoothConnectionService.STATE_LISTEN:
                            case BluetoothConnectionService.STATE_NONE:
                                tvStatus.setText("Not connected");
                                break;
                        }
                        break;
                    case Constants.MESSAGE_READ:
                        byte[] readBuf = (byte[]) msg.obj;
                        String message = new String(readBuf, 0, msg.arg1);
                        handleMessage(message);
                        break;
                }
                return true;
            }
        }));

        if (device == null) {
            isHost = true;
            tvStatus.setText("Hosting game...");
            playersList.add("You (Host)");
            connectionService.start();
        } else {
            tvStatus.setText("Connecting to host...");
            playersList.add("You");
            connectionService.connect(device);
        }

        playersAdapter.notifyDataSetChanged();

        btnStartGame.setEnabled(isHost);
        btnStartGame.setOnClickListener(v -> startGame());
    }

    private void handleMessage(String message) {
        if (message.startsWith("PLAYER:")) {
            String playerName = message.substring(7);
            if (!playersList.contains(playerName)) {
                playersList.add(playerName);
                playersAdapter.notifyDataSetChanged();
            }
        } else if (message.equals("START_GAME")) {
            startGame();
        }
    }

    private void startGame() {
        if (isHost) {
            // Notify all players to start game
            connectionService.write("START_GAME".getBytes());
        }
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("players", playersList.toArray(new String[0]));
        intent.putExtra("isHost", isHost);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectionService != null) {
            connectionService.stop();
        }
    }
}
