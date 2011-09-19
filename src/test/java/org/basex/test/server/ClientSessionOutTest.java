package org.basex.test.server;

import java.io.ByteArrayOutputStream;
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
    out = new ByteArrayOutputStream();
    super.startSession();
  }
}
