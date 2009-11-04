package rescuecore2.messages.components;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.messages.AbstractMessageComponent;

/**
   An ChangeSet component to a message.
 */
public class ChangeSetComponent extends AbstractMessageComponent {
    private ChangeSet changes;

    /**
       Construct a ChangeSetComponent with no content.
       @param name The name of the component.
    */
    public ChangeSetComponent(String name) {
        super(name);
        changes = new ChangeSet();
    }

    /**
       Construct a ChangeSetComponent with a specific set of changes.
       @param name The name of the component.
       @param changes The changes in this message component.
    */
    public ChangeSetComponent(String name, ChangeSet changes) {
        super(name);
        this.changes = new ChangeSet(changes);
    }

    /**
       Get the ChangeSet.
       @return The ChangeSet.
    */
    public ChangeSet getChangeSet() {
        return changes;
    }

    /**
       Set the ChangeSet.
       @param changes The new ChangeSet.
    */
    public void setChangeSet(ChangeSet changes) {
        this.changes = new ChangeSet(changes);
    }

    @Override
    public void write(OutputStream out) throws IOException {
        changes.write(out);
    }

    @Override
    public void read(InputStream in) throws IOException {
        changes = new ChangeSet();
        changes.read(in);
    }

    @Override
    public String toString() {
        return getName() + " = " + changes.getChangedEntities().size() + " entities";
    }
}