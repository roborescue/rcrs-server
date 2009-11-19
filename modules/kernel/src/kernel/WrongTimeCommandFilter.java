package kernel;

import rescuecore2.messages.Command;

/**
   A CommandFilter that ignores agent commands that have the wrong timestamp.
 */
public class WrongTimeCommandFilter extends AbstractCommandFilter {
    @Override
    protected boolean allowed(Command command) {
        if (command.getTime() == kernel.getTime()) {
            return true;
        }
        System.out.println("Ignoring command with wrong timestamp: " + command.getTime() + " should be " + kernel.getTime());
        return false;
    }
}