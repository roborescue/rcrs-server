package rescuecore2.score;

import rescuecore2.config.Config;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.Entity;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Collections;

/**
   A score function that applies some function to the scores of a set of child score functions.
 */
public abstract class CompositeScoreFunction extends AbstractScoreFunction {
    /** The child score functions. */
    protected Set<ScoreFunction> children;

    /**
       Create a CompositeScoreFunction with no children.
       @param name The name of this function.
    */
    public CompositeScoreFunction(String name) {
        super(name);
        children = new HashSet<ScoreFunction>();
    }

    /**
       Create a CompositeScoreFunction with a collection of children.
       @param name The name of this function.
       @param c The child score functions.
    */
    public CompositeScoreFunction(String name, Collection<ScoreFunction> c) {
        this(name);
        addChildFunctions(c);
    }

    /**
       Create a CompositeScoreFunction with a collection of children.
       @param name The name of this function.
       @param c The child score functions.
    */
    public CompositeScoreFunction(String name, ScoreFunction... c) {
        this(name);
        addChildFunctions(c);
    }

    /**
       Add a child score function.
       @param child The child function to add.
    */
    public void addChildFunction(ScoreFunction child) {
        children.add(child);
    }

    /**
       Add a collection of child score functions.
       @param c The child functions to add.
    */
    public final void addChildFunctions(Collection<ScoreFunction> c) {
        for (ScoreFunction next : c) {
            addChildFunction(next);
        }
    }

    /**
       Add a collection of child score functions.
       @param c The child functions to add.
    */
    public final void addChildFunctions(ScoreFunction... c) {
        for (ScoreFunction next : c) {
            addChildFunction(next);
        }
    }

    /**
       Remove a child score function.
       @param child The child function to remove.
    */
    public void removeChildFunction(ScoreFunction child) {
        children.remove(child);
    }

    /**
       Remove a collection of child score functions.
       @param c The child functions to remove.
    */
    public final void removeChildFunctions(Collection<ScoreFunction> c) {
        for (ScoreFunction next : c) {
            removeChildFunction(next);
        }
    }

    /**
       Remove a collection of child score functions.
       @param c The child functions to remove.
    */
    public final void removeChildFunctions(ScoreFunction... c) {
        for (ScoreFunction next : c) {
            removeChildFunction(next);
        }
    }

    @Override
    public void initialise(WorldModel<? extends Entity> world, Config config) {
        for (ScoreFunction next : children) {
            next.initialise(world, config);
        }
    }

    /**
       Get all the child functions.
       @return All child functions.
    */
    public Set<ScoreFunction> getChildFunctions() {
        return Collections.unmodifiableSet(children);
    }
}
