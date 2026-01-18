import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.UUID;


public class Lice {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
        System.out.println(HardwareIdUtil.getHardwareId());
    }
}


class HardwareIdUtil {

    private static volatile String CACHED_HWID;

    private HardwareIdUtil() {}

    public static String getHardwareId() {
        if (CACHED_HWID != null) {
            return CACHED_HWID;
        }

        synchronized (HardwareIdUtil.class) {
            if (CACHED_HWID != null) {
                return CACHED_HWID;
            }

            try {
                String rawId;
                String os = System.getProperty("os.name").toLowerCase();

                if (os.contains("win")) {
                    rawId = getWindowsMachineGuid();
                } else if (os.contains("linux")) {
                    rawId = Files.readString(Path.of("/etc/machine-id")).trim();
                } else if (os.contains("mac")) {
                    rawId = getMacPlatformUUID();
                } else {
                    rawId = UUID.randomUUID().toString();
                }

                CACHED_HWID = sha256(rawId);
                return CACHED_HWID;

            } catch (Exception e) {
                // stable fallback for this runtime
                try{
                    CACHED_HWID = sha256(UUID.randomUUID().toString());
                } catch (Exception ex){
                    CACHED_HWID = UUID.randomUUID().toString();
                }
                return CACHED_HWID;
            }
        }
    }

    /* ---------------- OS SPECIFIC ---------------- */

    private static String getWindowsMachineGuid() throws Exception {
        Process process = new ProcessBuilder(
                "reg",
                "query",
                "HKLM\\SOFTWARE\\Microsoft\\Cryptography",
                "/v",
                "MachineGuid"
        ).redirectErrorStream(true).start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("MachineGuid")) {
                    return line.substring(line.lastIndexOf(" ") + 1).trim();
                }
            }
        }

        throw new IllegalStateException("MachineGuid not found");
    }

    private static String getMacPlatformUUID() throws Exception {
        Process process = new ProcessBuilder(
                "ioreg", "-rd1", "-c", "IOPlatformExpertDevice"
        ).redirectErrorStream(true).start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("IOPlatformUUID")) {
                    return line.split("\"")[3];
                }
            }
        }

        throw new IllegalStateException("IOPlatformUUID not found");
    }

    /* ---------------- HASH ---------------- */

    private static String sha256(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes());
        StringBuilder hex = new StringBuilder(hash.length * 2);

        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }

        return hex.toString();
    }
}
