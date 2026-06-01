package practice3;

import practice1.PacketData;
import practice1.PacketRequest;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class NetworkPacket {
    private byte[] data;

    private Socket tcpSocket;

    private DatagramSocket udpSocket;
    private InetAddress udpAddress;
    private int udpPort;

    private PacketData packetData;
    private PacketRequest responseRequest;

    private NetworkPacket(byte[] data) {
        this.data = data;
    }

    public static NetworkPacket fake(byte[] data) {
        return new NetworkPacket(data);
    }

    public static NetworkPacket tcp(byte[] data, Socket socket) {
        NetworkPacket packet = new NetworkPacket(data);
        packet.tcpSocket = socket;

        return packet;
    }

    public static NetworkPacket udp(
            byte[] data,
            DatagramSocket socket,
            InetAddress address,
            int port
    ) {
        NetworkPacket packet = new NetworkPacket(data);
        packet.udpSocket = socket;
        packet.udpAddress = address;
        packet.udpPort = port;

        return packet;
    }

    public boolean isTcp() {
        return tcpSocket != null;
    }

    public boolean isUdp() {
        return udpSocket != null;
    }

    public String getClientKey() {
        if (isTcp()) {
            return "tcp";
        }

        if (isUdp()) {
            return udpAddress.getHostAddress() + ":" + udpPort;
        }

        return "fake";
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Socket getTcpSocket() {
        return tcpSocket;
    }

    public DatagramSocket getUdpSocket() {
        return udpSocket;
    }

    public InetAddress getUdpAddress() {
        return udpAddress;
    }

    public int getUdpPort() {
        return udpPort;
    }

    public PacketData getPacketData() {
        return packetData;
    }

    public void setPacketData(PacketData packetData) {
        this.packetData = packetData;
    }

    public PacketRequest getResponseRequest() {
        return responseRequest;
    }

    public void setResponseRequest(PacketRequest responseRequest) {
        this.responseRequest = responseRequest;
    }
}