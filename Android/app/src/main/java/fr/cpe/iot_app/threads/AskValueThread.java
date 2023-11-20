package fr.cpe.iot_app.threads;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class AskValueThread extends Thread {

    private static final int TEN_SECONDS_IN_MS = 10000;
    private static final String ASK_VALUE_CMD = "getValues()";

    private final int port;
    private final InetAddress address;
    private final DatagramSocket UDPSocket;

    public AskValueThread(DatagramSocket UDPSocket, InetAddress address, int port) {
        this.UDPSocket = UDPSocket;
        this.address = address;
        this.port = port;
    }

    public void run() {
        while (true) {
            try {
                sendMessage();
                Thread.sleep(TEN_SECONDS_IN_MS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessage() {
        try {
            byte[] data = ASK_VALUE_CMD.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
            UDPSocket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (UDPSocket != null && !UDPSocket.isClosed()) {
            UDPSocket.close();
        }
    }
}