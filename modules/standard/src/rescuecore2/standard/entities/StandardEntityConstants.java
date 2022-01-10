package rescuecore2.standard.entities;

/**
 * Constants for the standard entity package.
 */
public final class StandardEntityConstants {

  /**
   * Enum defining building codes.
   */
  public enum BuildingCode {
    /** Wooden construction. */
    WOOD,
    /** Steel frame construction. */
    STEEL,
    /** Reinforced concrete construction. */
    CONCRETE;
  }

  /**
   * Enum defining different levels of fieryness.
   */
  public enum Fieryness {
    /** Not burnt at all. */
    UNBURNT,
    /** On fire a bit. */
    HEATING,
    /** On fire a bit more. */
    BURNING,
    /** On fire a lot. */
    INFERNO,
    /** Not burnt at all, but has water damage. */
    WATER_DAMAGE,
    /** Extinguished but minor damage. */
    MINOR_DAMAGE,
    /** Extinguished but moderate damage. */
    MODERATE_DAMAGE,
    /** Extinguished but major damage. */
    SEVERE_DAMAGE,
    /** Completely burnt out. */
    BURNT_OUT;
  }

  private StandardEntityConstants() {
  }
}