package rescuecore2.messages.control;

/**
   URNs for control messages.
 */
public enum ControlMessageURN {
    /** Kernel-GIS connect. */
    KG_CONNECT,
    /** Kernel-GIS acknowledge. */
    KG_ACKNOWLEDGE,
    /** GIS-Kernel OK. */
    GK_CONNECT_OK,
    /** GIS-Kernel error. */
    GK_CONNECT_ERROR,

    /** Simulator-Kernel connect. */
    SK_CONNECT,
    /** Simulator-Kernel acknowledge. */
    SK_ACKNOWLEDGE,
    /** Simulator-Kernel update. */
    SK_UPDATE,
    /** Kernel-Simulator OK. */
    KS_CONNECT_OK,
    /** Kernel-Simulator error. */
    KS_CONNECT_ERROR,

    /** Viewer-Kernel connect. */
    VK_CONNECT,
    /** Viewer-Kernel acknowledge. */
    VK_ACKNOWLEDGE,
    /** Kernel-Viewer OK. */
    KV_CONNECT_OK,
    /** Kernel-Viewer error. */
    KV_CONNECT_ERROR,

    /** Agent-Kernel connect. */
    AK_CONNECT,
    /** Agent-Kernel acknowledge. */
    AK_ACKNOWLEDGE,
    /** Kernel-Agent OK. */
    KA_CONNECT_OK,
    /** Kernel-Agent error. */
    KA_CONNECT_ERROR,
    /** Kernel-Agent perception update. */
    KA_SENSE,

    /** Kernel update broadcast. */
    UPDATE,
    /** Kernel commands broadcast. */
    COMMANDS;
}