package rescuecore2.registry;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumSet;
import rescuecore2.commands.Command;

/**
 * An abstract command factory with helper methods for defining URNs with enums.
 *
 * @param <T>
 *          An enum type that defines the URNs this factory knows about.
 */
public abstract class AbstractCommandFactory<T extends Enum<T>>
    implements CommandFactory {

  private Class<T> clazz;
  private Method   fromString;


  /**
   * Constructor for AbstractCommandFactory.
   *
   * @param clazz
   *          The class of enum this factory uses.
   */
  protected AbstractCommandFactory( Class<T> clazz ) {
    this.clazz = clazz;
    try {
      fromString = clazz.getDeclaredMethod( "fromString", String.class );
    } catch ( NoSuchMethodException e ) {
      fromString = null;
    }
  }


  @Override
  public String[] getKnownCommandURNs() {
    EnumSet<T> set = getKnownCommandURNsEnum();
    String[] result = new String[set.size()];
    int i = 0;
    for ( T next : set ) {
      result[i++] = next.toString();
    }
    return result;
  }


  @Override
  @SuppressWarnings( "unchecked" )
  public Command makeCommand( String urn ) {
    T t = null;
    if ( fromString != null ) {
      try {
        t = (T) fromString.invoke( null, urn );
      } catch ( IllegalAccessException e ) {
        t = null;
      } catch ( InvocationTargetException e ) {
        t = null;
      }
    }
    if ( t == null ) {
      t = Enum.valueOf( clazz, urn );
    }
    return makeCommand( t );
  }


  /**
   * Get an EnumSet containing known command URNs. Default implementation
   * returns EnumSet.allOf(T).
   *
   * @return An EnumSet containing known command URNs.
   */
  protected EnumSet<T> getKnownCommandURNsEnum() {
    return EnumSet.allOf( clazz );
  }


  /**
   * Create a command based on its urn and populate it with data from a string.
   * If the urn is not recognised then return null.
   *
   * @param urn
   *          The urn of the command type to create.
   * @return A new Command object, or null if the urn is not recognised.
   */
  protected abstract Command makeCommand( T urn );
}