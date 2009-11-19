package kernel;

import java.util.Collection;
import java.util.Iterator;

import rescuecore2.config.Config;
import rescuecore2.messages.Command;

/**
   An abstract base class for command filters.
 */
public abstract class AbstractCommandFilter implements CommandFilter {
    /** The kernel. */
    protected Kernel kernel;

    @Override
    public void initialise(Config config, Kernel k) {
        this.kernel = k;
    }

    @Override
    public void filter(Collection<Command> commands) {
        for (Iterator<Command> it = commands.iterator(); it.hasNext();) {
            if (!allowed(it.next())) {
                it.remove();
            }
        }
    }

    /**
       Find out if a particular command is allowed.
       @param command The command.
       @return True iff the command is allowed.
     */
    protected abstract boolean allowed(Command command);
}