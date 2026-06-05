import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class AjpSecretTester {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8009;

    private static void writeAjpString(DataOutputStream out, String value) throws IOException {
        if (value == null) {
            out.writeShort(0xFFFF);
            return;
        }

        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        out.writeShort(bytes.length);
        out.write(bytes);
        out.writeByte(0);
    }

    private static byte[] buildForwardRequest(String secret) throws IOException {

        ByteArrayOutputStream bodyBaos = new ByteArrayOutputStream();
        DataOutputStream body = new DataOutputStream(bodyBaos);

        // FORWARD_REQUEST
        body.writeByte(0x02);

        // GET
        body.writeByte(0x02);

        writeAjpString(body, "HTTP/1.1");
        writeAjpString(body, "/");
        writeAjpString(body, "127.0.0.1");
        writeAjpString(body, "localhost");
        writeAjpString(body, "localhost");

        body.writeShort(80);

        // is_ssl
        body.writeByte(0);

        // num_headers
        body.writeShort(0);

        // AJP attribute: secret
        if (secret != null) {
            body.writeByte(0x0C);
            writeAjpString(body, secret);
        }

        // terminator
        body.writeByte(0xFF);

        body.flush();

        byte[] payload = bodyBaos.toByteArray();

        ByteArrayOutputStream packetBaos = new ByteArrayOutputStream();
        DataOutputStream packet = new DataOutputStream(packetBaos);

        packet.writeByte(0x12);
        packet.writeByte(0x34);

        packet.writeShort(payload.length);

        packet.write(payload);

        packet.flush();

        return packetBaos.toByteArray();
    }

    private static String runTest(String secret) {

        try (Socket socket = new Socket(HOST, PORT)) {

            socket.setSoTimeout(5000);

            OutputStream os = socket.getOutputStream();
            InputStream is = socket.getInputStream();

            os.write(buildForwardRequest(secret));
            os.flush();

            int magic1 = is.read();
            int magic2 = is.read();

            if (magic1 == -1 || magic2 == -1) {
                return "NO RESPONSE";
            }

            if (magic1 != 0x41 || magic2 != 0x42) {
                return String.format(
                        "UNEXPECTED RESPONSE (%02X %02X)",
                        magic1,
                        magic2);
            }

            int len1 = is.read();
            int len2 = is.read();

            if (len1 < 0 || len2 < 0) {
                return "INVALID PACKET";
            }

            int length = (len1 << 8) | len2;

            byte[] response = new byte[length];

            int read = 0;

            while (read < length) {
                int r = is.read(response, read, length - read);
                if (r < 0) {
                    break;
                }
                read += r;
            }

            if (length > 0) {

                int prefix = response[0] & 0xFF;

                switch (prefix) {

                    case 0x04:
                        return "SEND_HEADERS (REQUEST ACCEPTED)";

                    case 0x03:
                        return "SEND_BODY_CHUNK";

                    case 0x05:
                        return "END_RESPONSE";

                    case 0x06:
                        return "GET_BODY_CHUNK";

                    default:
                        return String.format(
                                "AJP RESPONSE TYPE 0x%02X",
                                prefix);
                }
            }

            return "EMPTY RESPONSE";

        } catch (Exception e) {
            return "FAILED: " + e.getMessage();
        }
    }

    public static void main(String[] args) {

        System.out.println("=== TEST 1 : NO SECRET ===");
        System.out.println(runTest(null));

        System.out.println();

        System.out.println("=== TEST 2 : WRONG SECRET ===");
        System.out.println(runTest("SALAH_SECRET"));

        System.out.println();

        System.out.println("=== TEST 3 : CORRECT SECRET ===");
        System.out.println(runTest("Ajp2014!a"));
    }
}