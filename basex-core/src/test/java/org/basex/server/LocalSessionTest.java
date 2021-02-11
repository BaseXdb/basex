package org.basex.server;

import org.basex.api.client.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the local session API.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class LocalSessionTest extends SessionTest {
  /** Starts a session. */
  @BeforeEach public void startSession() {
    session = new LocalSession(context, out);
  }
}
