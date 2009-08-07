package traffic3.log;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import java.io.File;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.StringWriter;
import java.io.PrintWriter;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeListener;
import java.text.DateFormat;
import traffic3.log.event.LogEvent;
import traffic3.log.event.LogListener;

/**
 *
 */
public final class Logger {

    private static StringBuffer logText = new StringBuffer();
    private static File logFile;
    private static BufferedWriter writer;
    private static boolean initialized;
    private static List<LogListener> changeListenerList = new ArrayList<LogListener>();
    private static Date date;
    private static DateFormat dateFormat;

    private Logger() {}

    /**
     * is initialize logger.
     * @return initialized
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * initialize.
     * @param f log file
     * @throws Exception exception
     */
    public static void initialize(File f) throws LoggerException {
        logFile = f;
        try {
            initialize(new BufferedWriter(new FileWriter(logFile)));
        }
        catch (IOException e) {
            throw new LoggerException(e.toString());
        }
    }

    /**
     * initialize.
     * @param w writer
     * @throws Exception exception
     */
    public static void initialize(BufferedWriter w) throws LoggerException {
        writer = w;
        if (initialized) {
            throw new LoggerException("already initialized");
        }
        if (writer == null) {
            throw new LoggerException("cannot initialize traffic3.log.Logger");
        }
        date = new Date();
        dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
        initialized = true;
        if (logFile == null) {
            log("logger is initialized: no log file.");
        }
        else {
            log("logger is initialized: " + logFile.getAbsolutePath());
        }
    }

    /**
     * log.
     * @param message message
     * @param type type
     */
    public static void log(Object message, int type) {
        if (initialized) {
            date.setTime(System.currentTimeMillis());

            try {
                StringBuffer sb = new StringBuffer();
                sb.append("[").append(dateFormat.format(date)).append("]");
                sb.append((type == JOptionPane.INFORMATION_MESSAGE ? "[o]" : "[e]"));
                if (message instanceof Exception) {
                    Exception exc = (Exception)message;
                    StringWriter sw = new StringWriter();
                    exc.printStackTrace(new PrintWriter(sw));
                    sb.append(sw.toString());
                }
                else {
                    if (message != null) {
                        sb.append(message.toString());
                    }
                    else {
                        sb.append("null");
                    }
                }
                writer.write(sb.toString());
                writer.newLine();
                writer.flush();
                logText.append(sb).append("\n");
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("traffic3.log.Logger is not initialized.: " + message);
        }
    }

    /**
     * get log as text.
     * @return log text
     */
    public static String getLogAsText() {
        return logText.toString();
    }

    /**
     * alert.
     * @param message message
     * @param type type
     */
    public static void alert(Object message, int type) {
        fireLog(message, type);
        log(message, type);
    }

    /**
     * add log listener.
     * @param listener listener
     */
    public static void addLogListener(LogListener listener) {
        changeListenerList.add(listener);
    }

    /**
     * remove log listener.
     * @param listener listener
     * @return success or not
     */
    public static boolean removeLogListener(ChangeListener listener) {
        return changeListenerList.remove(listener);
    }

    /**
     * firelog.
     * @param message message
     * @param type type
     */
    protected static void fireLog(Object message, int type) {
        final LogEvent e = new LogEvent(null, message, type);
        LogListener[] llist = changeListenerList.toArray(new LogListener[0]);

        for (int i = 0; i < llist.length; i++) {
            final LogListener ll = llist[i];
            new Thread(new Runnable() {
                    public void run() {
                        ll.log(e);
                    }
                }, "Notify log event.").start();
        }
    }

    /**
     * alert.
     * @param message message
     */
    public static void alert(Object message) {
        alert(message, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * alert.
     * @param message message
     * @param type information or error (i/e)
     */
    public static void alert(Object message, String type) {
        alert(message, (type.startsWith("i") ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE));
    }

    /**
     * log.
     * @param message message
     */
    public static void log(Object message) {
        log(message, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * alert.
     * @param message message
     * @param type information or error (i/e)
     */
   public static void log(Object message, String type) {
        log(message, (type.startsWith("i") ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE));
    }
}
