package rescuecore2.log;

import org.apache.log4j.LogManager;
import org.apache.log4j.NDC;

import java.util.Deque;
import java.util.ArrayDeque;

/**
   System-wide logging facilities.
*/
public final class Logger {
    private static final InheritableThreadLocal<Deque<org.apache.log4j.Logger>> LOG = new InheritableThreadLocal<Deque<org.apache.log4j.Logger>>() {
        @Override
        protected Deque<org.apache.log4j.Logger> initialValue() {
            return new ArrayDeque<org.apache.log4j.Logger>();
        }

        @Override
        protected Deque<org.apache.log4j.Logger> childValue(Deque<org.apache.log4j.Logger> parent) {
            return new ArrayDeque<org.apache.log4j.Logger>(parent);
        }
    };

    private Logger() {
    }

    /**
       Set the log context for this thread and all child threads.
       @param context The new log context.
    */
    public static void setLogContext(String context) {
        Deque<org.apache.log4j.Logger> queue = LOG.get();
        queue.clear();
        queue.addLast(LogManager.getLogger(context));
    }

    /**
       Push a log context onto the stack.
       @param context The new log context.
    */
    public static void pushLogContext(String context) {
        Deque<org.apache.log4j.Logger> queue = LOG.get();
        queue.addLast(LogManager.getLogger(context));
    }

    /**
       Pop a log context from the stack.
    */
    public static void popLogContext() {
        Deque<org.apache.log4j.Logger> queue = LOG.get();
        queue.removeLast();
    }

    private static org.apache.log4j.Logger get() {
        Deque<org.apache.log4j.Logger> queue = LOG.get();
        if (queue.isEmpty()) {
            return LogManager.getRootLogger();
        }
        return queue.getLast();
    }

    /**
       Push an item onto the nested diagnostic context.
       @param s The item to push.
    */
    public static void pushNDC(String s) {
        NDC.push(s);
    }

    /**
       Pop an item from the nested diagnostic context.
    */
    public static void popNDC() {
        NDC.pop();
    }

    /**
       Log a trace level message.
       @param msg The message to log.
    */
    public static void trace(String msg) {
        get().trace(msg);
    }

    /**
       Log a trace level message along with a throwable.
       @param msg The message to log.
       @param t The throwable stack trace to log.
    */
    public static void trace(String msg, Throwable t) {
        get().trace(msg, t);
    }

    /**
       Log a debug level message.
       @param msg The message to log.
    */
    public static void debug(String msg) {
        get().debug(msg);
    }

    /**
       Log a debug level message along with a throwable.
       @param msg The message to log.
       @param t The throwable stack trace to log.
    */
    public static void debug(String msg, Throwable t) {
        get().debug(msg, t);
    }

    /**
       Log an info level message.
       @param msg The message to log.
    */
    public static void info(String msg) {
        get().info(msg);
    }

    /**
       Log an info level message along with a throwable.
       @param msg The message to log.
       @param t The throwable stack trace to log.
    */
    public static void info(String msg, Throwable t) {
        get().info(msg, t);
    }

    /**
       Log a warn level message.
       @param msg The message to log.
    */
    public static void warn(String msg) {
        get().warn(msg);
    }

    /**
       Log a warn level message along with a throwable.
       @param msg The message to log.
       @param t The throwable stack trace to log.
    */
    public static void warn(String msg, Throwable t) {
        get().warn(msg, t);
    }

    /**
       Log an error level message.
       @param msg The message to log.
    */
    public static void error(String msg) {
        get().error(msg);
    }

    /**
       Log an error level message along with a throwable.
       @param msg The message to log.
       @param t The throwable stack trace to log.
    */
    public static void error(String msg, Throwable t) {
        get().error(msg, t);
    }

    /**
       Log a fatal level message.
       @param msg The message to log.
    */
    public static void fatal(String msg) {
        get().fatal(msg);
    }

    /**
       Log a fatal level message along with a throwable.
       @param msg The message to log.
       @param t The throwable stack trace to log.
    */
    public static void fatal(String msg, Throwable t) {
        get().fatal(msg, t);
    }
}
