package viewer;

public interface HeaderConstants {
  static final int HEADER_NULL      = 0x00;
  static final int AK_CONNECT       = 0x10;
  static final int AK_ACKNOWLEDGE   = 0x11;

  static final int SK_CONNECT       = 0x20;
  static final int SK_ACKNOWLEDGE   = 0x21;
  static final int SK_UPDATE        = 0x22;

  static final int KA_CONNECT_OK    = 0x50;
  static final int KA_CONNECT_ERROR = 0x51;
  static final int KA_SENSE         = 0x52;
  static final int KA_HEAR          = 0x53;

  static final int KS_CONNECT_OK    = 0x60;
  static final int KS_CONNECT_ERROR = 0x61;
  static final int KS_COMMANDS      = 0x62;
  static final int KS_UPDATE        = 0x63;

  static final int AK_REST       = 0x80;
  static final int AK_MOVE       = 0x81;
  static final int AK_LOAD       = 0x82;
  static final int AK_UNLOAD     = 0x83;
  static final int AK_SAY        = 0x84;
  static final int AK_TELL       = 0x85;
  static final int AK_EXTINGUISH = 0x86;
  static final int AK_STRETCH    = 0x87;
  static final int AK_RESCUE     = 0x88;
  static final int AK_CLEAR      = 0x89;
}
