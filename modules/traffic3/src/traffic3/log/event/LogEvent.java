package traffic3.log.event;

/**
 *
 */
public class LogEvent {

    private Object source;
    private Object message;
    private int type;

    /**
     * Constructor.
     * @param s source
     * @param m message
     * @param t type
     */
    public LogEvent(Object s, Object m, int t) {
        source = s;
        message = m;
        type = t;
    }

    /**
     * get source.
     * @return source
     */
    public Object getSource() {
        return source;
    }

    /**
     * get message.
     * @return message
     */
    public Object getMessage() {
        return message;
    }

    /**
     * get type.
     * @return type
     */
    public int getType() {
        return type;
    }
}
