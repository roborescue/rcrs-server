package rescuecore2.registry;

import java.util.Set;

import rescuecore2.worldmodel.Property;

/**
 * A property factory that filters urns that do not match a given set.
 */
public class FilterPropertyFactory extends AbstractFilterFactory<PropertyFactory> implements PropertyFactory {

  /**
   * Construct a FilterPropertyFactory.
   *
   * @param downstream The downstream property factory.
   * @param urns       The set of URNs.
   * @param inclusive  True if the set of URNs are allowed, false if they are
   *                   forbidden.
   */
  public FilterPropertyFactory(PropertyFactory downstream, Set<Integer> urns, boolean inclusive) {
    super(downstream, urns, inclusive);
  }

  @Override
  public Property makeProperty(int urn) {
    if (!isValidUrn(urn))
      return null;

    return downstream.makeProperty(urn);
  }
}