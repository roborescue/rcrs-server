package traffic3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Button;
import java.awt.Label;
import java.awt.Window;
import java.awt.Panel;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.TextArea;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.BorderFactory;
import traffic3.manager.WorldManager;
import traffic3.manager.gui.WorldManagerGUI;
import traffic3.log.event.LogListener;
import traffic3.log.LoggerException;
import traffic3.log.event.LogEvent;
import org.util.xml.io.XMLConfigManager;
import java.lang.reflect.InvocationTargetException;

/**
 *
 */
public final class Main {

    /**
     *
     */
    private static final String VERSION = "TrafficSimulator(3.0.17)";

    private static final int ALERT_DIALOG_SHOW_COUNT_LIMIT = 10;

    /**
     *
     */
    private static final String DEFAULT_CONFIG = ".traffic-simulator-config.xml";

    private Main() {}

    /**
     * Launch Point.
     * @param args args
     * @throws IOException exception
     */
    public static void main(String[] args) throws IOException {
        System.out.println("started");
        String configName = (args.length == 1 ? args[0] : DEFAULT_CONFIG);
        File configFile = new File(configName);
        System.out.println("config file: " + configFile.getAbsolutePath());
        final XMLConfigManager configManager = new XMLConfigManager(configFile);
        System.out.println("config: ");
        System.out.println(configManager);
        new Thread(new Runnable() {
                public void run() {
                    try {
                        start(configManager);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    catch (LaunchException e) {
                        e.printStackTrace();
                    }
                    catch (LoggerException e) {
                        e.printStackTrace();
                    }
                    catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }, "GUI Starting").start();
    }

    private static void start(final XMLConfigManager configManager) throws InterruptedException, LaunchException, LoggerException, InvocationTargetException {
        guiStart(configManager);
    }

    private static void cuiStart(final XMLConfigManager configManager) {
    }

    /**
     * start with gui.
     * @param configManager configuration
     */
    private static void guiStart(final XMLConfigManager configManager) throws InterruptedException, LaunchException, LoggerException, InvocationTargetException {
        final Exception[] exception = new Exception[1];
        final TextArea[] textarea = new TextArea[1];
        final Label progressbar = new Label();
        final boolean[] exit = new boolean[1];
        final Window[] window = new Window[1];
        final long[] timeStartEnd = new long[2];
        final WorldManagerGUI[] worldManagerGUI = new WorldManagerGUI[1];
        final WorldManager[] worldManager = new WorldManager[1];

        final int preferredFrameSizeWidth = 500;
        final int preferredFrameSizeHeight = 300;
        // create and show splash window
        Thread createSplashThread = new Thread(new Runnable() { public void run() {
            textarea[0] = new TextArea();
            textarea[0].setEditable(false);
            //window[0] = new Window((Frame)null);
            window[0] = new Frame();
            window[0].setSize(preferredFrameSizeWidth, preferredFrameSizeHeight);
            window[0].setLocationRelativeTo(null);
            window[0].setVisible(true);

            Button button = new Button("exit");
            button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        System.exit(1);
                    }
                });

            Panel tmp = new Panel(new FlowLayout());
            tmp.add(progressbar);
            tmp.add(button);

            window[0].setLayout(new BorderLayout());
            Label title = new Label("  " + getVersion() + "  ");
            final int fontSize = 25;
            title.setFont(new Font("Arial", Font.BOLD, fontSize));
            window[0].add(title, BorderLayout.NORTH);
            window[0].add(new Label("  "), BorderLayout.EAST);
            window[0].add(new Label("  "), BorderLayout.WEST);
            window[0].add(tmp, BorderLayout.SOUTH);
            //window[0].setVisible(true);
            window[0].add(textarea[0], BorderLayout.CENTER);
            window[0].setVisible(true);
        } });
        createSplashThread.start();
        createSplashThread.join();

        if (exception[0] != null) {
            throw new LaunchException(exception[0]);
        }

