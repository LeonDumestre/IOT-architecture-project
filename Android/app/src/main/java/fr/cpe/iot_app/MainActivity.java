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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fr.cpe.iot_app.threads.AskValueThread;
import fr.cpe.iot_app.threads.ListenThread;
import fr.cpe.iot_app.threads.ListenThreadEventListener;
import fr.cpe.iot_app.threads.TalkThread;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String CELSIUS_SYMBOL = "°C";
    private static final String PERCENTAGE_SYMBOL = "%";

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

    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUIComponents();
        initButtons();
    }

    private void initUIComponents() {
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
    }

    private void initButtons() {
        Button configButton = findViewById(R.id.config_button);
        Button swapButton = findViewById(R.id.swap_button);

        configButton.setOnClickListener(v -> {
            runNetwork();
            sendToast("Initialisation du réseau");
        });
        swapButton.setOnClickListener(v -> {
            exchangeElements();
            sendToast("Changement envoyé");
        });
    }

    private void runNetwork() {
        try {
            // stop askValueThread if running to set new network data
            if (askValueThread != null) {
                askValueThread.interrupt();
            }

            // get, format and set network data
            String portString = portField.getText().toString();
            port = Integer.parseInt(portString);

            UDPSocket = new DatagramSocket();
            String ipAddress = ipAddressField.getText().toString();
            address = InetAddress.getByName(ipAddress);

            // run threads to get values and listen response
            initReceiver();
            initValueRequest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initReceiver() {
        // set listener executed when thread receives data
        ListenThreadEventListener listener = byte_data -> runOnUiThread(() -> {
            // format received data
            String data = new String(byte_data.getBytes(), StandardCharsets.UTF_8);
            Log.e(TAG, "Received data: " + data);

            // check data integrity
            if(data.isEmpty() || data.contains(" ") || data.equals("-1") || data.length() < 3) {
                Log.e(TAG, "Error on server");
                return;
            }

            // split and set received data in UI components
            String[] parts = data.split(";");
            temperatureValue.setText(String.format("%s%s", parts[1], CELSIUS_SYMBOL));
            luminosityValue.setText(String.format("%s%s", parts[0], PERCENTAGE_SYMBOL));
        });
        
        ListenThread listenThread = new ListenThread(listener, UDPSocket);
        executorService.execute(listenThread);
    }

    public void initValueRequest() {
        askValueThread = new AskValueThread(UDPSocket, address, port);
        executorService.execute(askValueThread);
    }

    public void exchangeElements() {
        // get current values
        String text1 = element1.getText().toString();
        String text2 = element2.getText().toString();

        // exchange text view values
        element1.setText(text2);
        element2.setText(text1);

        // get exchanges values
        String sensorAbbreviation1 = getKeyByValue(sensorValues, text2);
        String sensorAbbreviation2 = getKeyByValue(sensorValues, text1);

        // format message to send
        String message = sensorAbbreviation1 +  ";" + sensorAbbreviation2 + "~";
        sendMessage(message);
    }

    private void sendMessage(String message) {
        TalkThread talkThread = new TalkThread(message, UDPSocket, address, port);
        executorService.execute(talkThread);
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

    @Override
    protected void onResume() {
        super.onResume();
        executorService = Executors.newCachedThreadPool();
    }

    @Override
    protected void onPause() {
        super.onPause();
        executorService.shutdownNow();
    }
}