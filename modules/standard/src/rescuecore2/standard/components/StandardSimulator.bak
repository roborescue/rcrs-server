package rescuecore2.standard.components;

import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.components.AbstractSimulator;

/**
   Abstract base class for standard simulators.
*/
public abstract class StandardSimulator extends AbstractSimulator<StandardWorldModel> {
    @Override
    protected StandardWorldModel createWorldModel() {
        return new StandardWorldModel();
    }

    @Override
    protected void postConnect() {
        super.postConnect();
        model.index();
    }
}
