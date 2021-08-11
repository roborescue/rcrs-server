package rescuecore2.messages;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
   An abstract base class for Message objects. This class is implemented in terms of MessageComponent objects so subclasses need only provide a urn and a list of MessageComponent objects.
 */
public abstract class AbstractMessage implements Message {
    private String urn;
    private List<MessageComponent> components;

    /**
       Construct a message with a given urn.
       @param urn The urn of the message.
     */
    protected AbstractMessage(String urn) {
        this.urn = urn;
        this.components = new ArrayList<MessageComponent>();
    }

    /**
       Construct a message with a urn defined as an enum.
       @param urn The urn of the message.
     */
    protected AbstractMessage(Enum<?> urn) {
        this(urn.toString());
    }

    @Override
    public final String getURN() {
        return urn;
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
        result.append(urn);
        result.append(" : ");
        for (Iterator<MessageComponent> it = components.iterator(); it.hasNext();) {
            MessageComponent next = it.next();
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
