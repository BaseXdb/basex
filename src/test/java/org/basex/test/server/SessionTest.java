package org.basex.test.server;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.basex.core.BaseXException;
import org.basex.server.Session;
import org.junit.After;
import org.junit.Test;

/**
 * This class tests the client/server query API.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class SessionTest {
  /** Output stream. */
  protected static ByteArrayOutputStream out;
  /** Serialization parameters to wrap query result with an element. */
  protected static final String WRAPPER =
    "declare option output:wrap-prefix 'db';" +
    "declare option output:wrap-uri 'ns';";
  /** Client session. */
  protected Session session;

  /** Stops a session. */
  @After
  public final void stopSession() {
    try {
      session.close();
    } catch(final IOException ex) {
      fail(ex.toString());
    }
  }

  /** Runs a query command and retrieves the result as string.
   * @throws BaseXException command exception */
  @Test
  public final void command() throws BaseXException {
    session.execute("set serializer wrap-prefix=,wrap-uri=");
    check("A", session.execute("xquery 'A'"));
  }

  /** Runs a query command and wraps the result.
   * @throws BaseXException command exception */
  @Test
  public final void commandSerial1() throws BaseXException {
    session.execute("set serializer wrap-prefix=db,wrap-uri=ns");
    check("<db:results xmlns:db=\"ns\"></db:results>",
        session.execute("xquery ()"));
  }

  /** Runs a query command and wraps the result.
   * @throws BaseXException command exception */
  @Test
  public final void commandSerial2() throws BaseXException {
    check("<db:results xmlns:db=\"ns\">" +
          "  <db:result>1</db:result>" +
          "</db:results>",
          session.execute("xquery " + WRAPPER + "1"));
  }

  /** Runs an erroneous query command.
   * @throws BaseXException expected exception */
  @Test(expected = org.basex.core.BaseXException.class)
  public final void commandError() throws BaseXException {
    session.execute("xquery (");
  }

  /**
   * Checks if the most recent output equals the specified string.
   * @param exp expected string
   * @param ret string returned from the client API
   */
  protected final void check(final Object exp, final Object ret) {
    String result;
    if(out == null) {
      result = ret.toString();
    } else {
      result = out.toString();
      out.reset();
    }
    assertEquals(exp.toString(), result.replaceAll("\\r|\\n", ""));
  }
}
