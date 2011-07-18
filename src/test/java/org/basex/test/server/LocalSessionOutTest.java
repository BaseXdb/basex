package org.basex.test.server;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;

import org.basex.core.BaseXException;
import org.basex.server.Query;
import org.junit.Before;
import org.junit.Test;

/**
 * This class tests the local API with an output stream.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class LocalSessionOutTest extends LocalSessionTest {
  /** Initializes the test. */
  @Override
  @Before
  public void startSession() {
    out = new ByteArrayOutputStream();
    super.startSession();
  }

  /** Runs a query and retrieves the result as string.
   * @throws BaseXException command exception */
  @Test
  public void query22() throws BaseXException {
    final Query query = session.query("1");
    if(!query.more()) fail("No result returned");
    check("1", query.next());
    check("", query.close());
  }
}
