package org.basex.test.server;

import java.io.*;

import org.basex.io.in.*;
import org.basex.io.out.*;
import org.junit.*;

/**
 * This class tests the local session API with an output stream.
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
  /**
   * Creates new databases.
   * @throws IOException I/O exception
   */
  @Test
  public void ccreate() throws IOException {
    session.create(NAME, new ArrayInput(""));
    assertEqual("", session.query("doc('" + NAME + "')").execute());
  }
}
