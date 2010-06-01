package kernel;

import rescuecore2.messages.Command;
import rescuecore2.log.Logger;

/**
   A CommandFilter that ignores agent commands that have the wrong timestamp.
 */
public class WrongTimeCommandFilter extends AbstractCommandFilter {
    @Override
    protected boolean allowed(Command command, KernelState state) {
        if (command.getTime() == state.getTime()) {
            return true;
        }
        Logger.info("Ignoring command " + command + ": Wrong timestamp: " + command.getTime() + " should be " + state.getTime());
        return false;
    }
}
