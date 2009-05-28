package kernel;

import rescuecore2.connection.ConnectionManagerListener;
import rescuecore2.messages.Message;
import rescuecore2.messages.Command;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.WorldModel;

import java.util.Collection;

/**
   This class manages connections from simulators.
   @param <S> The subclass of WorldModel that this manager understands.
   @param <T> The subclass of Entity that this manager understands.
 */
public interface SimulatorManager<T extends Entity, S extends WorldModel<? super T>> extends ConnectionManagerListener, WorldModelAware<S> {
    /**
       Add a SimulatorManagerListener.
       @param l The listener to add.
    */
    void addSimulatorManagerListener(SimulatorManagerListener l);

    /**
       Remove a SimulatorManagerListener.
       @param l The listener to add.
    */
    void removeSimulatorManagerListener(SimulatorManagerListener l);

    /**
       Get all Simulators. This method may block if it needs to wait for simulators to connect.
       @throws InterruptedException If this thread is interrupted while waiting for simulators.
    */
    Collection<Simulator<T, S>> getAllSimulators() throws InterruptedException;

    /**
       Shut this manager down.
     */
    void shutdown();
}