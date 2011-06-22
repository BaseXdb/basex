package org.basex.test.server;

import static org.basex.core.Text.*;
import static org.junit.Assert.*;
import java.io.IOException;
import org.basex.BaseXServer;
import org.basex.core.BaseXException;
import org.basex.io.ArrayOutput;
import org.basex.server.ClientQuery;
import org.basex.server.ClientSession;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the client/server query API.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class ServerQueryTest {
  /** Serialization parameters to wrap query result with an element. */
  private static final String WRAPPER =
    "declare option output:wrap-prefix 'db';" +
    "declare option output:wrap-uri 'ns';";
  /** Server reference. */
  private static BaseXServer server;
  /** Client session. */
  private ClientSession cs;

  /** Starts the server. */
  @BeforeClass
  public static void start() {
    server = new BaseXServer("-z");
  }

  /** Starts a session. */
  @Before
  public void startSession() {
    try {
      cs = new ClientSession(server.context, ADMIN, ADMIN);
    } catch(final IOException ex) {
      fail(ex.toString());
    }
  }

  /** Stops a session. */
  @After
  public void stopSession() {
    try {
      cs.close();
    } catch(final IOException ex) {
      fail(ex.toString());
    }
  }

  /** Stops the server. */
  @AfterClass
  public static void stop() {
    server.stop();
  }

  /** Runs a query command and retrieves the result as string.
   * @throws BaseXException command exception */
  @Test
  public void command() throws BaseXException {
    assertEquals("A", cs.execute("xquery 'A'"));
  }

  /** Runs a query command and sends the result to an output stream.
   * @throws BaseXException command exception */
  @Test
  public void commandOut() throws BaseXException {
    final ArrayOutput ao = new ArrayOutput();
    cs.setOutputStream(ao);
    assertNull(cs.execute("info"));
    assertTrue(ao.size() != 0);
  }

  /** Runs a query command and wraps the result.
   * @throws BaseXException command exception */
  @Test
  public void commandSerial1() throws BaseXException {
    cs.execute("set serializer wrap-prefix=db,wrap-uri=ns");
    assertEquals(
      "<db:results xmlns:db=\"ns\">\n</db:results>",
      cs.execute("xquery ()").replaceAll("\\r", ""));
  }

  /** Runs a query command and wraps the result.
   * @throws BaseXException command exception */
  @Test
  public void commandSerial2() throws BaseXException {
    assertEquals(
      "<db:results xmlns:db=\"ns\">\n" +
      "  <db:result>1</db:result>\n" +
      "</db:results>",
      cs.execute("xquery " + WRAPPER + "1").replaceAll("\\r", ""));
  }

  /** Runs an erroneous query command.
   * @throws BaseXException expected exception */
  @Test(expected = org.basex.core.BaseXException.class)
  public void commandError() throws BaseXException {
      cs.execute("xquery (");
  }

  /** Runs a query and retrieves the result as string.
   * @throws BaseXException command exception */
  @Test
  public void query() throws BaseXException {
    final ClientQuery cq = cs.query("1");
    assertEquals("1", cq.execute());
    assertEquals("", cq.close());
  }

  /** Runs a query and retrieves the result as string.
   * @throws BaseXException command exception */
  @Test
  public void query2() throws BaseXException {
    final ClientQuery cq = cs.query("1");
    if(!cq.more()) fail("No result returned");
    assertEquals("1", cq.next());
    assertEquals("", cq.close());
  }

  /** Runs a query and retrieves the empty result as string.
   * @throws BaseXException command exception */
  @Test
  public void queryEmpty() throws BaseXException {
    final ClientQuery cq = cs.query("()");
    assertFalse("No result was expected.", cq.more());
    cq.close();
  }

  /** Tolerate multiple close calls.
   * @throws BaseXException command exception */
  @Test
  public void queryClose() throws BaseXException {
    final ClientQuery cq = cs.query("()");
    assertEquals("", cq.close());
    assertEquals("", cq.close());
    cq.close();
  }

  /** Runs a query, using init().
   * @throws BaseXException command exception */
  @Test
  public void queryInit() throws BaseXException {
    final ClientQuery cq = cs.query("()");
    assertEquals("", cq.init());
    assertFalse("No result was expected.", cq.more());
    assertEquals("", cq.close());
  }

  /** Runs a query and retrieves multiple results as string.
   * @throws BaseXException command exception */
  @Test
  public void queryMore() throws BaseXException {
    final ClientQuery cq = cs.query("1 to 3");
    int c = 0;
    cq.init();
    while(cq.more()) assertEquals(++c, Integer.parseInt(cq.next()));
    assertEquals(c, 3);
    cq.close();
  }

  /** Runs a query, omitting more().
   * @throws BaseXException command exception */
  @Test
  public void queryNoMore() throws BaseXException {
    final ClientQuery cq = cs.query("1 to 2");
    assertEquals("1", cq.next());
    assertEquals("2", cq.next());
    assertEquals("", cq.next());
    cq.close();
  }

  /** Runs a query with additional serialization parameters.
   * @throws BaseXException command exception */
  @Test
  public void querySerial1() throws BaseXException {
    cs.execute("set serializer wrap-prefix=db,wrap-uri=ns");
    final ClientQuery cq = cs.query(WRAPPER + "()");
    assertEquals("<db:results xmlns:db=\"ns\">", cq.init());
    assertFalse("No result was expected.", cq.more());
    assertEquals("</db:results>", cq.close().replaceAll("\\r|\\n", ""));
  }

  /** Runs a query with additional serialization parameters.
   * @throws BaseXException command exception */
  @Test
  public void querySerial2() throws BaseXException {
    // avoid query evaluation, if more()/next() isn't called
    final ClientQuery cq = cs.query(WRAPPER + "1 to 10000000000000");
    assertEquals("<db:results xmlns:db=\"ns\">", cq.init());
    assertEquals("</db:results>", cq.close().replaceAll("\\r|\\n", ""));
  }

  /** Runs a query with additional serialization parameters.
   * @throws BaseXException command exception */
  @Test
  public void querySerial3() throws BaseXException {
    final ClientQuery cq = cs.query(WRAPPER + "1 to 2");
    assertEquals("<db:results xmlns:db=\"ns\">", cq.init());
    assertEquals("<db:result>1</db:result>", cq.next());
    assertEquals("<db:result>2</db:result>", cq.next());
    assertEquals("</db:results>", cq.close().replaceAll("\\r|\\n", ""));
  }

  /** Runs a query and sends the result to an output stream.
   * @throws BaseXException command exception */
  @Test
  public void queryOut() throws BaseXException {
    final ArrayOutput ao = new ArrayOutput();
    cs.setOutputStream(ao);
    final ClientQuery cq = cs.query("1");
    assertTrue("No result was returned.", cq.more());
    assertNull(cq.next());
    assertEquals("1", ao.toString());
    cq.close();
  }

  /** Runs a query with an external variable declaration.
   * @throws BaseXException command exception */
  @Test
  public void queryBind() throws BaseXException {
    final ClientQuery cq = cs.query("declare variable $a external; $a");
    cq.bind("$a", "5");
    assertEquals("5", cq.next());
    cq.close();
  }

  /** Runs a query with an external variable declaration.
   * @throws BaseXException exception
   */
  @Test(expected = org.basex.core.BaseXException.class)
  public void queryBind2() throws BaseXException {
    cs.query("declare variable $a external; $a").next();
  }

  /** Runs a query with an external variable declaration.
   * @throws BaseXException command exception */
  @Test
  public void queryBindURI() throws BaseXException {
    final ClientQuery cq = cs.query(
        "declare variable $a external; $a");
    cq.bind("$a", "X", "xs:anyURI");
    assertEquals("X", cq.next());
    cq.close();
  }

  /** Runs a query with an external variable declaration.
   * @throws BaseXException command exception */
  @Test
  public void queryBindInt() throws BaseXException {
    final ClientQuery cq = cs.query(
        "declare variable $a as xs:integer external; $a");
    cq.bind("a", "5", "xs:integer");
    assertEquals("5", cq.next());
    cq.close();
  }

  /** Runs a query with an external variable declaration.
   * @throws BaseXException command exception */
  @Test
  public void queryBindDynamic() throws BaseXException {
    final ClientQuery cq = cs.query(
        "declare variable $a as xs:integer external; $a");
    cq.bind("a", "1");
    assertEquals("1", cq.execute());
    cq.close();
  }

  /** Runs a query, omitting more().
   * @throws BaseXException command exception */
  @Test
  public void queryInfo() throws BaseXException {
    final ClientQuery cq = cs.query("1 to 2");
    cq.execute();
    final String info = cq.info();
    assertTrue("Total Time not contained in '" + info + "'.",
        info.contains(QUERYTOTAL));
    cq.close();
  }

  /** Runs an erroneous query.
   * @throws BaseXException expected exception*/
  @Test(expected = org.basex.core.BaseXException.class)
  public void queryError() throws BaseXException {
      final ClientQuery cq = cs.query("(");
      cq.next();
  }

  /** Runs an erroneous query. */
  @Test
  public void queryError2() {
    try {
      cs.query("(");
      fail("Error expected.");
    } catch(final BaseXException ex) {
    }
  }

  /** Runs an erroneous query.
   * @throws BaseXException expected exception*/
  @Test(expected = org.basex.core.BaseXException.class)

  public void queryError3() throws BaseXException {
      final ClientQuery cq = cs.query("(1,'a')[. eq 1]");
      cq.init();
      assertEquals("1", cq.next());
      cq.next();
  }

  /** Runs two queries in parallel.
   * @throws BaseXException command exception */
  @Test
  public void queryParallel() throws BaseXException {
    final ClientQuery cq1 = cs.query("1 to 2");
    final ClientQuery cq2 = cs.query("reverse(3 to 4)");
    assertEquals("1", cq1.next());
    assertEquals("4", cq2.next());
    assertEquals("2", cq1.next());
    assertEquals("3", cq2.next());
    assertEquals("", cq1.next());
    assertEquals("", cq2.next());
    cq1.close();
    cq2.close();
  }

  /** Runs 5 queries in parallel.
   * @throws BaseXException command exception */
  @Test
  public void query8() throws BaseXException {
    final int size = 8;
    final ClientQuery[] cqs = new ClientQuery[size];
    for(int q = 0; q < size; q++) cqs[q] = cs.query(Integer.toString(q));
    for(int q = 0; q < size; q++)
      assertEquals(q, Integer.parseInt(cqs[q].next()));
    for(final ClientQuery cq : cqs) cq.close();
  }
}
