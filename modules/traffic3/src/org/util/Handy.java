package org.util;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.Action;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.awt.Component;
import java.util.List;
import java.util.ArrayList;

import static traffic3.log.Logger.alert;

/**
 * handy class.
 */
public final class Handy {

    private static final int NATURAL_NUMBER_COMMA = 3;

    private Handy() {};

    /**
     * to natural string of a int value.
     * @param value value (12345)
     * @return string value of the value (12,345)
     */
    public static String toNaturalString(int value) {
        return java.text.NumberFormat.getInstance().format(value);
    }


    /**
     * input string.
     * This method block thread while user is inputting.
     * So this method must not be called from Event Dispatch Thread.
     * @param parent parent
     * @param message message
     * @return inputted value
     * @throws Exception called from Event Dispatch Thread
     */
    public static String inputString(final JComponent parent, final Object message) throws CannotStopEDTException {

        if (SwingUtilities.isEventDispatchThread()) {
            throw new CannotStopEDTException("input string method is called from Event Dispatch Thread!");
        }

        final String[] result = new String[1];
        SwingUtilities.invokeLater(new Runnable() { public void run() {
            final JFrame frame = new JFrame("Input");
            final JTextField tf = new JTextField();
            final JButton button = new JButton("OK");

            Action finishAction = new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        button.requestFocus();
                        result[0] = tf.getText();
                        frame.setVisible(false);
                        frame.dispose();
                        try {
                            synchronized (result) {
                                result.notifyAll();
                            }
                        }
                        catch (Exception exc) {
                            exc.printStackTrace();
                        }
                    }
                };
            tf.addActionListener(finishAction);

            JPanel buttonpanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            button.addActionListener(finishAction);
            buttonpanel.add(button);

            JComponent messagepanel = null;
            if (message instanceof JComponent) {
                messagepanel = (JComponent)message;
            }
            else {
                messagepanel = new JLabel(message.toString());
            }


            final Border border = BorderFactory.createEmptyBorder(10, 10, 10, 10);

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(messagepanel, BorderLayout.NORTH);
            panel.add(tf, BorderLayout.CENTER);
            panel.add(buttonpanel, BorderLayout.SOUTH);
            panel.setBorder(border);

            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.setContentPane(panel);
            frame.pack();
            frame.setLocationRelativeTo(parent);
            frame.setVisible(true);
            //result[0] = JOptionPane.showInputDialog(thisObject, message);
        } });
        try {
            synchronized (result) {
                result.wait();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return result[0];
    }

    /**
     * input double value.
     * @param parent parent
     * @param message message
     * @return inputted value
     * @throws Exception e
     */
    public static  double inputDouble(JComponent parent, Object message) throws CannotStopEDTException {
        return Double.parseDouble(inputString(parent, message));
    }

    /**
     * input int value.
     * @param parent parent
     * @param message message
     * @return inputted value
     * @throws Exception e
     */
    public static int inputInt(JComponent parent, Object message) throws CannotStopEDTException {
        return Integer.parseInt(inputString(parent, message));
    }

    /**
     * confirm yes or no (true/false).
     * @param parent parent
     * @param message message
     * @return yes or no
     * @throws Exception e
     */
    public static boolean confirm(JComponent parent, Object message) throws CannotStopEDTException {
        int result = JOptionPane.showConfirmDialog(parent, message, "confirm", JOptionPane.YES_NO_OPTION);
        return result == JOptionPane.YES_OPTION;
    }

    public static void show(Component parent, String title, Object... contents) {
        JPanel contentpane = new JPanel();
        contentpane.setLayout(new javax.swing.BoxLayout(contentpane, javax.swing.BoxLayout.Y_AXIS));
        for (Object o : contents) {
            if (o instanceof JComponent) {
                contentpane.add((JComponent)o);
            }
            else {
                javax.swing.JTextPane ta = new javax.swing.JTextPane();
                ta.setContentType("text/html");
                ta.setEditable(false);
                if (o instanceof Exception) {
                    Exception exc = (Exception)o;
                    String detail = getStackTraceText(exc);
                    StringBuffer sb = new StringBuffer();
                    sb.append("<html>");
                    sb.append("<div style='color:red;'>").append(exc.toString()).append("</div>");
                    sb.append("<div style='margin:0 0 0 20px;font-size:7px;color:red;'>");
                    sb.append(detail).append("</div>");
                    sb.append("</html>");
                    ta.setText(sb.toString());
                }
                else {
                    StringBuffer sb = new StringBuffer();
                    sb.append("<html>");
                    sb.append("<div style='font-size:8px;'>");
                    sb.append(o.toString());
                    sb.append("</div>");
                    sb.append("</html>");
                    ta.setText(sb.toString());
                }
                contentpane.add(new javax.swing.JScrollPane(ta));
            }
        }
        JButton closeButton = new JButton("close");
        JPanel controlpane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlpane.add(closeButton);
        controlpane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new java.awt.Dimension(500, 300));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        if (title != null) {
            JLabel label = new JLabel(title);
            label.setFont(label.getFont().deriveFont((float)30));
            panel.add(label, BorderLayout.NORTH);
        }
        panel.add(contentpane, BorderLayout.CENTER);
        panel.add(controlpane, BorderLayout.SOUTH);
        final JFrame frame = showFrame(parent, panel);
        closeButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    frame.dispose();
                    frame.setVisible(false);
                }
            });
    }

    public static String getStackTraceText(Exception exc) {
        java.io.StringWriter sw = new java.io.StringWriter();
        exc.printStackTrace(new java.io.PrintWriter(sw));
        return sw.toString();
    }

    public static InputStream getResourceAsStream(String path) throws IOException {
        //        URL url = getClass().getClassLoader().getResource("org/util/capture/ImageOutputTool.class");
        StringBuffer log = new StringBuffer();
        File f = new File(path);
        if (f.exists()) {
            return new FileInputStream(f);
        }
        log.append("cannot find: " + f.getAbsolutePath()).append("\n");
        URL url = ClassLoader.getSystemClassLoader().getResource(path);
        if (url != null) {
            return url.openStream();
        }
        log.append("cannot find: " + path).append("\n");
        url = ClassLoader.getSystemClassLoader().getResource("org/util/Handy.class");
        String rootFileName = url.toString();
        final String jarFileStartsWith = "jar:file:";
        final int start = jarFileStartsWith.length();
        final int end = rootFileName.indexOf("!");
        if (rootFileName.startsWith(jarFileStartsWith) && end != -1) {
            String jarfile = rootFileName.substring(start, end);
            File tfile = new File(jarfile);
            File file = new File(tfile.getParent(), path);
            System.err.println(file.getAbsolutePath());
            if (file.exists()) {
                return new FileInputStream(file);
            }
            log.append("cannot find: " + file.getAbsolutePath()).append("\n");
        }
        throw new IOException("cannot find resource: " + path + "\n log:" + log);
    }

    public static JFrame showFrame(JComponent comp) {
        return showFrame(null, comp);
    }

    public static JFrame showFrame(Component parent, JComponent comp) {
        JFrame frame = new JFrame();
        frame.setContentPane(comp);
        frame.pack();
        frame.setLocationRelativeTo(parent);
        frame.setVisible(true);
        return frame;
    }
}
