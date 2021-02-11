package org.basex;

import org.basex.core.*;
import org.junit.jupiter.api.*;

/**
 * If this class is extended, tests will be run in a sandbox.
 *
 * @author BaseX Team 2005-21, BSD License
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
}
