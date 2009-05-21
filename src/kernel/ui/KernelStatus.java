package kernel.ui;

import java.util.List;
import java.util.ArrayList;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;

import kernel.KernelListener;
import kernel.AgentManagerListener;
import kernel.SimulatorManagerListener;
import kernel.ViewerManagerListener;
import kernel.AgentInfo;
import kernel.SimulatorInfo;
import kernel.ViewerInfo;

/**
   A status panel for the kernel.
 */
public class KernelStatus extends JPanel implements KernelListener, AgentManagerListener, SimulatorManagerListener, ViewerManagerListener {
    private ListModelList<AgentInfo> agents;
    private ListModelList<SimulatorInfo> simulators;
    private ListModelList<ViewerInfo> viewers;

    private JList agentsList;
    private JList simulatorsList;
    private JList viewersList;

    public KernelStatus() {
        super(new BorderLayout());
        agents = new ListModelList<AgentInfo>(new ArrayList<AgentInfo>());
        simulators = new ListModelList<SimulatorInfo>(new ArrayList<SimulatorInfo>());
        viewers = new ListModelList<ViewerInfo>(new ArrayList<ViewerInfo>());
        agentsList = new JList(agents);
        simulatorsList = new JList(simulators);
        viewersList = new JList(viewers);
        JPanel lists = new JPanel(new GridLayout(3,1));
        JScrollPane agentsScroll = new JScrollPane(agentsList);
        JScrollPane simulatorsScroll = new JScrollPane(simulatorsList);
        JScrollPane viewersScroll = new JScrollPane(viewersList);
        agentsScroll.setBorder(BorderFactory.createTitledBorder("Agents"));
        simulatorsScroll.setBorder(BorderFactory.createTitledBorder("Simulators"));
        viewersScroll.setBorder(BorderFactory.createTitledBorder("Viewers"));
        lists.add(agentsScroll);
        lists.add(simulatorsScroll);
        lists.add(viewersScroll);
        add(lists, BorderLayout.CENTER);
    }

    @Override
    public void worldModelComplete() {
    }

    @Override
    public void timestep(int time) {
    }

    @Override
    public void agentConnected(AgentInfo info) {
        System.out.println("Agent connected: " + info);
        agents.add(info);
    }

    @Override
    public void simulatorConnected(SimulatorInfo info) {
        System.out.println("Simulator connected: " + info);
        simulators.add(info);
    }

    @Override
    public void viewerConnected(ViewerInfo info) {
        System.out.println("Viewer connected: " + info);
        viewers.add(info);
    }
}