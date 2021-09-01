package gis2;

/**
 * Exception class for problems with scenarios.
 */
public class ScenarioException extends Exception {
  /**
   * Construct a scenario exception with no information.
   */
  public ScenarioException() {
    super();
  }

  /**
   * Construct a scenario exception with an error message.
   *
   * @param msg The error message.
   */
  public ScenarioException(String msg) {
    super(msg);
  }

  /**
   * Construct a scenario exception that was caused by another exception.
   *
   * @param cause The cause of this exception.
   */
  public ScenarioException(Throwable cause) {
    super(cause);
  }

  /**
   * Construct a scenario exception with an error message and an underlying cause.
   *
   * @param msg   The error message.
   * @param cause The cause of this exception.
   */
  public ScenarioException(String msg, Throwable cause) {
    super(msg, cause);
  }
}