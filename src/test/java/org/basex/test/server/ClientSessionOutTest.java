package org.basex.test.server;

import java.io.ByteArrayOutputStream;

import org.junit.BeforeClass;

/**
 * This class tests the client/server API with an output stream.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class ClientSessionOutTest extends ClientSessionTest {
  /** Initializes the test. */
  @BeforeClass
  public static void start() {
    out = new ByteArrayOutputStream();
  }
}
