package rescuecore2.messages.components;

import static rescuecore2.misc.EncodingTools.readInt32;
import static rescuecore2.misc.EncodingTools.writeInt32;

import rescuecore2.messages.AbstractMessageComponent;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
   An integer component to a message.
 */
public class IntComponent extends AbstractMessageComponent {
    private int value;

    /**
       Construct an IntComponent with no content.
       @param name The name of the component.
     */
    public IntComponent(String name) {
        super(name);
    }

    /**
       Construct an IntComponent with a specific value.
       @param name The name of the component.
       @param value The value of this component.
     */
    public IntComponent(String name, int value) {
        super(name);
        this.value = value;
    }

    /**
       Get the value of this message component.
       @return The value of the component.
     */
    public int getValue() {
        return value;
    }

    /**
       Set the value of this message component.
       @param value The value of the component.
     */
    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public void write(OutputStream out) throws IOException {
        writeInt32(value, out);
    }

    @Override
    public void read(InputStream in) throws IOException {
        value = readInt32(in);
    }

    @Override
    public String toString() {
        return getName() + " = " + value;
    }
}
