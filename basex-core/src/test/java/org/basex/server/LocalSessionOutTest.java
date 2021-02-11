package org.basex.server;

import org.basex.io.out.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the local session API with an output stream.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class LocalSessionOutTest extends LocalSessionTest {
  /** Initializes the test. */
  @Override
  @BeforeEach public void startSession() {
    out = new ArrayOutput();
    super.startSession();
  }
}
