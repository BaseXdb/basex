package org.basex.server;

import org.basex.io.out.*;
import org.junit.*;

/**
 * This class tests the local session API with an output stream.
 *
 * @author BaseX Team 2005-15, BSD License
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
