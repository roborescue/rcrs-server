package rescuecore2.connection;

/**
   Utility class for logging raw bytes. Handy for debugging.
 */
public final class ByteLogger {
    private static final Object LOCK = new Object();

    /** Utility class: private constructor. */
    private ByteLogger() {}

    /**
       Log a byte array.
       @param bytes The bytes to log.
    */
    public static void log(byte[] bytes) {
        log(bytes, null);
    }

    /**
       Log a byte array.
       @param bytes The bytes to log.
       @param header A header to print out.
    */
    public static void log(byte[] bytes, String header) {
        synchronized (LOCK) {
            if (header != null) {
                System.err.println(header);
            }
            // CHECKSTYLE:OFF:MagicNumber
            System.err.println(bytes.length + "-byte message");
            System.err.println("Offset    Hex         Int");
            for (int i = 0; i < bytes.length; i += 4) {
                byte b1 = bytes[i];
                byte b2 = (i + 1) >= bytes.length ? 0 : bytes[i + 1];
                byte b3 = (i + 2) >= bytes.length ? 0 : bytes[i + 2];
                byte b4 = (i + 3) >= bytes.length ? 0 : bytes[i + 3];
                int v1 = (int)(bytes[i] & 0xFF);
                int v2 = (i + 1) >= bytes.length ? 0 : (int)(bytes[i + 1] & 0xFF);
                int v3 = (i + 2) >= bytes.length ? 0 : (int)(bytes[i + 2] & 0xFF);
                int v4 = (i + 3) >= bytes.length ? 0 : (int)(bytes[i + 3] & 0xFF);
                System.err.printf("%1$-8d  0x%2$02x%3$02x%4$02x%5$02x  %6$3d %7$3d %8$3d %9$3d%n",
                                  i, b1, b2, b3, b4,
                                  v1, v2, v3, v4);
            }
            // CHECKSTYLE:ON:MagicNumber
        }
    }
}
