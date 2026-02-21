package com.robo4j.hw.rpi.imu;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.plugin.linuxfs.provider.i2c.LinuxFsI2CProviderImpl;

/**
 * Minimal I2C diagnostic for BNO08x. Sends a soft reset, drains post-reset
 * packets, then reads and prints SHTP packets from the device.
 */
public class Bno08xI2CDiag {

    // SHTP channels
    private static final String[] CHANNEL_NAMES = {
            "COMMAND", "EXECUTABLE", "CONTROL", "REPORTS", "WAKE_REPORTS", "GYRO"
    };

    public static void main(String[] args) throws Exception {
        Context ctx = Pi4J.newContextBuilder()
                .add(new LinuxFsI2CProviderImpl())
                .build();
        var config = I2C.newConfigBuilder(ctx)
                .id("bno08x-diag")
                .bus(1)
                .device(0x4B)
                .build();
        I2C i2c = ctx.i2c().create(config);

        System.out.println("=== BNO08x I2C Diagnostic @ 0x4B ===\n");

        // Step 1: Send soft reset
        // SHTP packet: length=5 (header+1 byte body), channel=1 (EXECUTABLE), seq=0, body=0x01 (reset)
        byte[] resetPacket = {0x05, 0x00, 0x01, 0x00, 0x01};
        System.out.println("Sending soft reset...");
        int written = i2c.write(resetPacket);
        System.out.printf("  write() returned: %d%n", written);

        // Step 2: Wait for device to complete reset
        System.out.println("Waiting 300ms for reset...");
        Thread.sleep(300);

        // Step 3: Drain all queued post-reset packets (advertisement, reset complete, etc.)
        System.out.println("\n=== Draining post-reset packets ===");
        for (int pktNum = 0; pktNum < 10; pktNum++) {
            byte[] header = new byte[4];
            i2c.read(header, 0, 4);
            int pktLen = (header[0] & 0xFF) | ((header[1] & 0x7F) << 8);
            int ch = header[2] & 0xFF;
            int seq = header[3] & 0xFF;

            if (pktLen == 0) {
                System.out.printf("Packet %d: empty (no more queued packets)%n", pktNum);
                break;
            }

            String chName = (ch < CHANNEL_NAMES.length) ? CHANNEL_NAMES[ch] : "UNKNOWN(" + ch + ")";
            System.out.printf("Packet %d: length=%d, channel=%d (%s), seq=%d%n",
                    pktNum, pktLen, ch, chName, seq);
            printHex("  header", header, 4);

            if (pktLen > 4) {
                int bodyLen = pktLen - 4;
                // BNO08x I2C quirk: second read prepends another 4-byte SHTP header
                byte[] raw = new byte[bodyLen + 4];
                i2c.read(raw, 0, raw.length);
                printHex("  repeated hdr", raw, 4);
                System.out.printf("  body (%d bytes): ", bodyLen);
                for (int i = 4; i < Math.min(4 + bodyLen, raw.length); i++) {
                    System.out.printf("0x%02x ", raw[i] & 0xFF);
                }
                System.out.println();
            }

            Thread.sleep(10);
        }

        // Step 4: Send Product ID request to verify communication
        // SHTP packet: length=6 (header+2 byte body), channel=2 (CONTROL), seq=0
        // body: [0xF9 (PRODUCT_ID_REQUEST), 0x00]
        System.out.println("\n=== Sending Product ID Request ===");
        byte[] prodIdReq = {0x06, 0x00, 0x02, 0x00, (byte) 0xF9, 0x00};
        written = i2c.write(prodIdReq);
        System.out.printf("  write() returned: %d%n", written);
        Thread.sleep(50);

        // Read response
        for (int attempt = 0; attempt < 5; attempt++) {
            byte[] header = new byte[4];
            i2c.read(header, 0, 4);
            int pktLen = (header[0] & 0xFF) | ((header[1] & 0x7F) << 8);
            int ch = header[2] & 0xFF;
            int seq = header[3] & 0xFF;

            if (pktLen == 0) {
                System.out.printf("  attempt %d: no data yet%n", attempt);
                Thread.sleep(20);
                continue;
            }

            String chName = (ch < CHANNEL_NAMES.length) ? CHANNEL_NAMES[ch] : "UNKNOWN(" + ch + ")";
            System.out.printf("  Response: length=%d, channel=%d (%s), seq=%d%n",
                    pktLen, ch, chName, seq);

            if (pktLen > 4) {
                int bodyLen = pktLen - 4;
                byte[] raw = new byte[bodyLen + 4];
                i2c.read(raw, 0, raw.length);
                byte[] body = new byte[bodyLen];
                System.arraycopy(raw, 4, body, 0, bodyLen);
                printHex("  body", body, Math.min(bodyLen, 32));

                // Parse Product ID response (report ID 0xF8)
                if (bodyLen >= 16 && (body[0] & 0xFF) == 0xF8) {
                    int swMajor = body[2] & 0xFF;
                    int swMinor = body[3] & 0xFF;
                    long swPatch = (body[12] & 0xFFL) | ((body[13] & 0xFFL) << 8)
                            | ((body[14] & 0xFFL) << 16) | ((body[15] & 0xFFL) << 24);
                    long swBuild = (body[8] & 0xFFL) | ((body[9] & 0xFFL) << 8)
                            | ((body[10] & 0xFFL) << 16) | ((body[11] & 0xFFL) << 24);
                    System.out.printf("  Product ID: SW version %d.%d.%d (build %d)%n",
                            swMajor, swMinor, swPatch, swBuild);
                }
            }
            break;
        }

        i2c.close();
        ctx.shutdown();
        System.out.println("\nDone.");
    }

    private static void printHex(String label, byte[] data, int len) {
        System.out.print(label + ": ");
        for (int i = 0; i < len; i++) {
            System.out.printf("0x%02x ", data[i] & 0xFF);
        }
        System.out.println();
    }
}
