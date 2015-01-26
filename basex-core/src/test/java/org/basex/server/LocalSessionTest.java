package org.basex.server;

import java.io.*;

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

  /** Runs a query and retrieves JSON.
   * @throws IOException I/O exception */
  @Test
  public void x() throws IOException {
    final Query query = session.query("declare option output:indent 'no'; map { 'a': '\n' }");
    System.out.println(query.next());
  }
}
