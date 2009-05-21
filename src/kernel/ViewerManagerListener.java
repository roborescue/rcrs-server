package kernel;

/**
   Interface for objects interested in ViewerManager events.
 */
public interface ViewerManagerListener {
    /**
       Notification that a viewer has connected.
       @param info Information about the viewer.
    */
    void viewerConnected(ViewerInfo info);
}