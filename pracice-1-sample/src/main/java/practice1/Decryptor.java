package practice1;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Decryptor {

    private static final byte MAGIC = 0x13;
    private static final String KEY_TEXT = "1234567890abcdef";

    public PacketData readPacket(byte[] packetBytes) {
        if (packetBytes.length < 18) {
            throw new IllegalArgumentException("Packet is too short");
        }

        ByteBuffer buffer = ByteBuffer.wrap(packetBytes);
        buffer.order(ByteOrder.BIG_ENDIAN);

        byte magic = buffer.get();

        if (magic != MAGIC) {
            throw new IllegalArgumentException("Wrong magic byte");
        }

        int source = buffer.get() & 0xFF;
        long packetId = buffer.getLong();
        int messageLength = buffer.getInt();

        int expectedLength = 1 + 1 + 8 + 4 + 2 + messageLength + 2;

        if (packetBytes.length != expectedLength) {
            throw new IllegalArgumentException("Wrong packet length");
        }

        short savedHeaderCrc = buffer.getShort();

        byte[] headerBytes = Arrays.copyOfRange(packetBytes, 0, 14);
        short calculatedHeaderCrc = Crc16.calculateCrc(headerBytes);

        if (savedHeaderCrc != calculatedHeaderCrc) {
            throw new IllegalArgumentException("Wrong header CRC");
        }

        byte[] encryptedMessage = new byte[messageLength];
        buffer.get(encryptedMessage);

        short savedMessageCrc = buffer.getShort();
        short calculatedMessageCrc = Crc16.calculateCrc(encryptedMessage);

        if (savedMessageCrc != calculatedMessageCrc) {
            throw new IllegalArgumentException("Wrong message CRC");
        }

        byte[] plainMessage = decryptBytes(encryptedMessage);

        PacketMessage message = readMessage(plainMessage);

        return new PacketData(source, packetId, messageLength, message);
    }

    private PacketMessage readMessage(byte[] plainMessage) {
        ByteBuffer messageBuffer = ByteBuffer.wrap(plainMessage);
        messageBuffer.order(ByteOrder.BIG_ENDIAN);

        int commandType = messageBuffer.getInt();
        int userId = messageBuffer.getInt();

        byte[] textBytes = new byte[plainMessage.length - 8];
        messageBuffer.get(textBytes);

        String text = new String(textBytes);

        return new PacketMessage(commandType, userId, text);
    }

    public byte[] decryptBytes(byte[] encryptedData) {
        try {
            SecretKeySpec key = new SecretKeySpec(KEY_TEXT.getBytes(), "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);

            return cipher.doFinal(encryptedData);
        } catch (Exception e) {
            throw new RuntimeException("Decryption error", e);
        }
    }
}