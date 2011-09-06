package org.basex.test.server;

import static org.basex.core.Text.*;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.basex.core.BaseXException;
import org.basex.server.Query;
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
  protected ByteArrayOutputStream out;
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
    check("<db:results xmlns:db=\"ns\"/>",
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

  /** Runs a query and retrieves the result as string.
   * @throws BaseXException command exception */
  @Test
  public void query() throws BaseXException {
    final Query query = session.query("1");
    check("1", query.execute());
    check("", query.close());
  }

  /** Runs a query and retrieves the result as string.
   * @throws BaseXException command exception */
  @Test
  public void query2() throws BaseXException {
    final Query query = session.query("1");
    if(!query.more()) fail("No result returned");
    check("1", query.next());
    check("", query.close());
  }

  /** Runs a query and retrieves the empty result as string.
   * @throws BaseXException command exception */
  @Test
  public void queryEmpty() throws BaseXException {
    final Query query = session.query("()");
    assertFalse("No result was expected.", query.more());
    query.close();
  }

  /** Tolerate multiple close calls.
   * @throws BaseXException command exception */
  @Test
  public void queryClose() throws BaseXException {
    final Query query = session.query("()");
    check("", query.close());
    check("", query.close());
    query.close();
  }

  /** Runs a query, using init().
   * @throws BaseXException command exception */
  @Test
  public void queryInit() throws BaseXException {
    final Query query = session.query("()");
    check("", query.init());
    assertFalse("No result was expected.", query.more());
    check("", query.close());
  }

  /** Runs a query and retrieves multiple results as string.
   * @throws BaseXException command exception */
  @Test
  public void queryMore() throws BaseXException {
    final Query query = session.query("1 to 3");
    int c = 0;
    query.init();
    while(query.more()) check(++c, query.next());
    query.close();
  }

  /** Runs a query, omitting more().
   * @throws BaseXException command exception */
  @Test
  public void queryNoMore() throws BaseXException {
    final Query query = session.query("1 to 2");
    check("1", query.next());
    check("2", query.next());
    check("", query.next());
    query.close();
  }

  /** Runs a query with additional serialization parameters.
   * @throws BaseXException command exception */
  @Test
  public void querySerial1() throws BaseXException {
    session.execute("set serializer wrap-prefix=db,wrap-uri=ns");
    final Query query = session.query(WRAPPER + "()");
    check("<db:results xmlns:db=\"ns\"", query.init());
    assertFalse("No result was expected.", query.more());
    check("/>", query.close());
  }

  /** Runs a query with additional serialization parameters.
   * @throws BaseXException command exception */
  @Test
  public void querySerial2() throws BaseXException {
    // avoid query evaluation, if more()/next() isn't called
    final Query query = session.query(WRAPPER + "1 to 10000000000000");
    check("<db:results xmlns:db=\"ns\"", query.init());
    check("/>", query.close());
  }

  /** Runs a query with additional serialization parameters.
   * @throws BaseXException command exception */
  @Test
  public void querySerial3() throws BaseXException {
    final Query query = session.query(WRAPPER + "1 to 2");
    check("<db:results xmlns:db=\"ns\"", query.init());
    check("><db:result>1</db:result>", query.next());
    check("<db:result>2</db:result>", query.next());
    check("</db:results>", query.close());
  }

  /** Runs a query with an external variable declaration.
   * @throws BaseXException command exception */
  @Test
  public void queryBind() throws BaseXException {
    final Query query = session.query("declare variable $a external; $a");
    query.bind("$a", "5");
    check("5", query.next());
    query.close();
  }

  /** Runs a query with an external variable declaration.
   * @throws BaseXException exception
   */
  @Test(expected = org.basex.core.BaseXException.class)
  public void queryBind2() throws BaseXException {
    session.query("declare variable $a external; $a").next();
  }

  /** Runs a query with an external variable declaration.
   * @throws BaseXException command exception */
  @Test
  public void queryBindURI() throws BaseXException {
    final Query query = session.query(
        "declare variable $a external; $a");
    query.bind("$a", "X", "xs:anyURI");
    check("X", query.next());
    query.close();
  }

  /** Runs a query with an external variable declaration.
   * @throws BaseXException command exception */
  @Test
  public void queryBindInt() throws BaseXException {
    final Query query = session.query(
        "declare variable $a as xs:integer external; $a");
    query.bind("a", "5", "xs:integer");
    check("5", query.next());
    query.close();
  }

  /** Runs a query with an external variable declaration.
   * @throws BaseXException command exception */
  @Test
  public void queryBindDynamic() throws BaseXException {
    final Query query = session.query(
        "declare variable $a as xs:integer external; $a");
    query.bind("a", "1");
    check("1", query.execute());
    query.close();
  }

  /** Runs a query, omitting more().
   * @throws BaseXException command exception */
  @Test
  public void queryInfo() throws BaseXException {
    final Query query = session.query("1 to 2");
    query.execute();
    final String info = query.info();
    assertTrue("Total Time not contained in '" + info + "'.",
        info.contains(QUERYTOTAL));
    query.close();
  }

  /** Runs an erroneous query.
   * @throws BaseXException expected exception*/
  @Test(expected = org.basex.core.BaseXException.class)
  public void queryError() throws BaseXException {
    final Query query = session.query("(");
    query.next();
  }

  /** Runs an erroneous query.
   * @throws BaseXException expected exception*/
  @Test(expected = org.basex.core.BaseXException.class)
  public void queryError3() throws BaseXException {
      final Query query = session.query("(1,'a')[. eq 1]");
      query.init();
      check("1", query.next());
      query.next();
  }

  /** Runs two queries in parallel.
   * @throws BaseXException command exception */
  @Test
  public void queryParallel() throws BaseXException {
    final Query query1 = session.query("1 to 2");
    final Query query2 = session.query("reverse(3 to 4)");
    check("1", query1.next());
    check("4", query2.next());
    check("2", query1.next());
    check("3", query2.next());
    check("", query1.next());
    check("", query2.next());
    query1.close();
    query2.close();
  }

  /** Runs 5 queries in parallel.
   * @throws BaseXException command exception */
  @Test
  public void query8() throws BaseXException {
    final int size = 8;
    final Query[] cqs = new Query[size];
    for(int q = 0; q < size; q++) cqs[q] = session.query(Integer.toString(q));
    for(int q = 0; q < size; q++) check(q, cqs[q].next());
    for(final Query query : cqs) query.close();
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
