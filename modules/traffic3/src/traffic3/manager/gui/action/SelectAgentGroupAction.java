package traffic3.manager.gui.action;

import java.util.List;
import java.util.ArrayList;

import java.awt.event.ActionEvent;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JCheckBox;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;

import static traffic3.log.Logger.log;

import traffic3.objects.TrafficObject;
import traffic3.objects.TrafficAgent;


/**
 * Management Groups and select group. It is for target of "set destination action".
 * <ul>
 *  <li>create group
 *   <ul>
 *    <li>by agent's type</li>
 *    <li>by building which the agent is in.</li>
 *    <li>by selection which was clicked.</li>
 *   </ul>
 *  </li>
 *  <li>reset group</li>
 *  <li>set selected group</li>
 *  <li>create group</li>
 * </ul>
 */
public class SelectAgentGroupAction extends TrafficAction {

    /**
     *
     */
    public SelectAgentGroupAction() {
        super("Management Groups and Select Group");
    }

    /**
     * management gourps and select group.
     * @param e event
     */
    public void actionPerformed(ActionEvent e) {

        log(">select agents by group");
        //try {
            getWorldManagerGUI().requestFocus();
            final JFrame logFrame = new JFrame("Agent Group Manager");
            JComponent content = createGroupPanel();
            final JPanel panel = new JPanel(new BorderLayout());
            panel.setFocusable(true);
            final javax.swing.border.Border border = BorderFactory.createEmptyBorder(10, 10, 10, 10);
            panel.setBorder(border);
            final JPanel controlPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            controlPane.add(new JButton(new AbstractAction("reset") {
                    public void actionPerformed(ActionEvent e) {
                        getWorldManagerGUI().getAgentGroupList().clear();
                        getWorldManagerGUI().getSelectedAgentGroupList().clear();
                        panel.removeAll();
                        panel.add(createGroupPanel(), BorderLayout.CENTER);
                        panel.add(controlPane, BorderLayout.SOUTH);
                        panel.revalidate();
                    }
                }));
            controlPane.add(new JButton(new AbstractAction("create group") {
                    public void actionPerformed(ActionEvent e) {
                        String[] choice = new String[]{"type", "building", "selected agent"};
                        String selection = (String)JOptionPane.showInputDialog(getWorldManagerGUI(), "Select", "Select", JOptionPane.INFORMATION_MESSAGE, null, choice, choice[0]);
                        String saname = null;
                        if (selection.equals(choice[2])) {
                            saname = JOptionPane.showInputDialog(getWorldManagerGUI(), "input name");
                        }
                        for (TrafficAgent agent : getWorldManagerGUI().getWorldManager().getAgentList()) {
                            String name = null;

                            if (selection == choice[0]) {
                                name = agent.getType();
                            }
                            else if (selection == choice[1]) {
                                name = agent.getArea().getID();
                            }
                            else if (selection == choice[2]) {
                                for (TrafficObject o : getWorldManagerGUI().getTargetList().values()) {
                                    if (o  ==  agent) {
                                        name = saname;
                                    }
                                }
                                if (name == null) {
                                    continue;
                                }
                            }
                            else {
                                break;
                            }

                            List<TrafficAgent> tal = getWorldManagerGUI().getAgentGroupList().get(name);
                            if (tal == null) {
                                tal = new ArrayList<TrafficAgent>();
                                getWorldManagerGUI().getAgentGroupList().put(name, tal);
                            }
                            tal.add(agent);
                        }
                        panel.removeAll();
                        panel.add(createGroupPanel(), BorderLayout.CENTER);
                        panel.add(controlPane, BorderLayout.SOUTH);
                        panel.revalidate();
                    }
                }));
            controlPane.add(new JButton(new AbstractAction("close") {
                    public void actionPerformed(ActionEvent e) {
                        logFrame.dispose();
                    }
                }));
            panel.add(content, BorderLayout.CENTER);
            panel.add(controlPane, BorderLayout.SOUTH);
            logFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            logFrame.setContentPane(panel);
            logFrame.pack();
            logFrame.setLocationRelativeTo(getWorldManagerGUI());
            logFrame.setVisible(true);
          //    }
    //catch (RuntimeException exception) {
    //      exception.printStackTrace();
    //  }
    }

    /**
     * @return create group panel
     */
    public JComponent createGroupPanel() {
        JPanel content = new JPanel();
        String[] groupNameList = getWorldManagerGUI().getAgentGroupList().keySet().toArray(new String[0]);
        for (int i = 0; i < groupNameList.length; i++) {
            final String name = groupNameList[i];
            JCheckBox check = new JCheckBox(name);
            for (String tmp : getWorldManagerGUI().getSelectedAgentGroupList()) {
                if (tmp.equals(name)) {
                    check.setSelected(true);
                }
            }
            check.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JCheckBox tmpCheck = (JCheckBox)e.getSource();
                        if (tmpCheck.isSelected()) {
                            getWorldManagerGUI().getSelectedAgentGroupList().add(name);
                        }
                        else {
                            for (String tmp : getWorldManagerGUI().getSelectedAgentGroupList()) {
                                if (tmp.equals(name)) {
                                    getWorldManagerGUI().getSelectedAgentGroupList().remove(name);
                                }
                            }
                        }
                    }
                });
            content.add(check);
        }
        //JScrollPane sp = new JScrollPane(content);
        //sp.setPreferredSize(new Dimension(600, 400));
        final Dimension preferredSize = new Dimension(600, 400);
        content.setPreferredSize(preferredSize);

        return content;
    }
}