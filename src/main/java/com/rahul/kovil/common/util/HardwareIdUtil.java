package com.rahul.kovil.common.util;
import java.security.MessageDigest;
import java.util.UUID;


public class HardwareIdUtil {

    public static String getHardwareId() {
        String os = System.getProperty("os.name").toLowerCase();

        try {
            if (os.contains("win")) {
                return hash(runCommand("wmic csproduct get uuid"));
            } else if (os.contains("linux")) {
                return hash(runCommand("cat /etc/machine-id"));
            } else if (os.contains("mac")) {
                return hash(runCommand("ioreg -l | awk '/IOPlatformUUID/ { print $4; }'"));
            }
        } catch (Exception ignored) {}

        // fallback (should not normally happen)
        return UUID.randomUUID().toString();
    }

    private static String runCommand(String cmd) throws Exception {
        Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", cmd});
        process.waitFor();
        return new String(process.getInputStream().readAllBytes()).trim();
    }

    private static String hash(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = digest.digest(input.getBytes());
        StringBuilder hexString = new StringBuilder();

        for (byte b : bytes)
            hexString.append(String.format("%02x", b));

        return hexString.toString();
    }
}
