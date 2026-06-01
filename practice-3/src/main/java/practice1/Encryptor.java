package practice1;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Encryptor {

    private static final byte MAGIC = 0x13;

    private static final String KEY_TEXT = "1234567890abcdef";

    private long packetIdCounter = 1L;

    public byte[] enty_take(PacketRequest packetRequest) {

        int source = packetRequest.getSource();

        PacketMessage packetMessage = packetRequest.getPacketMessage();

        long packetId = packetIdCounter;
        packetIdCounter++;

        int commandType = packetMessage.getCommandType();
        int userId = packetMessage.getUserId();
        String entryStr = packetMessage.getMessage();

        byte[] messageTextBytes = entryStr.getBytes();

        byte[] plainMessage = createMessage(commandType, userId, messageTextBytes);

        byte[] encryptedMessage = encryptBytes(plainMessage);

        int messageLength = encryptedMessage.length;

        byte[] header = createHeader(source, packetId, messageLength);

        short headerCrc = Crc16.calculateCrc(header);
        short messageCrc = Crc16.calculateCrc(encryptedMessage);

        ByteBuffer byteBuffer = ByteBuffer.allocate(1 + 1 + 8 + 4 + 2 + messageLength + 2);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);

        byteBuffer.put((byte) 0x13);
        byteBuffer.put((byte) source);
        byteBuffer.putLong(packetId);
        byteBuffer.putInt(messageLength);
        byteBuffer.putShort(headerCrc);
        byteBuffer.put(encryptedMessage);
        byteBuffer.putShort(messageCrc);

        return byteBuffer.array();
    }

    private byte[] createMessage(int commandType, int userId, byte[] messageTextBytes) {
        ByteBuffer messageBuffer = ByteBuffer.allocate(4 + 4 + messageTextBytes.length);
        messageBuffer.order(ByteOrder.BIG_ENDIAN);

        messageBuffer.putInt(commandType);
        messageBuffer.putInt(userId);
        messageBuffer.put(messageTextBytes);

        return messageBuffer.array();
    }

    private byte[] createHeader(int source, long packetId, int messageLength) {
        ByteBuffer headerBuffer = ByteBuffer.allocate(1 + 1 + 8 + 4);
        headerBuffer.order(ByteOrder.BIG_ENDIAN);

        headerBuffer.put(MAGIC);
        headerBuffer.put((byte) source);
        headerBuffer.putLong(packetId);
        headerBuffer.putInt(messageLength);

        return headerBuffer.array();
    }

    private byte[] encryptBytes(byte[] data) {
        try {
            SecretKeySpec key = new SecretKeySpec(KEY_TEXT.getBytes(), "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException("Encryption error", e);
        }
    }

}