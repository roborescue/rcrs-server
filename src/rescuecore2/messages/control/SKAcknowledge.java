package rescuecore2.messages.control;

import rescuecore2.messages.Control;
import rescuecore2.messages.AbstractMessage;
import rescuecore2.messages.IntComponent;

/**
   A message for acknowleding a connection to the kernel.
 */
public class SKAcknowledge extends AbstractMessage implements Control {
    private IntComponent id;

    /**
       SKAcknowledge message with an undefined ID component.
     */
    public SKAcknowledge() {
        super("SK_ACKNOWLEDGE", ControlMessageConstants.SK_ACKNOWLEDGE);
        id = new IntComponent("ID");
        addMessageComponent(id);
    }

    /**
       SKAcknowledge message with a specific ID component.
       @param id The value of the ID component.
     */
    public SKAcknowledge(int id) {
        this();
        this.id.setValue(id);
    }

    /**
       Get the ID of the simulator that is acknowledging the connection.
       @return The simulator ID component.
     */
    public int getSimulatorID() {
        return id.getValue();
    }
}