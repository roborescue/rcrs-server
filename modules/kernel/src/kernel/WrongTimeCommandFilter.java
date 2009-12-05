package kernel;

import rescuecore2.messages.Command;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
   A CommandFilter that ignores agent commands that have the wrong timestamp.
 */
public class WrongTimeCommandFilter extends AbstractCommandFilter {
    private static final Log LOG = LogFactory.getLog(WrongTimeCommandFilter.class);

    @Override
    protected boolean allowed(Command command, KernelState state) {
        if (command.getTime() == state.getTime()) {
            return true;
        }
        LOG.warn("Ignoring command with wrong timestamp: " + command.getTime() + " should be " + state.getTime());
        return false;
    }
}