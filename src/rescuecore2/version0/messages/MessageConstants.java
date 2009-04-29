package rescuecore2.version0.messages;

/**
   Constants defining message IDs.
 */
public final class MessageConstants {
    /** Kernel-GIS connect. */
    public static final int KG_CONNECT = 0x10;
    /** Kernel-GIS acknowledge. */
    public static final int KG_ACKNOWLEDGE = 0x11;
    /** GIS-Kernel OK. */
    public static final int GK_CONNECT_OK = 0x12;
    /** GIS-Kernel error. */
    public static final int GK_CONNECT_ERROR = 0x13;

    /** Simulator-Kernel connect. */
    public static final int SK_CONNECT = 0x20;
    /** Simulator-Kernel acknowledge. */
    public static final int SK_ACKNOWLEDGE = 0x21;
    /** Kernel-Simulator OK. */
    public static final int KS_CONNECT_OK = 0x23;
    /** Kernel-Simulator error. */
    public static final int KS_CONNECT_ERROR = 0x24;

    /** Viewer-Kernel connect. */
    public static final int VK_CONNECT = 0x30;
    /** Viewer-Kernel acknowledge. */
    public static final int VK_ACKNOWLEDGE = 0x31;
    /** Kernel-Viewer OK. */
    public static final int KV_CONNECT_OK = 0x32;
    /** Kernel-Viewer error. */
    public static final int KV_CONNECT_ERROR = 0x33;

    /** Agent-Kernel connect. */
    public static final int AK_CONNECT = 0x40;
    /** Agent-Kernel acknowledge. */
    public static final int AK_ACKNOWLEDGE = 0x41;
    /** Kernel-Agent OK. */
    public static final int KA_CONNECT_OK = 0x42;
    /** Kernel-Agent error. */
    public static final int KA_CONNECT_ERROR = 0x43;

    private MessageConstants() {}
}