package org.util;

/**
 * In Java API, the EDT(Event Dispatch Thread) cannot be stop.
 * It means that alert window (such as JOptionPane) cannot be called from EDT.
 * A simple solution to call methods for alert window is that create a new Thread and call from the thread.
 */
public class CannotStopEDTException extends Exception {

    /**
     * Constructor.
     * @param message message
     */
    public CannotStopEDTException(String message) {
        super(message);
    }
}
