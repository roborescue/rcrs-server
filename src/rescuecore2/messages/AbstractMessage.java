package rescuecore2.messages;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import rescuecore2.messages.MessageComponent;

/**
   An abstract base class for Message objects.
 */
public abstract class AbstractMessage implements Message {
    private String name;
    private int id;
    private List<MessageComponent> components;

    protected AbstractMessage(String name, int id) {
        this.name = name;
        this.id = id;
        this.components = new ArrayList<MessageComponent>();
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final int getMessageTypeID() {
        return id;
    }

    @Override
    public final List<MessageComponent> getComponents() {
        return components;
    }

    /**
       Add a message component.
       @param component The component to add.
     */
    protected void addMessageComponent(MessageComponent component) {
        components.add(component);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(name);
        result.append(" (");
        result.append(id);
        result.append(") : ");
        for (Iterator<MessageComponent> it = components.iterator(); it.hasNext(); ) {
            MessageComponent next = it.next();
            result.append(next.getName());
            result.append(": ");
            result.append(next.toString());
            if (it.hasNext()) {
                result.append(", ");
            }
        }
        return result.toString();
    }
}