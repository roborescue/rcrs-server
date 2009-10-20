package kernel.ui;

import java.util.ArrayList;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;

import kernel.KernelListener;
import kernel.AgentProxy;
import kernel.SimulatorProxy;
import kernel.ViewerProxy;
import kernel.Kernel;
import kernel.Timestep;

import rescuecore2.misc.gui.ListModelList;

/**
   A status panel for the kernel.
 */
public class KernelStatus extends JPanel implements KernelListener {
    private Kernel kernel;

    private ListModelList<AgentProxy> agents;
    private ListModelList<SimulatorProxy> simulators;
    private ListModelList<ViewerProxy> viewers;

    private JList agentsList;
    private JList simulatorsList;
    private JList viewersList;
    private JLabel timeLabel;

    /**
       Construct a KernelStatus component.
       @param kernel The Kernel to watch.
    */
    public KernelStatus(Kernel kernel) {
        super(new BorderLayout());
        this.kernel = kernel;
        agents = new ListModelList<AgentProxy>(new ArrayList<AgentProxy>());
        simulators = new ListModelList<SimulatorProxy>(new ArrayList<SimulatorProxy>());
        viewers = new ListModelList<ViewerProxy>(new ArrayList<ViewerProxy>());
        kernel.addKernelListener(this);
        agentsList = new JList(agents);
        simulatorsList = new JList(simulators);
        viewersList = new JList(viewers);
        // CHECKSTYLE:OFF:MagicNumber
        JPanel lists = new JPanel(new GridLayout(3, 1));
        // CHECKSTYLE:ON:MagicNumber
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
        timeLabel = new JLabel("Time: not started", JLabel.CENTER);
        add(timeLabel, BorderLayout.NORTH);
        agents.addAll(kernel.getAllAgents());
        simulators.addAll(kernel.getAllSimulators());
        viewers.addAll(kernel.getAllViewers());
    }

    @Override
    public void timestepCompleted(Timestep time) {
        timeLabel.setText("Time: " + time.getTime());
    }

    @Override
    public void agentAdded(AgentProxy info) {
        //        System.out.println("Agent added: " + info);
        agents.add(info);
    }

    @Override
    public void agentRemoved(AgentProxy info) {
        //        System.out.println("Agent removed: " + info);
        agents.remove(info);
    }

    @Override
    public void simulatorAdded(SimulatorProxy info) {
        //        System.out.println("Simulator added: " + info);
        simulators.add(info);
    }

    @Override
    public void simulatorRemoved(SimulatorProxy info) {
        //        System.out.println("Simulator removed: " + info);
        simulators.remove(info);
    }

    @Override
    public void viewerAdded(ViewerProxy info) {
        //        System.out.println("Viewer added: " + info);
        viewers.add(info);
    }

    @Override
    public void viewerRemoved(ViewerProxy info) {
        //        System.out.println("Viewer removed: " + info);
        viewers.remove(info);
    }
}