package practice1;

public class Utils {

    public static String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();

        for (byte b : bytes) {
            builder.append(String.format("%02X ", b));
        }

        return builder.toString().trim();
    }
}