package rescuecore2.messages;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
   An abstract base class for Message objects. This class is implemented in terms of MessageComponent objects so subclasses need only provide a name, ID and a list of MessageComponent objects.
 */
public abstract class AbstractMessage implements Message {
    private String name;
    private int id;
    private List<MessageComponent> components;

    /**
       Construct a message with a given name and type ID.
       @param name The name of the message.
       @param id The type ID of the message.
     */
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

    /**
       Get all the components of this message.
       @return A List of MessageComponent objects.
     */
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
        for (Iterator<MessageComponent> it = components.iterator(); it.hasNext();) {
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

    @Override
    public void write(OutputStream out) throws IOException {
        for (MessageComponent next : components) {
            next.write(out);
        }	
    }

    @Override
    public void read(InputStream in) throws IOException {
        for (MessageComponent next : components) {
            next.read(in);
        }	
    }
}