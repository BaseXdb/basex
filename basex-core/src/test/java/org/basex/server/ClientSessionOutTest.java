package org.basex.server;

import org.basex.io.out.*;
import org.junit.*;

/**
 * This class tests the client/server session API with an output stream.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class ClientSessionOutTest extends ClientSessionTest {
  /** Initializes the test. */
  @Override
  @Before
  public void startSession() {
    out = new ArrayOutput();
    super.startSession();
  }
}
