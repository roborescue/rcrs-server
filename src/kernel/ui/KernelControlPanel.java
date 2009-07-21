package kernel.ui;

import static rescuecore2.misc.JavaTools.instantiate;

import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import kernel.Kernel;
import kernel.KernelException;
import kernel.ComponentManager;

import rescuecore2.misc.WorkerThread;
import rescuecore2.misc.Pair;
import rescuecore2.config.Config;
import rescuecore2.connection.Connection;
import rescuecore2.connection.StreamConnection;
import rescuecore2.connection.ConnectionException;
import rescuecore2.components.Component;
import rescuecore2.components.ComponentLauncher;

/**
   A JComponent containing various controls for the kernel GUI.
 */
public class KernelControlPanel extends JPanel {
    private Kernel kernel;
    private Config config;
    private ComponentManager componentManager;
    private ComponentLauncher launcher;
    private Collection<JButton> controlButtons;
    private JButton stepButton;
    private JButton runButton;

    private volatile boolean running;
    private volatile boolean step;
    private RunThread runThread;

    private final Object runLock = new Object();

    /**
       Create a KernelControlPanel component.
       @param kernel The kernel to control.
       @param config The kernel configuration.
       @param componentManager The kernel component manager.
    */
    public KernelControlPanel(Kernel kernel, Config config, ComponentManager componentManager) {
        super(new GridLayout(0, 1));
        this.kernel = kernel;
        this.config = config;
        this.componentManager = componentManager;
        controlButtons = new ArrayList<JButton>();
        JButton addAgent = new JButton("Add agent");
        JButton removeAgent = new JButton("Remove agent");
        JButton addSim = new JButton("Add simulator");
        JButton removeSim = new JButton("Remove simulator");
        JButton addViewer = new JButton("Add viewer");
        JButton removeViewer = new JButton("Remove viewer");
        stepButton = new JButton("Step");
        runButton = new JButton("Run");
        add(addAgent);
        add(removeAgent);
        add(addSim);
        add(removeSim);
        add(addViewer);
        add(removeViewer);
        add(stepButton);
        add(runButton);
        controlButtons.add(addAgent);
        controlButtons.add(removeAgent);
        controlButtons.add(addSim);
        controlButtons.add(removeSim);
        controlButtons.add(addViewer);
        controlButtons.add(removeViewer);
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
                    stepButtonPressed();
                }
            });
        runButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    runButtonPressed();
                }
            });
        runThread = new RunThread(config.getIntValue(Kernel.TIMESTEPS_KEY));
        running = false;
    }

    /**
       Activate this control panel.
    */
    public void activate() {
        runThread.start();
        Pair<Connection, Connection> connections = createConnectionPair();
        componentManager.newConnection(connections.first());
        launcher = new ComponentLauncher(connections.second());
    }

    private void addAgent() {
        Component[] as = createComponents("agents");
        addComponent(as, "agent");
    }

    private void removeAgent() {
    }

    private void addSim() {
        Component[] ss = createComponents("simulators");
        addComponent(ss, "simulator");
    }

    private void removeSim() {
    }

    private void addViewer() {
        Component[] vs = createComponents("viewers");
        addComponent(vs, "viewer");
    }

    private void removeViewer() {
    }

    private void addComponent(Component[] options, String type) {
        Component c = (Component)JOptionPane.showInputDialog(this, "Choose a " + type, "Choose a " + type, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (c == null) {
            return;
        }
        try {
            String result = launcher.connect(c);
            if (result != null) {
                System.out.println("Adding " + type + " failed: " + result);
                JOptionPane.showMessageDialog(this, "Adding " + type + " failed: " + result);
            }
            else {
                System.out.println(type + " added OK");
            }
        }
        catch (ConnectionException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Adding " + type + " failed: " + e);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void stepButtonPressed() {
        // This method should only be called on the event dispatch thread so it's OK to update the GUI.
        // Do a sanity check just in case.
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new RuntimeException("stepButtonPressed called by thread " + Thread.currentThread() + ", not the event dispatch thread.");
        }
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

    private void runButtonPressed() {
        // This method should only be called on the event dispatch thread so it's OK to update the GUI.
        // Do a sanity check just in case.
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new RuntimeException("runButtonPressed called by thread " + Thread.currentThread() + ", not the event dispatch thread.");
        }
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

    private void disableAllButtons() throws InterruptedException {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        setControlButtonsEnabled(false);
                        stepButton.setEnabled(false);
                        runButton.setEnabled(false);
                    }
                });
        }
        catch (java.lang.reflect.InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private boolean shouldStep() {
        synchronized (runLock) {
            return running || step;
        }
    }

    private Pair<Connection, Connection> createConnectionPair() {
        return StreamConnection.createConnectionPair();
    }

    private Component[] createComponents(String type) {
        List<String> classNames = config.getArrayValue("kernel.ui." + type);
        List<Component> instances = new ArrayList<Component>();
        for (String next : classNames) {
            System.out.println("Option found: '" + next + "'");
            Component c = instantiate(next, Component.class);
            if (c != null) {
                instances.add(c);
            }
        }
        return instances.toArray(new Component[0]);
    }

    private class RunThread extends WorkerThread {
        private int maxTime;

        public RunThread(int max) {
            maxTime = max;
        }

        @Override
        public boolean work() throws InterruptedException {
            if (shouldStep()) {
                if (kernel.getTime() < maxTime) {
                    try {
                        kernel.timestep();
                    }
                    catch (KernelException e) {
                        e.printStackTrace();
                        kernel.shutdown();
                        disableAllButtons();
                        return false;
                    }
                    synchronized (runLock) {
                        if (step) {
                            endStep();
                        }
                    }
                    return true;
                }
                else {
                    kernel.shutdown();
                    disableAllButtons();
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