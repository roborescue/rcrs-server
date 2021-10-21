package rescuecore2.messages;

import rescuecore2.URN;

/**
   Abstract base class for message components.
 */
public abstract class AbstractMessageComponent implements MessageComponent {
    private final URN name;

    /**
       Construct a message component with a name.
       @param name The name of this component.
     */
    protected AbstractMessageComponent(URN name) {
        this.name = name;
    }

    @Override
    public URN getName() {
        return name;
    }
}
