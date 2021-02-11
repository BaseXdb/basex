package org.basex.query.func;

/**
 * Java binding example.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class JavaFunctionExample {
  /** Value. */
  Boolean x;

  /**
   * Constructor.
   */
  public JavaFunctionExample() {
  }

  /**
   * Constructor.
   * @param x argument
   */
  public JavaFunctionExample(final boolean x) {
    this.x = x;
  }

  /**
   * Constructor.
   * @param x argument
   */
  public JavaFunctionExample(final Boolean x) {
    this.x = x;
  }

  /**
   * Constructor.
   * @param f argument
   * @return parameter
   */
  public String f(final String f) {
    return f;
  }

  /**
   * Constructor.
   * @param b argument
   * @return parameter
   */
  public boolean b(final boolean b) {
    return b;
  }

  /**
   * Constructor.
   * @param b argument
   * @return parameter
   */
  public boolean a(final boolean b) {
    return b;
  }

  /**
   * Constructor.
   * @param b argument
   * @return parameter
   */
  public Boolean a(final Boolean b) {
    return b;
  }

  /**
   * Constructor.
   * @param g argument
   * @return parameter
   */
  public String g(final String g) {
    return g;
  }

  /**
   * Constructor.
   * @param g argument
   * @return parameter
   */
  public Object g(final Integer g) {
    return g;
  }

  /**
   * Throws an exception.
   */
  public static void error() {
    throw new RuntimeException("ERROR");
  }
}
