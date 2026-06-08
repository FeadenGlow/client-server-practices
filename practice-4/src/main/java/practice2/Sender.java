package practice2;

import practice3.NetworkPacket;
import practice3.PacketSocketUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Sender implements Runnable {
    private final SharedQueue<NetworkPacket> packetsToSend;
    private final List<byte[]> sentMessages = Collections.synchronizedList(new ArrayList<>());

    public Sender(SharedQueue<NetworkPacket> packetsToSend) {
        this.packetsToSend = packetsToSend;
    }

    @Override
    public void run() {
        try {
            NetworkPacket packet;

            while ((packet = packetsToSend.take()) != null) {
                sendMessage(packet);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void sendMessage(NetworkPacket packet) {
        sentMessages.add(packet.getData());

        try {
            if (packet.isTcp()) {
                sendTcp(packet);
                return;
            }

            if (packet.isUdp()) {
                sendUdp(packet);
                return;
            }

            System.out.println("Sender: fake send, bytes=" + packet.getData().length);
        } catch (IOException e) {
            System.out.println("Sender: cannot send response: " + e.getMessage());
        }
    }

    private void sendTcp(NetworkPacket packet) throws IOException {
        PacketSocketUtil.writePacket(
                packet.getTcpSocket().getOutputStream(),
                packet.getData()
        );

        System.out.println("Sender: sent TCP response, bytes=" + packet.getData().length);
    }

    private void sendUdp(NetworkPacket packet) throws IOException {
        DatagramPacket responsePacket = new DatagramPacket(
                packet.getData(),
                packet.getData().length,
                packet.getUdpAddress(),
                packet.getUdpPort()
        );

        packet.getUdpSocket().send(responsePacket);

        System.out.println("Sender: sent UDP response, bytes=" + packet.getData().length);
    }

    public List<byte[]> getSentMessages() {
        synchronized (sentMessages) {
            return new ArrayList<>(sentMessages);
        }
    }
}