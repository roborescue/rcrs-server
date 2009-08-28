package rescuecore2.components;

import rescuecore2.connection.Connection;
import rescuecore2.connection.ConnectionException;
import rescuecore2.connection.ConnectionListener;
import rescuecore2.messages.Message;
import rescuecore2.messages.control.AKConnect;
import rescuecore2.messages.control.AKAcknowledge;
import rescuecore2.messages.control.KAConnectOK;
import rescuecore2.messages.control.KAConnectError;
import rescuecore2.messages.control.SKConnect;
import rescuecore2.messages.control.SKAcknowledge;
import rescuecore2.messages.control.KSConnectOK;
import rescuecore2.messages.control.KSConnectError;
import rescuecore2.messages.control.VKConnect;
import rescuecore2.messages.control.VKAcknowledge;
import rescuecore2.messages.control.KVConnectOK;
import rescuecore2.messages.control.KVConnectError;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.Queue;

/**
   A class that knows how to connect components to the kernel.
 */
public class ComponentLauncher {
    private static final String SUCCESS_MESSAGE = "success";

    private Connection connection;
    private int nextRequestID;

    /**
       Construct a new ComponentLauncher that will use a particular connection to connect components. The same connection will be re-used for all components.
       @param connection The Connection to use.
    */
    public ComponentLauncher(Connection connection) {
        this.connection = connection;
        nextRequestID = 1;
    }

    /**
       Connect a Component to the kernel. Throws a ComponentConnectionException if the connection fails due to a kernel ConnectError message.
       @param c The component to connect.
       @throws InterruptedException If the thread is interrupted before the connection attempt completes.
       @throws ConnectionException If there is a problem communicating with the kernel.
       @throws ComponentConnectionException If the connection fails.
    */
    public void connect(Component c) throws InterruptedException, ConnectionException, ComponentConnectionException {
        int requestID = getNextRequestID();
        BlockingQueue<String> q = new ArrayBlockingQueue<String>(1);
        if (c instanceof Agent) {
            connection.addConnectionListener(new AgentConnectionListener((Agent)c, requestID, q));
            connection.sendMessage(generateAgentConnect((Agent)c, requestID));
        }
        else if (c instanceof Simulator) {
            connection.addConnectionListener(new SimulatorConnectionListener((Simulator)c, requestID, q));
            connection.sendMessage(generateSimulatorConnect((Simulator)c, requestID));
        }
        else if (c instanceof Viewer) {
            connection.addConnectionListener(new ViewerConnectionListener((Viewer)c, requestID, q));
            connection.sendMessage(generateViewerConnect((Viewer)c, requestID));
        }
        else {
            throw new IllegalArgumentException("Don't know how to connect components of type " + c.getClass().getName());
        }
        String result = q.take();
        if (!SUCCESS_MESSAGE.equals(result)) {
            throw new ComponentConnectionException(result);
        }
    }

    private int getNextRequestID() {
        synchronized (this) {
            return nextRequestID++;
        }
    }

    private Message generateAgentConnect(Agent agent, int requestID) {
        return new AKConnect(requestID, 1, agent.getRequestedEntityIDs());
    }

    private Message generateSimulatorConnect(Simulator simulator, int requestID) {
        return new SKConnect(requestID, 1);
    }

    private Message generateViewerConnect(Viewer viewer, int requestID) {
        return new VKConnect(requestID, 1);
    }

    private static class AgentConnectionListener implements ConnectionListener {
        private Agent agent;
        private int requestID;
        private Queue<String> q;

        public AgentConnectionListener(Agent agent, int requestID, Queue<String> q) {
            this.agent = agent;
            this.requestID = requestID;
            this.q = q;
        }

        @Override
        public void messageReceived(Connection c, Message msg) {
            if (msg instanceof KAConnectOK) {
                handleConnectOK(c, (KAConnectOK)msg);
            }
            if (msg instanceof KAConnectError) {
                handleConnectError(c, (KAConnectError)msg);
            }
        }

        private void handleConnectOK(Connection c, KAConnectOK ok) {
            if (ok.getRequestID() == requestID) {
                c.removeConnectionListener(this);
                agent.postConnect(c, ok.getAgentID(), ok.getEntities());
                try {
                    c.sendMessage(new AKAcknowledge(requestID, ok.getAgentID()));
                    q.add(SUCCESS_MESSAGE);
                }
                catch (ConnectionException e) {
                    q.add(e.toString());
                }
            }
        }

        private void handleConnectError(Connection c, KAConnectError error) {
            if (error.getRequestID() == requestID) {
                c.removeConnectionListener(this);
                q.add(error.getReason());
            }
        }
    }

    private static class SimulatorConnectionListener implements ConnectionListener {
        private Simulator simulator;
        private int requestID;
        private Queue<String> q;

        public SimulatorConnectionListener(Simulator simulator, int requestID, Queue<String> q) {
            this.simulator = simulator;
            this.requestID = requestID;
            this.q = q;
        }

        @Override
        public void messageReceived(Connection c, Message msg) {
            if (msg instanceof KSConnectOK) {
                handleConnectOK(c, (KSConnectOK)msg);
            }
            if (msg instanceof KSConnectError) {
                handleConnectError(c, (KSConnectError)msg);
            }
        }

        private void handleConnectOK(Connection c, KSConnectOK ok) {
            if (ok.getRequestID() == requestID) {
                c.removeConnectionListener(this);
                simulator.postConnect(c, ok.getSimulatorID(), ok.getEntities());
                try {
                    c.sendMessage(new SKAcknowledge(requestID, ok.getSimulatorID()));
                    q.add(SUCCESS_MESSAGE);
                }
                catch (ConnectionException e) {
                    q.add(e.toString());
                }
            }
        }

        private void handleConnectError(Connection c, KSConnectError error) {
            if (error.getRequestID() == requestID) {
                c.removeConnectionListener(this);
                q.add(error.getReason());
            }
        }
    }

    private static class ViewerConnectionListener implements ConnectionListener {
        private Viewer viewer;
        private int requestID;
        private Queue<String> q;

        public ViewerConnectionListener(Viewer viewer, int requestID, Queue<String> q) {
            this.viewer = viewer;
            this.requestID = requestID;
            this.q = q;
        }

        @Override
        public void messageReceived(Connection c, Message msg) {
            if (msg instanceof KVConnectOK) {
                handleConnectOK(c, (KVConnectOK)msg);
            }
            if (msg instanceof KVConnectError) {
                handleConnectError(c, (KVConnectError)msg);
            }
        }

        private void handleConnectOK(Connection c, KVConnectOK ok) {
            if (ok.getRequestID() == requestID) {
                c.removeConnectionListener(this);
                viewer.postConnect(c, ok.getViewerID(), ok.getEntities());
                try {
                    c.sendMessage(new VKAcknowledge(requestID, ok.getViewerID()));
                    q.add(SUCCESS_MESSAGE);
                }
                catch (ConnectionException e) {
                    q.add(e.toString());
                }
            }
        }

        private void handleConnectError(Connection c, KVConnectError error) {
            if (error.getRequestID() == requestID) {
                c.removeConnectionListener(this);
                q.add(error.getReason());
            }
        }
    }
}