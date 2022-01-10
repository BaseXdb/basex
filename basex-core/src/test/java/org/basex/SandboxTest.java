package org.basex;

import org.basex.core.*;
import org.junit.jupiter.api.*;

/**
 * If this class is extended, tests will be run in a sandbox.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public abstract class SandboxTest extends Sandbox {
  /**
   * Creates the sandbox.
   */
  @BeforeAll public static void initTests() {
    initSandbox();
  }

  /**
   * Removes test databases and closes the database context.
   */
  @AfterAll public static void finishTests() {
    finishSandbox();
  }

  /**
   * Returns a context value reference string that will not be optimized at compile time.
   * @return container
   */
  public static String wrapContext() {
    return " data(attribute _ { . })";
  }

  /**
   * Returns a value string that will not be optimized at compile time.
   * @param value value to return
   * @return container
   */
  public static String wrap(final Object value) {
    return " data(attribute _ { '" + value + "' })";
  }
}
