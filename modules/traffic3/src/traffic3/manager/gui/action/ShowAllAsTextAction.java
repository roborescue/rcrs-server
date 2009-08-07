package traffic3.manager.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.border.Border;

import static traffic3.log.Logger.log;
import traffic3.objects.TrafficObject;
import traffic3.objects.TrafficAgent;
import traffic3.objects.area.TrafficArea;
import traffic3.objects.area.TrafficAreaEdge;
import traffic3.objects.area.TrafficAreaNode;

/**
 * Show all the information about the world manager.
 */
public class ShowAllAsTextAction extends TrafficAction {

    /**
     * Constructor.
     */
    public ShowAllAsTextAction() {
        super("Show All as Text");
    }

    /**
     * Show all the information about the world manager.
     * @param e event
     */
    public void actionPerformed(ActionEvent e) {
        log(">show all as text");
        //try {
            final JFrame logFrame = new JFrame("Log");
            JTextPane ta = new JTextPane();
            ta.setContentType("text/html");
            KeyAdapter ka = new KeyAdapter() {
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode()  ==  KeyEvent.VK_ESCAPE) {
                            logFrame.dispose();
                        }
                    }
                };
            TrafficObject[] all = getWorldManagerGUI().getWorldManager().getAll();
            //.toArray(new TrafficObject[0]);

            TrafficAgent[] agentList = getWorldManagerGUI().getWorldManager().getAgentList();
            TrafficArea[] areaList = getWorldManagerGUI().getWorldManager().getAreaList();
            TrafficAreaEdge[] edges = getWorldManagerGUI().getWorldManager().getAreaConnectorEdgeList();
            TrafficAreaNode[] nodes = getWorldManagerGUI().getWorldManager().getAreaNodeList();
            com.infomatiq.jsi.rtree.RTree rtree = getWorldManagerGUI().getWorldManager().getRTree();

            StringBuffer sb = new StringBuffer();
            sb.append("<html>");
            sb.append("<div style='font-size:120%;'>Information</div>");
            sb.append("<table>");
            sb.append("<tr><td>Objects</td><td>").append(all.length).append("</td></tr>");
            sb.append("<tr><td>Agents</td><td>").append(agentList.length).append("</td></tr>");
            sb.append("<tr><td>Areas</td><td>").append(areaList.length).append("</td></tr>");
            sb.append("<tr><td>Edges</td><td>").append(edges.length).append("</td></tr>");
            sb.append("<tr><td>Nodes</td><td>").append(nodes.length).append("</td></tr>");
            sb.append("<tr><td>RTree</td><td>").append(rtree.size()).append("</td></tr>");
            sb.append("<table>");
            sb.append("</html>");
            ta.setText(sb.toString());

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