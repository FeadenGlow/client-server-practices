package practice3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PacketSocketUtil {
    private static final int MAX_PACKET_SIZE = 10_000;

    public static void writePacket(OutputStream outputStream, byte[] packet) throws IOException {
        DataOutputStream out = new DataOutputStream(outputStream);

        out.writeInt(packet.length);
        out.write(packet);
        out.flush();
    }

    public static byte[] readPacket(InputStream inputStream) throws IOException {
        DataInputStream in = new DataInputStream(inputStream);

        try {
            int length = in.readInt();

            if (length <= 0 || length > MAX_PACKET_SIZE) {
                throw new IOException("Wrong packet length: " + length);
            }

            byte[] packet = new byte[length];
            in.readFully(packet);

            return packet;
        } catch (EOFException e) {
            return null;
        }
    }
}