        worldManager[0] = new WorldManager();
        String launchMode = configManager.getValue("launch/mode", "plain");
        boolean isRCRSMode = "rcrs".equals(launchMode);
        String guiMode = configManager.getValue("launch/mode_rcrs/showGUI", "true");
        boolean isShowingGUIMode = !isRCRSMode || "true".equals(guiMode);
        String logEnable = configManager.getValue("log/enable", "true");
        String logType = configManager.getValue("log/type", "file");
        final String logFileName = configManager.getValue("log/filename", "traffic3.log");

        timeStartEnd[0] = System.currentTimeMillis();
        if (isShowingGUIMode) {
            // create and show main frame
            SwingUtilities.invokeAndWait(new Runnable() { public void run() { try {
                            textarea[0].append(getVersion() + "\n");
                            textarea[0].append("set system look and feel\n");
                            try {
                                UIManager.LookAndFeelInfo[] infos = UIManager.getInstalledLookAndFeels();
                                for (int i = 0; i < infos.length; i++) {
                                    System.out.println(infos[i]);
                                }
                                String selected = UIManager.getSystemLookAndFeelClassName();
                                //UIManager.setLookAndFeel(selected);
                            }
                            catch (Exception exc) {
                                textarea[0].append("cannot set system look and feel.\n" + exc.getMessage() + "\n");
                            }

                            final JFrame frame = new JFrame();
                            textarea[0].append("initialize logger\n");
                            File logFile = new File(logFileName);
                            textarea[0].append("log file:" + logFile.getAbsolutePath() + "\n");
                            traffic3.log.Logger.initialize(logFile);
                            final int[] alertDialogCounter = new int[]{0};
                            traffic3.log.Logger.addLogListener(new LogListener() {
                                    public void log(LogEvent e) {
                                        if (alertDialogCounter[0] > ALERT_DIALOG_SHOW_COUNT_LIMIT) {
                                            System.err.println("alert dialog error: dialog counter > 10: internal error:" + e.getMessage());
                                            return;
                                        }
                                        alertDialogCounter[0]++;

                                        Object message = e.getMessage();
                                        if (message instanceof Exception) {
                                            ((Exception)message).printStackTrace();
                                            org.util.Handy.show(frame, "Exception", message);
                                        }
                                        else {
                                            if (e.getType() == JOptionPane.ERROR_MESSAGE) {
                                                org.util.Handy.show(frame, "Error", message);
                                            }
                                            else {
                                                org.util.Handy.show(frame, "Information", message);
                                                //worldManagerGUI[0].setStatus(message.toString());
                                            }
                                        }
                                        if (alertDialogCounter[0] <= ALERT_DIALOG_SHOW_COUNT_LIMIT) {
                                            alertDialogCounter[0]--;
                                        }
                                    }
                                });
                            textarea[0].append("successed to initialize logger\n");
                            //textarea[0].append("cleate world manager\n");
                            textarea[0].append("cleate gui\n");
                            worldManagerGUI[0] = new WorldManagerGUI(worldManager[0], configManager);
                            worldManagerGUI[0].setBackground(Color.white);
                            JPanel contentpane = new JPanel(new BorderLayout());
                            contentpane.add(worldManagerGUI[0], BorderLayout.CENTER);
                            contentpane.add(worldManagerGUI[0].getStatusBar(), BorderLayout.SOUTH);

                            frame.setTitle(getVersion());
                            frame.setContentPane(contentpane);
                            frame.setJMenuBar(worldManagerGUI[0].createMenuBar());
                            frame.pack();
                            final int preferredWidth = 600;
                            final int preferredHeight = 400;
                            frame.setSize(preferredWidth, preferredHeight);
                            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                            frame.setLocationRelativeTo(null);
                            frame.setVisible(true);
                            window[0].toFront();
                        }
                        catch (Exception e) {
                            exception[0] = e;
                        }
            } });
        }
        else {
            textarea[0].append("initialize logger\n");
            File logFile = new File(logFileName);
            textarea[0].append("log file:" + logFile.getAbsolutePath() + "\n");
            traffic3.log.Logger.initialize(logFile);
            traffic3.log.Logger.addLogListener(new LogListener() {
                    public void log(LogEvent e) {
                        Object message = e.getMessage();
                        if (message instanceof Exception) {
                            System.err.println(message);
                            textarea[0].append(((Exception)message).getMessage());
                            textarea[0].append("\n");
                        }
                        else {
                            System.out.println(message);
                            textarea[0].append(message.toString());
                            textarea[0].append("\n");
                        }
                    }
                });
        }

