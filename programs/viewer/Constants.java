package viewer;

import java.util.*;
import viewer.object.*;

public interface Constants {
  // WorldModel
  static final WorldModel WORLD = new WorldModel();

  // IO
  static final int PACKET_SIZE = 1472;  // [byte] {cf. send_udp_size @ RUN/config.txt}
  static final int DEFAULT_KERNEL_PORT = 6000;
  //static final String ACTION_LOG_FILE_NAME = "tmp/action.log";//"action.log";

  // Viewer
  static final Viewer VIEWER = new Viewer();
  static final int VIEWER_DEFAULT_INTERVAL_EACH_CYCLE = 1000; // [m sec]
  static final int VIEWER_INTERVAL_EACH_EXPOSURE = 100; // [m sec]
  static final boolean VIEWER_DO_ANIMATION_AS_A_DEFAULT = true;
  static final int VIEWER_WIDTH  = 650;
  static final int VIEWER_HEIGHT = 550;
  static final int VIEWER_GAP = 50;
  static final int VIEWER_HUMANOID_SIZE = 12;
  static final int VIEWER_BLOCKADE_SIZE =  4;
  static final int VIEWER_NODE_SIZE     =  2;
  static final int VIEWER_ROAD_MAX_WIDTH = 2000; // [mm]
  static final int VIEWER_BACKGROUND_OF_ATTENTED_OBJECT_SIZE = 20;

  static final int SIMULATING_TIME = 300; // [cycle]
}
