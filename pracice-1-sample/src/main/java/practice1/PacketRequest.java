package practice1;

public class PacketRequest {

    private int source;
    private PacketMessage packetMessage;

    public PacketRequest(int source, PacketMessage packetMessage) {
        this.source = source;
        this.packetMessage = packetMessage;
    }

    public int getSource() {
        return source;
    }

    public PacketMessage getPacketMessage() {
        return packetMessage;
    }
}
