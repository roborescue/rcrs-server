package rescuecore2.version0.entities;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.properties.IntArrayProperty;
import rescuecore2.worldmodel.properties.BooleanProperty;

/**
   The Node object.
 */
public class Node extends Vertex {
    private BooleanProperty signal;
    private IntArrayProperty shortcut;
    private IntArrayProperty pocket;
    private IntArrayProperty timing;

    /**
       Construct a Node object with entirely undefined values.
       @param id The ID of this entity.
     */
    public Node(EntityID id) {
        super(id, RescueEntityType.NODE);
        signal = new BooleanProperty(RescuePropertyType.SIGNAL);
        shortcut = new IntArrayProperty(RescuePropertyType.SHORTCUT_TO_TURN);
        pocket = new IntArrayProperty(RescuePropertyType.POCKET_TO_TURN_ACROSS);
        timing = new IntArrayProperty(RescuePropertyType.SIGNAL_TIMING);
        addProperties(signal, shortcut, pocket, timing);
    }

    @Override
    protected Entity copyImpl() {
        return new Node(getID());
    }

    /**
       Get the signal property.
       @return The signal property.
     */
    public BooleanProperty getSignalProperty() {
        return signal;
    }

    /**
       Find out if this node has traffic signals.
       @return Whether this node has traffic signals.
     */
    public boolean hasSignal() {
        return signal.getValue();
    }

    /**
       Set whether this node has traffic signals.
       @param newSignal The new signal value.
    */
    public void setHasSignals(boolean newSignal) {
        this.signal.setValue(newSignal);
    }

    /**
       Find out if the signal property has been defined.
       @return True if the signal property has been defined, false otherwise.
     */
    public boolean isSignalDefined() {
        return signal.isDefined();
    }

    /**
       Undefine the signal property.
    */
    public void undefineSignal() {
        signal.undefine();
    }

    /**
       Get the shortcut to turn property.
       @return The shortcut to turn property.
     */
    public IntArrayProperty getShortcutToTurnProperty() {
        return shortcut;
    }

    /**
       Get the shortcut to turn property value.
       @return The shortcut to turn property value.
     */
    public int[] getShortcutToTurn() {
        return shortcut.getValue();
    }

    /**
       Set the shortcut to turn property value.
       @param newShortcut to turn The new shortcut to turn.
    */
    public void setShortcutToTurn(int[] newShortcut) {
        this.shortcut.setValue(newShortcut);
    }

    /**
       Find out if the shortcut to turn property has been defined.
       @return True if the shortcut to turn property has been defined, false otherwise.
     */
    public boolean isShortcutToTurnDefined() {
        return shortcut.isDefined();
    }

    /**
       Undefine the shortcut to turn property.
    */
    public void undefineShortcutToTurn() {
        shortcut.undefine();
    }

    /**
       Get the pocketToTurnAcross property.
       @return The pocketToTurnAcross property.
     */
    public IntArrayProperty getPocketToTurnAcrossProperty() {
        return pocket;
    }

    /**
       Get the pocketToTurnAcross property value.
       @return The pocketToTurnAcross property value.
     */
    public int[] getPocketToTurnAcross() {
        return pocket.getValue();
    }

    /**
       Set the pocketToTurnAcross property value.
       @param pocketToTurnAcross The new pocketToTurnAcross value.
    */
    public void setPocketToTurnAcross(int[] pocketToTurnAcross) {
        this.pocket.setValue(pocketToTurnAcross);
    }

    /**
       Find out if the pocketToTurnAcross property has been defined.
       @return True if the pocketToTurnAcross property has been defined, false otherwise.
     */
    public boolean isPocketToTurnAcrossDefined() {
        return pocket.isDefined();
    }

    /**
       Undefine the pocketToTurnAcross property.
    */
    public void undefinePocketToTurnAcross() {
        pocket.undefine();
    }

    /**
       Get the signalTiming property.
       @return The signalTiming property.
     */
    public IntArrayProperty getSignalTimingProperty() {
        return timing;
    }

    /**
       Get the signalTiming property value.
       @return The signalTiming property value.
     */
    public int[] getSignalTiming() {
        return timing.getValue();
    }

    /**
       Set the signalTiming property value.
       @param signalTiming The new signalTiming.
    */
    public void setSignalTiming(int[] signalTiming) {
        this.timing.setValue(signalTiming);
    }

    /**
       Find out if the signalTiming property has been defined.
       @return True if the signalTiming property has been defined, false otherwise.
     */
    public boolean isSignalTimingDefined() {
        return timing.isDefined();
    }

    /**
       Undefine the signalTiming property.
    */
    public void undefineSignalTiming() {
        timing.undefine();
    }
}