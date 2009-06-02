package kernel.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.BorderFactory;

import kernel.KernelListener;
import kernel.Agent;
import kernel.Simulator;
import kernel.Viewer;
import kernel.Kernel;

import rescuecore2.worldmodel.Entity;
import rescuecore2.config.Config;
import rescuecore2.misc.WorkerThread;

/**
   A status panel for the kernel.
   @param <T> The subclass of Entity that this status object understands.
 */
public class KernelStatus<T extends Entity> extends JPanel implements KernelListener<T> {
    private Kernel<T> kernel;
    private Config config;

    private ListModelList<Agent<T>> agents;
    private ListModelList<Simulator<T>> simulators;
    private ListModelList<Viewer<T>> viewers;

    private JList agentsList;
    private JList simulatorsList;
    private JList viewersList;

    private JLabel timeLabel;

    private Collection<JButton> controlButtons;
    private JButton stepButton;
    private JButton runButton;

    private volatile boolean running;
    private volatile boolean step;
    private RunThread runThread;

    private final Object runLock = new Object();

    /**
       Construct a KernelStatus component.
       @param kernel The kernel.
       @param config The kernel configuration.
       @param allowControl Whether to allow the control buttons or not.
    */
    public KernelStatus(Kernel<T> kernel, Config config, boolean allowControl) {
        super(new BorderLayout());
        this.kernel = kernel;
        agents = new ListModelList<Agent<T>>(new ArrayList<Agent<T>>());
        simulators = new ListModelList<Simulator<T>>(new ArrayList<Simulator<T>>());
        viewers = new ListModelList<Viewer<T>>(new ArrayList<Viewer<T>>());
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
        controlButtons = new ArrayList<JButton>();
        if (allowControl) {
            JPanel buttons = new JPanel(new GridLayout(0, 1));
            JButton addAgent = new JButton("Add agent");
            JButton removeAgent = new JButton("Remove agent");
            JButton addSim = new JButton("Add simulator");
            JButton removeSim = new JButton("Remove simulator");
            JButton addViewer = new JButton("Add viewer");
            JButton removeViewer = new JButton("Remove viewer");
            stepButton = new JButton("Step");
            runButton = new JButton("Run");
            buttons.add(addAgent);
            buttons.add(removeAgent);
            buttons.add(addSim);
            buttons.add(removeSim);
            buttons.add(addViewer);
            buttons.add(removeViewer);
            buttons.add(stepButton);
            buttons.add(runButton);
            controlButtons.add(addAgent);
            controlButtons.add(removeAgent);
            controlButtons.add(addSim);
            controlButtons.add(removeSim);
            controlButtons.add(addViewer);
            controlButtons.add(removeViewer);
            add(buttons, BorderLayout.EAST);
            addAgent.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        addAgent();
                    }
                });
            removeAgent.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        removeAgent();
                    }
                });
            addSim.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        addSim();
                    }
                });
            removeSim.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        removeSim();
                    }
                });
            addViewer.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        addViewer();
                    }
                });
            removeViewer.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        removeViewer();
                    }
                });
            stepButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        step();
                    }
                });
            runButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        run();
                    }
                });
            runThread = new RunThread(config.getIntValue("timesteps"));
        }
        running = false;
    }

    /**
       Start the background UI thread.
     */
    public void start() {
        if (runThread != null) {
            runThread.start();
        }
    }


    @Override
    public void timestepCompleted(int time) {
        timeLabel.setText("Time: " + time);
    }

    @Override
    public void agentAdded(Agent<T> info) {
        System.out.println("Agent added: " + info);
        agents.add(info);
    }

    @Override
    public void agentRemoved(Agent<T> info) {
        System.out.println("Agent removed: " + info);
        agents.remove(info);
    }

    @Override
    public void simulatorAdded(Simulator<T> info) {
        System.out.println("Simulator added: " + info);
        simulators.add(info);
    }

    @Override
    public void simulatorRemoved(Simulator<T> info) {
        System.out.println("Simulator removed: " + info);
        simulators.remove(info);
    }

    @Override
    public void viewerAdded(Viewer<T> info) {
        System.out.println("Viewer added: " + info);
        viewers.add(info);
    }

    @Override
    public void viewerRemoved(Viewer<T> info) {
        System.out.println("Viewer removed: " + info);
        viewers.remove(info);
    }

    private void addAgent() {
    }
    private void removeAgent() {
    }
    private void addSim() {
    }
    private void removeSim() {
    }
    private void addViewer() {
    }
    private void removeViewer() {
    }

    private void step() {
        synchronized (runLock) {
            if (!running) {
                step = true;
                stepButton.setText("Working");
                stepButton.setEnabled(false);
                runButton.setEnabled(false);
                setControlButtonsEnabled(false);
                runLock.notifyAll();
            }
        }
    }

    private void endStep() {
        step = false;
        stepButton.setText("Step");
        stepButton.setEnabled(true);
        runButton.setEnabled(true);
        setControlButtonsEnabled(true);
    }

    private void run() {
        synchronized (runLock) {
            if (running) {
                setControlButtonsEnabled(true);
                stepButton.setEnabled(true);
                runButton.setText("Run");
            }
            else {
                setControlButtonsEnabled(false);
                stepButton.setEnabled(false);
                runButton.setText("Stop");
            }
            running = !running;
            runLock.notifyAll();
        }
    }

    private void setControlButtonsEnabled(boolean b) {
        for (JButton next : controlButtons) {
            next.setEnabled(b);
        }
    }

    private boolean shouldStep() {
        synchronized (runLock) {
            return running || step;
        }
    }

    private class RunThread extends WorkerThread {
        private int maxTime;

        public RunThread(int max) {
            maxTime = max;
        }

        @Override
        public boolean work() throws InterruptedException {
            if (shouldStep()) {
                if (kernel.getTime() <= maxTime) {
                    kernel.timestep();
                    synchronized (runLock) {
                        if (step) {
                            endStep();
                        }
                    }
                    return true;
                }
                else {
                    kernel.shutdown();
                    return false;
                }
            }
            else {
                synchronized (runLock) {
                    runLock.wait(1000);
                }
                return true;
            }
        }
    }
}