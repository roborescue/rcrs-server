package human;

import rescuecore2.Constants;
import rescuecore2.misc.CommandLineOptions;
import rescuecore2.config.Config;
import rescuecore2.config.ConfigException;
import rescuecore2.connection.ConnectionException;
import rescuecore2.components.Agent;
import rescuecore2.components.ComponentLauncher;
import rescuecore2.components.TCPComponentLauncher;
import rescuecore2.components.ComponentConnectionException;
import rescuecore2.view.ViewComponent;
import rescuecore2.view.ViewListener;
import rescuecore2.view.RenderedObject;
import rescuecore2.messages.control.KVTimestep;
import rescuecore2.log.Logger;

import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.view.StandardWorldModelViewer;
import rescuecore2.standard.components.StandardViewer;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.AbstractListModel;

import java.util.List;
import java.util.ArrayList;

import java.io.IOException;

/**
   GUI for controlled agents.
 */
public class ControlledAgentGUI extends JPanel {
    private static final int VIEW_SIZE = 500;

    private List<ControlledFireBrigade> fbs;
    private ListListModel fbListModel;
    private JList fbList;
    private List<ControlledPoliceForce> pfs;
    private ListListModel pfListModel;
    private JList pfList;
    private List<ControlledAmbulanceTeam> ats;
    private ListListModel atListModel;
    private JList atList;

    /**
       Construct a ControlledAgentGUI.
       @param view The view of the world.
    */
    public ControlledAgentGUI(StandardWorldModelViewer view) {
        super(new BorderLayout());
        fbs = new ArrayList<ControlledFireBrigade>();
        fbListModel = new ListListModel(fbs);
        fbList = new JList(fbListModel);
        pfs = new ArrayList<ControlledPoliceForce>();
        pfListModel = new ListListModel(pfs);
        pfList = new JList(pfListModel);
        ats = new ArrayList<ControlledAmbulanceTeam>();
        atListModel = new ListListModel(ats);
        atList = new JList(atListModel);
        // CHECKSTYLE:OFF:MagicNumber
        JPanel agents = new JPanel(new GridLayout(3, 1));
        // CHECKSTYLE:ON:MagicNumber
        JScrollPane scroll = new JScrollPane(fbList);
        scroll.setBorder(BorderFactory.createTitledBorder("Fire brigades"));
        agents.add(scroll);
        scroll = new JScrollPane(pfList);
        scroll.setBorder(BorderFactory.createTitledBorder("Police forces"));
        agents.add(scroll);
        scroll = new JScrollPane(atList);
        scroll.setBorder(BorderFactory.createTitledBorder("Ambulance teams"));
        agents.add(scroll);
        add(agents, BorderLayout.WEST);
        add(view, BorderLayout.CENTER);
        view.addViewListener(new ViewListener() {
                @Override
                public void objectsClicked(ViewComponent view, List<RenderedObject> objects) {
                    handleClick(objects);
                }
                @Override
                public void objectsRollover(ViewComponent view, List<RenderedObject> objects) {
                }
            });
    }

    /**
       Entry point.
       @param args Command-line arguments.
    */
    public static void main(String[] args) {
        Config config = new Config();
        try {
            CommandLineOptions.processArgs(args, config);
        }
        catch (ConfigException e) {
            Logger.error("Configuration error", e);
            System.exit(-1);
        }
        catch (IOException e) {
            Logger.error("Configuration error", e);
            System.exit(-1);
        }
        StandardWorldModelViewer view = new StandardWorldModelViewer();
        view.setPreferredSize(new Dimension(VIEW_SIZE, VIEW_SIZE));
        ControlledAgentGUI gui = new ControlledAgentGUI(view);
        JFrame frame = new JFrame("Controlled agents");
        frame.add(gui);
        frame.pack();
        frame.setVisible(true);

        // Connect a viewer and agents
        int port = config.getIntValue(Constants.KERNEL_PORT_NUMBER_KEY, Constants.DEFAULT_KERNEL_PORT_NUMBER);
        String host = config.getValue(Constants.KERNEL_HOST_NAME_KEY, Constants.DEFAULT_KERNEL_HOST_NAME);
        ComponentLauncher launcher = new TCPComponentLauncher(host, port, config);
        ControlViewer viewer = new ControlViewer(view, gui);
        try {
            launcher.connect(viewer);
        }
        catch (InterruptedException e) {
            Logger.error("Interrupted", e);
            System.exit(-1);
        }
        catch (ConnectionException e) {
            Logger.error("Viewer connection failed", e);
            System.exit(-1);
        }
        catch (ComponentConnectionException e) {
            Logger.error("Viewer connection failed", e);
            System.exit(-1);
        }
        gui.launchAgents(launcher);
    }

    private void launchAgents(ComponentLauncher launcher) {
        // Connect as many fire brigades, police forces and ambulance teams as possible, but do it in a new thread.
        new AgentConnector(launcher).start();
    }

    private void refreshLists() {
        fbListModel.refresh();
        pfListModel.refresh();
        atListModel.refresh();
    }

