package rescuecore2.registry;

import rescuecore2.commands.Command;

/**
 * Factory class for creating messages.
 */
public interface CommandFactory {

  /**
   * Create a command based on its urn and populate it with data from a string.
   * If the urn is not recognised then return null.
   *
   * @param urn
   *          The urn of the command type to create.
   * @return A new Command object, or null if the urn is not recognised.
   */
  Command makeCommand( String urn );

  /**
   * Get all command urns understood by this factory.
   *
   * @return All command urns.
   */
  String[] getKnownCommandURNs();
}