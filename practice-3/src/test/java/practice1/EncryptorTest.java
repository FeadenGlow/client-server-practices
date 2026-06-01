package practice1;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EncryptorTest {

    @Test
    public void shouldCreatePacketAsByteArray() {
        Encryptor encryptor = new Encryptor();

        PacketMessage message = new PacketMessage(1, 1, "hello");
        PacketRequest request = new PacketRequest(1, message);

        byte[] packet = encryptor.enty_take(request);

        assertThat(packet).isNotNull();
        assertThat(packet.length).isGreaterThan(0);
        assertThat(packet[0]).isEqualTo((byte) 0x13);
    }
}