package kernel.ui;

import java.util.ArrayList;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;

import kernel.KernelListener;
import kernel.AgentProxy;
import kernel.SimulatorProxy;
import kernel.ViewerProxy;
import kernel.Kernel;

import rescuecore2.misc.gui.ListModelList;
import rescuecore2.Timestep;

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
    private JLabel scoreLabel;

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
        scoreLabel = new JLabel("Score: not started", JLabel.CENTER);
        JPanel labels = new JPanel(new GridLayout(1, 2));
        labels.add(timeLabel);
        labels.add(scoreLabel);
        add(labels, BorderLayout.NORTH);
        agents.addAll(kernel.getAllAgents());
        simulators.addAll(kernel.getAllSimulators());
        viewers.addAll(kernel.getAllViewers());
    }

    @Override
    public void simulationStarted(Kernel k) {
    }

    @Override
    public void simulationEnded(Kernel k) {
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    timeLabel.setText("Time: ended");
                }
            });
    }

    @Override
    public void timestepCompleted(Kernel k, final Timestep time) {
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    timeLabel.setText("Time: " + time.getTime());
                    scoreLabel.setText("Score: " + time.getScore());
                }
            });
    }

    @Override
    public void agentAdded(Kernel k, AgentProxy info) {
        agents.add(info);
    }

    @Override
    public void agentRemoved(Kernel k, AgentProxy info) {
        agents.remove(info);
    }

    @Override
    public void simulatorAdded(Kernel k, SimulatorProxy info) {
        simulators.add(info);
    }

    @Override
    public void simulatorRemoved(Kernel k, SimulatorProxy info) {
        simulators.remove(info);
    }

    @Override
    public void viewerAdded(Kernel k, ViewerProxy info) {
        viewers.add(info);
    }

    @Override
    public void viewerRemoved(Kernel k, ViewerProxy info) {
        viewers.remove(info);
    }
}
