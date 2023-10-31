package fr.cpe.iot_app;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import fr.cpe.iot_app.threads.AskValueThread;
import fr.cpe.iot_app.threads.ListenThread;
import fr.cpe.iot_app.threads.ListenThreadEventListener;
import fr.cpe.iot_app.threads.TalkThread;

public class MainActivity extends AppCompatActivity {

    private InetAddress address; // Structure Java décrivant une adresse résolue
    private DatagramSocket UDPSocket; // Structure Java permettant d'accéder au réseau
    private int port;

    private EditText ipAddressField;
    private EditText portField;

    private TextView element1;
    private TextView element2;

    private TextView luminosityValue;
    private TextView temperatureValue;

    private final Map<String, String> sensorValues = new HashMap<>();
    AskValueThread askValueThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ipAddressField = findViewById(R.id.ip_address);
        portField = findViewById(R.id.port);

        element1 = findViewById(R.id.element1);
        element2 = findViewById(R.id.element2);

        sensorValues.put("L", getString(R.string.luminosity_text));
        sensorValues.put("T", getString(R.string.temperature_text));

        element1.setText(sensorValues.get("L"));
        element2.setText(sensorValues.get("T"));

        luminosityValue = findViewById(R.id.luminosity_value);
        temperatureValue = findViewById(R.id.temperature_value);

        Button configButton = findViewById(R.id.config_button);
        Button swapButton = findViewById(R.id.swap_button);

        configButton.setOnClickListener(v -> {
            initNetwork();
            sendToast("Initialisation du réseau");
        });
        swapButton.setOnClickListener(v -> {
            exchangeElements();
            sendToast("Changement envoyé");
        });
    }

    public void initNetwork() {
        try {
            if (askValueThread != null) {
                askValueThread.interrupt();
            }

            String portString = portField.getText().toString();
            port = Integer.parseInt(portString);

            UDPSocket = new DatagramSocket();
            String ipAddress = ipAddressField.getText().toString();
            address = InetAddress.getByName(ipAddress);
            initReceiver();
            initValueRequest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initReceiver() {
        ListenThreadEventListener listener = byte_data -> runOnUiThread(() -> {
            String data = new String(byte_data.getBytes(), StandardCharsets.UTF_8);
            Log.e("MainActivity", "Received data: " + data);

            if(data == null || data.isEmpty() || data.contains(" ") || data.equals("-1") || data.length() < 3) {
                Log.e("MainActivity", "Error on server");
                return;
            }

            String[] parts = data.split(";");
            temperatureValue.setText(parts[1] + "°C");
            luminosityValue.setText(parts[0] + "%");
        });
        ListenThread listenThread = new ListenThread(listener, UDPSocket);
        listenThread.start();
    }

    public void initValueRequest() {
        askValueThread = new AskValueThread(UDPSocket, address, port);
        askValueThread.start();
    }

    public void exchangeElements() {
        String text1 = element1.getText().toString();
        String text2 = element2.getText().toString();

        element1.setText(text2);
        element2.setText(text1);

        String sensorAbbreviation1 = getKeyByValue(sensorValues, text2);
        String sensorAbbreviation2 = getKeyByValue(sensorValues, text1);

        String message = sensorAbbreviation1 +  ";" + sensorAbbreviation2 + "~";
        sendMessage(message);
    }

    private void sendMessage(String message) {
        TalkThread talkThread = new TalkThread(message, UDPSocket, address, port);
        talkThread.start();
    }

    private String getKeyByValue(Map<String, String> map, String value) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void sendToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}