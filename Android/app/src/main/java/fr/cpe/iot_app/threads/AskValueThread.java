package fr.cpe.iot_app.threads;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class AskValueThread extends Thread {

    private final int port;
    private final InetAddress address;
    private final DatagramSocket UDPSocket;

    public AskValueThread(DatagramSocket UDPSocket, InetAddress address, int port) {
        this.UDPSocket = UDPSocket;
        this.address = address;
        this.port = port;
    }

    public void run() {
        while(true) {
            try {
                sendMessage();
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessage() {
        try {
            byte[] data = "getValues()".getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
            UDPSocket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}