package rescuecore2.standard.components;

import rescuecore2.components.AbstractSimulator;
import rescuecore2.standard.entities.StandardWorldModel;

/**
 * Abstract base class for standard simulators.
 */
public abstract class StandardSimulator
    extends AbstractSimulator<StandardWorldModel> {

  @Override
  protected StandardWorldModel createWorldModel() {
    return new StandardWorldModel();
  }


  @Override
  protected void postConnect() {
    super.postConnect();
    this.model.index();
  }
}