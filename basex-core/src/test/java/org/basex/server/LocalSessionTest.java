package org.basex.server;

import org.basex.api.client.*;
import org.junit.*;

/**
 * This class tests the local session API.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public class LocalSessionTest extends SessionTest {
  /** Starts a session. */
  @Before
  public void startSession() {
    session = new LocalSession(context, out);
  }
}
