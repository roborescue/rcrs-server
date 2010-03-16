package kernel.ui;

import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import java.awt.GridLayout;

import java.util.List;

import rescuecore2.misc.gui.ListModelList;

/**
   A user interface component for viewing that state of the ComponentManager.
 */
public class ComponentManagerGUI extends JPanel {
    private JList uncontrolledAgents;
    private JList agentAck;
    private JList simulatorAck;
    private JList viewerAck;
    private ListModelList<String> uncontrolledAgentsModel;
    private ListModelList<String> agentAckModel;
    private ListModelList<String> simulatorAckModel;
    private ListModelList<String> viewerAckModel;

    /**
       Construct a new ComponentManagerGUI.
     */
    public ComponentManagerGUI() {
        // CHECKSTYLE:OFF:MagicNumber
        super(new GridLayout(4, 1));
        // CHECKSTYLE:ON:MagicNumber
        uncontrolledAgentsModel = new ListModelList<String>();
        agentAckModel = new ListModelList<String>();
        simulatorAckModel = new ListModelList<String>();
        viewerAckModel = new ListModelList<String>();
        uncontrolledAgents = new JList(uncontrolledAgentsModel);
        agentAck = new JList(agentAckModel);
        simulatorAck = new JList(simulatorAckModel);
        viewerAck = new JList(viewerAckModel);
        add(uncontrolledAgents, "Agents with no controller");
        add(agentAck, "Agents that have not acknowledged");
        add(simulatorAck, "Simulators that have not acknowledged");
        add(viewerAck, "Viewers that have not acknowledged");
    }

    /**
       Update the list of uncontrolled agents.
       @param data A list of uncontrolled agent descriptions. This list will be displated verbatim.
     */
    public void updateUncontrolledAgents(final List<String> data) {
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    uncontrolledAgentsModel.clear();
                    uncontrolledAgentsModel.addAll(data);
                }
            });
    }

    /**
       Update the list of agents that have not acknowledged the connection.
       @param data A list of unacknowledged agent descriptions. This list will be displayed verbatim.
     */
    public void updateAgentAck(final List<String> data) {
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    agentAckModel.clear();
                    agentAckModel.addAll(data);
                }
            });
    }

    /**
       Update the list of simulators that have not acknowledged the connection.
       @param data A list of unacknowledged simulator descriptions. This list will be displayed verbatim.
     */
    public void updateSimulatorAck(final List<String> data) {
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    simulatorAckModel.clear();
                    simulatorAckModel.addAll(data);
                }
            });
    }

    /**
       Update the list of viewers that have not acknowledged the connection.
       @param data A list of unacknowledged viewer descriptions. This list will be displayed verbatim.
     */
    public void updateViewerAck(final List<String> data) {
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    viewerAckModel.clear();
                    viewerAckModel.addAll(data);
                }
            });
    }

    private void add(JList list, String title) {
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createTitledBorder(title));
        add(scroll);
    }
}
