package fr.cpe.iot_app;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import fr.cpe.iot_app.threads.TalkThread;

public class MainActivity extends AppCompatActivity {

    private InetAddress address; // Structure Java décrivant une adresse résolue
    private DatagramSocket UDPSocket; // Structure Java permettant d'accéder au réseau

    private EditText ipAddressField;
    private EditText portField;

    private String currentIpAddress = "";

    private TextView element1;
    private TextView element2;

    private final Map<String, String> sensorValues = new HashMap<>();

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

        /* initNetwork();

        ListenThreadEventListener listener = data -> runOnUiThread(() -> textReceived.setText(String.format("Response: %s", data)));
        ListenThread listenThread = new ListenThread(listener, UDPSocket);
        listenThread.start();

        buttonSend.setOnClickListener(v -> {
            String message = textToSend.getText().toString();
            String portString = portField.getText().toString();
            try {
                int port = Integer.parseInt(portString);
                TalkThread talkThread = new TalkThread(message, UDPSocket, address, port);
                talkThread.start();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }); */
    }

    private void initNetwork() {
        try {
            UDPSocket = new DatagramSocket();
            String ipAddress = ipAddressField.getText().toString();
            address = InetAddress.getByName(ipAddress);

            currentIpAddress = ipAddress;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exchangeElements(View view) {
        String text1 = element1.getText().toString();
        String text2 = element2.getText().toString();

        element1.setText(text2);
        element2.setText(text1);

        String sensorAbbreviation1 = getKeyByValue(sensorValues, text2);
        String sensorAbbreviation2 = getKeyByValue(sensorValues, text1);

        String message = sensorAbbreviation1 + sensorAbbreviation2;
        sendMessage(message);
    }

    private void sendMessage(String message) {
        if (!currentIpAddress.equals(ipAddressField.getText().toString())) {
            initNetwork();
        }

        String portString = portField.getText().toString();
        try {
            int port = Integer.parseInt(portString);
            TalkThread talkThread = new TalkThread(message, UDPSocket, address, port);
            talkThread.start();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private String getKeyByValue(Map<String, String> map, String value) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }

}