        if (exception[0] != null) {
            final Color background = new Color(255, 200, 200);
            textarea[0].setBackground(background);
            textarea[0].append(getLog(exception[0]));
            throw new LaunchException(exception[0]);
        }
        // initialize all component
        // now launch process is successfully finished.
        // show infomation that  finished to launch.
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { try {
                        timeStartEnd[1] = System.currentTimeMillis();
                        textarea[0].append("launch successfull in " + (timeStartEnd[1] - timeStartEnd[0] + "[ms]\n"));
                    }
                    catch (Exception e) {
                        exception[0] = e;
                    }
        } });
        final int errorWindowWaitTime = 500;
        if (exception[0] != null) {
            throw new LaunchException(exception[0]);
        }
        try {
            Thread.sleep(errorWindowWaitTime);
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }

        final int splashWindowWaitTime = 2000;
        if (isShowingGUIMode) {
            // dispose splash window
            SwingUtilities.invokeAndWait(new Runnable() { public void run() {
                try {
                    timeStartEnd[1] = System.currentTimeMillis();
                    if ((timeStartEnd[1] - timeStartEnd[0]) < splashWindowWaitTime) {
                        try {
                            Thread.sleep(splashWindowWaitTime - (timeStartEnd[1] - timeStartEnd[0]));
                        }
                        catch (Exception exc) {
                            exc.printStackTrace();
                        }
                    }
                    if (window[0] != null) {
                        window[0].dispose();
                    }
                    traffic3.log.Logger.log("successfully launched.");
                }
                catch (Exception e) {
                    exception[0] = e;
                }
            }
                });
        }
        if (exception[0] != null) {
            throw new LaunchException(exception[0]);
        }

        if (isRCRSMode) {
            configManager.getDouble("/rcrs/traffic3/microStep", 100);
            final traffic3.simulator.RCRSTrafficSimulator rcrsSimulator = new traffic3.simulator.RCRSTrafficSimulator(worldManager[0], configManager);
            new Thread(new Runnable() { public void run() {
                try {
                    rcrsSimulator.start();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            } }, "rcrs thread").start();
        }
        else {
            try {
                File file = new File(configManager.getValue("launch/mode_plain/auto-import", "./data/auto-import.gml"));
                if (file.exists()) {
                    worldManagerGUI[0].open(file);
                }
            }
            catch (Exception e) {
                traffic3.log.Logger.alert(e, "error");
            }
        }
    }

    /**
     * get version as string.
     * @return version
     */
    public static String getVersion() {
        return VERSION;
    }

    /**
     * alert.
     * @param message message you want to show with GUI.
     */
    public static void alert(Object message) {
        System.out.println(message);
        // javax.swing.JOptionPane.showMessageDialog(null, message);
    }

    /**
     * input double value.
     * @param msg message
     * @param init initial value
     * @return inputed value
     */
    public static double inputValue(String msg, String init) {
      boolean continueflag = true;
      double tmpd = -1;
      while (continueflag) {
        try {
          tmpd = Double.parseDouble(JOptionPane.showInputDialog(msg, init));
          continueflag = false;
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
      return tmpd;
    }

    /**
     * Exception.printStackTrace to String.
     * @param exc exception
     * @return string
     */
    public static String getLog(Exception exc) {
      StringWriter sw = new StringWriter();
      exc.printStackTrace(new PrintWriter(sw));
      return sw.toString();
    }
}
