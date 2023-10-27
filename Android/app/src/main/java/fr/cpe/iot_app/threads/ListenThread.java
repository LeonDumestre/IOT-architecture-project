package fr.cpe.iot_app.threads;

import android.widget.TextView;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ListenThread extends Thread {

    private final ListenThreadEventListener listener;
    private final DatagramSocket UDPSocket;

    public ListenThread(ListenThreadEventListener listener, DatagramSocket UDPSocket) {
        this.listener = listener;
        this.UDPSocket = UDPSocket;
    }

    public void run() {
        while (true) {
            receiveMessage();
        }
    }

    private void receiveMessage() {
        try {
            byte[] data = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            UDPSocket.receive(packet);
            String message = new String(packet.getData(), 0, packet.getLength());
            listener.onEvent(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}