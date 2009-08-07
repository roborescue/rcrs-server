package traffic3.manager.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.KeyStroke;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.JTextArea;

import static traffic3.log.Logger.log;

/**
 * Show log.
 */
public class ShowLogAction extends TrafficAction {

    /**
     * Constructor.
     */
    public ShowLogAction() {
        super("Log");
        putValue("MnemonicKey", KeyEvent.VK_L);
        putValue("ShortDescription", "See all the log information.");
        putValue("AcceleratorKey", KeyStroke.getKeyStroke(KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_DOWN_MASK));
    }

    /**
     * show log.
     * @param e event
     */
    public void actionPerformed(ActionEvent e) {
        log(">show log");
        //try {
            getWorldManagerGUI().requestFocus();
            final JFrame logFrame = new JFrame("Log");
            JTextArea ta = new JTextArea();
            KeyAdapter ka = new KeyAdapter() {
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode()  ==  KeyEvent.VK_ESCAPE) {
                            logFrame.dispose();
                        }
                    }
                };
            ta.setText(traffic3.log.Logger.getLogAsText());

            final Border border = BorderFactory.createEmptyBorder(10, 10, 10, 10);
            final Dimension preferredSize = new Dimension(600, 400);

            JPanel panel = new JPanel(new BorderLayout());
            panel.setFocusable(true);
            panel.setBorder(border);
            JScrollPane sp = new JScrollPane(ta);
            sp.setPreferredSize(preferredSize);
            JPanel controlPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            controlPane.add(new JButton(new AbstractAction("close") {
                    public void actionPerformed(ActionEvent e) {
                        logFrame.dispose();
                    }
                }));
            panel.add(sp, BorderLayout.CENTER);
            panel.add(controlPane, BorderLayout.SOUTH);
            logFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            logFrame.setContentPane(panel);
            logFrame.pack();
            logFrame.setLocationRelativeTo(getWorldManagerGUI());
            logFrame.setVisible(true);

            logFrame.addKeyListener(ka);
            ta.addKeyListener(ka);
            ta.requestFocus();
            ta.revalidate();
            /*
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
            */
    }
}