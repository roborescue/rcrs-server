package rescuecore2.connection;

public final class ByteLogger {
    public static void log(byte[] bytes) {
        System.out.println(bytes.length + "-byte message");
        System.out.println("Offset    Hex         Int");
        for (int i = 0; i < bytes.length; i += 4) {
            System.out.printf("%1$-8d  0x%2$02x%3$02x%4$02x%5$02x  %2$3d %3$3d %4$3d %5$3d%n",
                              i,
                              bytes[i],
                              (i + 1) >= bytes.length ? 0 : bytes[i+1],
                              (i + 2) >= bytes.length ? 0 : bytes[i+2],
                              (i + 3) >= bytes.length ? 0 : bytes[i+3]);
        }
    }
}