    private void handleClick(List<RenderedObject> clicked) {
        handleFBClick(clicked);
        handlePFClick(clicked);
        handleATClick(clicked);
    }

    private void handleFBClick(List<RenderedObject> clicked) {
        for (RenderedObject next : clicked) {
            if (next.getObject() instanceof Building) {
                Building b = (Building)next.getObject();
                for (ControlledFireBrigade agent : getSelectedFireBrigades()) {
                    agent.setTarget(b);
                }
                break;
            }
        }
        fbListModel.refresh();
    }

    private void handlePFClick(List<RenderedObject> clicked) {
        for (RenderedObject next : clicked) {
            if (next.getObject() instanceof Road) {
                Road r = (Road)next.getObject();
                for (ControlledPoliceForce agent : getSelectedPoliceForces()) {
                    agent.setTarget(r);
                }
                break;
            }
        }
        pfListModel.refresh();
    }

    private void handleATClick(List<RenderedObject> clicked) {
        for (RenderedObject next : clicked) {
            if (next.getObject() instanceof Human) {
                Human h = (Human)next.getObject();
                for (ControlledAmbulanceTeam agent : getSelectedAmbulanceTeams()) {
                    agent.setTarget(h);
                }
                break;
            }
        }
        atListModel.refresh();
    }

    private List<ControlledFireBrigade> getSelectedFireBrigades() {
        int[] selected = fbList.getSelectedIndices();
        List<ControlledFireBrigade> agents = new ArrayList<ControlledFireBrigade>(selected.length);
        for (int next : selected) {
            agents.add(fbs.get(next));
        }
        return agents;
    }

    private List<ControlledPoliceForce> getSelectedPoliceForces() {
        int[] selected = pfList.getSelectedIndices();
        List<ControlledPoliceForce> agents = new ArrayList<ControlledPoliceForce>(selected.length);
        for (int next : selected) {
            agents.add(pfs.get(next));
        }
        return agents;
    }

    private List<ControlledAmbulanceTeam> getSelectedAmbulanceTeams() {
        int[] selected = atList.getSelectedIndices();
        List<ControlledAmbulanceTeam> agents = new ArrayList<ControlledAmbulanceTeam>(selected.length);
        for (int next : selected) {
            agents.add(ats.get(next));
        }
        return agents;
    }

    private static class ControlViewer extends StandardViewer {
        private StandardWorldModelViewer view;
        private ControlledAgentGUI gui;

        public ControlViewer(StandardWorldModelViewer view, ControlledAgentGUI gui) {
            this.view = view;
            this.gui = gui;
        }

        @Override
        protected void postConnect() {
            view.view(model);
        }

        @Override
        protected void handleTimestep(KVTimestep t) {
            super.handleTimestep(t);
            view.repaint();
            gui.refreshLists();
        }
    }

    private static class ListListModel extends AbstractListModel {
        private List<?> data;

        public ListListModel(List<?> data) {
            this.data = data;
        }

        @Override
        public int getSize() {
            return data.size();
        }

        @Override
        public Object getElementAt(int index) {
            return data.get(index);
        }

        public void refresh() {
            fireContentsChanged(this, 0, data.size());
        }
    }

    private class AgentConnector extends Thread {
        private ComponentLauncher launcher;

        public AgentConnector(ComponentLauncher launcher) {
            this.launcher = launcher;
        }

        @Override
        public void run() {
            connectAgents(new FireBrigadeAgentType(), fbs, fbListModel);
            connectAgents(new PoliceForceAgentType(), pfs, pfListModel);
            connectAgents(new AmbulanceTeamAgentType(), ats, atListModel);
        }

        private <T extends Agent> void connectAgents(AgentType<T> type, List<? super T> list, ListListModel model) {
            int count = 0;
            while (true) {
                ++count;
                T agent = type.createAgent();
                try {
                    launcher.connect(agent);
                    list.add(agent);
                }
                catch (ComponentConnectionException e) {
                    break;
                }
                catch (InterruptedException e) {
                    break;
                }
                catch (ConnectionException e) {
                    break;
                }
            }
            model.refresh();
        }
    }

    private interface AgentType<T extends Agent> {
        /**
           Create an Agent of the right type.
           @return A new Agent implementation.
        */
        T createAgent();
    }

    private static class FireBrigadeAgentType implements AgentType<ControlledFireBrigade> {
        @Override
        public ControlledFireBrigade createAgent() {
            return new ControlledFireBrigade();
        }

        @Override
        public String toString() {
            return "fire brigade";
        }
    }

    private static class PoliceForceAgentType implements AgentType<ControlledPoliceForce> {
        @Override
        public ControlledPoliceForce createAgent() {
            return new ControlledPoliceForce();
        }

        @Override
        public String toString() {
            return "police force";
        }
    }

    private static class AmbulanceTeamAgentType implements AgentType<ControlledAmbulanceTeam> {
        @Override
        public ControlledAmbulanceTeam createAgent() {
            return new ControlledAmbulanceTeam();
        }

        @Override
        public String toString() {
            return "ambulance team";
        }
    }
}
