package org.basex.test.server;

import java.io.*;

import org.basex.server.*;
import org.junit.*;

/**
 * This class tests the local session API.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class LocalSessionTest extends SessionTest {
  /** Starts a session. */
  @Before
  public void startSession() {
    session = new LocalSession(context, out);
  }

  /** Runs a query with a bound context item.
   * @throws IOException I/O exception */
  @Test
  public void x() throws IOException {
    final Query query = session.query("declare variable $a := .; $a");
    query.context("<a/>", "element()");
    assertEqual("<a/>", query.next());
    query.close();
  }
}
