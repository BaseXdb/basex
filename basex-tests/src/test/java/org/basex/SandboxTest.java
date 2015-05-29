package org.basex;

import org.basex.core.*;
import org.junit.*;

/**
 * If this class is extended, tests will be run in a sandbox.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public abstract class SandboxTest extends Sandbox {
  /**
   * Creates the sandbox.
   */
  @BeforeClass
  public static void initTests() {
    initSandbox();
  }

  /**
   * Removes test databases and closes the database context.
   */
  @AfterClass
  public static void finishTests() {
    finishSandbox();
  }
}
