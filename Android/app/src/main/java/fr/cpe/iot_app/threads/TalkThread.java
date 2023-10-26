package fr.cpe.iot_app.threads;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class TalkThread extends Thread {

    private final int port;
    private final InetAddress address;
    private final DatagramSocket UDPSocket;
    private final String messageToSend;

    public TalkThread(String messageToSend, DatagramSocket UDPSocket, InetAddress address, int port) {
        this.messageToSend = messageToSend;
        this.UDPSocket = UDPSocket;
        this.address = address;
        this.port = port;
    }

    public void run() {
        sendMessage(messageToSend);
    }

    private void sendMessage(String message) {
        try {
            byte[] data = message.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
            UDPSocket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}