package practice1;

public class PacketData {

    private int source;
    private long packetId;
    private int messageLength;
    private PacketMessage packetMessage;

    public PacketData(int source, long packetId, int messageLength, PacketMessage packetMessage) {
        this.source = source;
        this.packetId = packetId;
        this.messageLength = messageLength;
        this.packetMessage = packetMessage;
    }

    public int getSource() {
        return source;
    }

    public long getPacketId() {
        return packetId;
    }

    public int getMessageLength() {
        return messageLength;
    }

    public PacketMessage getPacketMessage() {
        return packetMessage;
    }

    @Override
    public String toString() {
        return "PacketData{" +
                "source=" + source +
                ", packetId=" + packetId +
                ", messageLength=" + messageLength +
                ", packetMessage=" + packetMessage +
                '}';
    }
}