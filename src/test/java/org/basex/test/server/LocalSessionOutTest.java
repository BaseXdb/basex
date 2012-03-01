package org.basex.test.server;

import org.basex.io.out.*;
import org.junit.*;

/**
 * This class tests the local API with an output stream.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class LocalSessionOutTest extends LocalSessionTest {
  /** Initializes the test. */
  @Override
  @Before
  public void startSession() {
    out = new ArrayOutput();
    super.startSession();
  }
}