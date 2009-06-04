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
import kernel.Agent;
import kernel.Simulator;
import kernel.Viewer;
import kernel.Kernel;

/**
   A status panel for the kernel.
 */
public class KernelStatus extends JPanel implements KernelListener {
    private Kernel kernel;

    private ListModelList<Agent> agents;
    private ListModelList<Simulator> simulators;
    private ListModelList<Viewer> viewers;

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
        kernel.addKernelListener(this);
        agents = new ListModelList<Agent>(new ArrayList<Agent>());
        simulators = new ListModelList<Simulator>(new ArrayList<Simulator>());
        viewers = new ListModelList<Viewer>(new ArrayList<Viewer>());
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
    }

    @Override
    public void timestepCompleted(int time) {
        timeLabel.setText("Time: " + time);
    }

    @Override
    public void agentAdded(Agent info) {
        System.out.println("Agent added: " + info);
        agents.add(info);
    }

    @Override
    public void agentRemoved(Agent info) {
        System.out.println("Agent removed: " + info);
        agents.remove(info);
    }

    @Override
    public void simulatorAdded(Simulator info) {
        System.out.println("Simulator added: " + info);
        simulators.add(info);
    }

    @Override
    public void simulatorRemoved(Simulator info) {
        System.out.println("Simulator removed: " + info);
        simulators.remove(info);
    }

    @Override
    public void viewerAdded(Viewer info) {
        System.out.println("Viewer added: " + info);
        viewers.add(info);
    }

    @Override
    public void viewerRemoved(Viewer info) {
        System.out.println("Viewer removed: " + info);
        viewers.remove(info);
    }
}