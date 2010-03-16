package kernel;

import rescuecore2.config.Config;

import java.util.Collection;
import java.util.Iterator;

/**
   A TerminationCondition that returns true if any of its children return true.
*/
public class OrTerminationCondition implements TerminationCondition {
    private Collection<TerminationCondition> children;

    /**
       Construct a new OrTerminationCondition.
       @param children The child conditions. This must have at least one element.
    */
    public OrTerminationCondition(Collection<TerminationCondition> children) {
        if (children == null || children.size() == 0) {
            throw new IllegalArgumentException("Must have at least one child");
        }
        this.children = children;
    }

    @Override
    public boolean shouldStop(KernelState state) {
        for (TerminationCondition next : children) {
            if (next.shouldStop(state)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void initialise(Config config) {
        for (TerminationCondition next : children) {
            next.initialise(config);
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (Iterator<TerminationCondition> it = children.iterator(); it.hasNext();) {
            result.append(it.next());
            if (it.hasNext()) {
                result.append(" | ");
            }
        }
        return result.toString();
    }
}
