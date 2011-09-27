package org.basex.test.server;

import org.basex.io.out.ArrayOutput;
import org.junit.Before;

/**
 * This class tests the client/server API with an output stream.
 *
 * @author BaseX Team 2005-11, BSD License
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
