package org.basex.test.server;

import static org.basex.core.Text.*;
import static org.basex.query.func.Function.*;
import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import java.io.IOException;

import org.basex.io.in.ArrayInput;
import org.basex.io.out.ArrayOutput;
import org.basex.server.Query;
import org.basex.server.Session;
import org.basex.util.Util;
import org.junit.After;
import org.junit.Test;

/**
 * This class tests the client/server query API.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class SessionTest {
  /** Test database name. */
  protected static final String DB = Util.name(SessionTest.class);
  /** Raw output method. */
  protected static final String RAW = "declare option output:method 'raw';";
  /** Output stream. */
  protected ArrayOutput out;
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
      fail(Util.message(ex));
    }
  }

  /**
   * Runs a query command and retrieves the result as string.
   * @throws IOException I/O exception
   */
  @Test
  public final void command() throws IOException {
    session.execute("set serializer wrap-prefix=,wrap-uri=");
    check("A", session.execute("xquery 'A'"));
  }

  /** Runs a query command and wraps the result.
   * @throws IOException I/O exception */
  @Test
  public final void commandSerial1() throws IOException {
    session.execute("set serializer wrap-prefix=db,wrap-uri=ns");
    check("<db:results xmlns:db=\"ns\"/>",
        session.execute("xquery ()"));
  }

  /** Runs a query command and wraps the result.
   * @throws IOException I/O exception */
  @Test
  public final void commandSerial2() throws IOException {
    check("<db:results xmlns:db=\"ns\">" +
          "  <db:result>1</db:result>" +
          "</db:results>",
          session.execute("xquery " + WRAPPER + "1"));
  }

  /** Runs an erroneous query command.
   * @throws IOException I/O exception */
  @Test(expected = org.basex.core.BaseXException.class)
  public final void commandError() throws IOException {
    session.execute("xquery (");
  }

  /**
   * Runs a query command and retrieves the result as string.
   * @throws IOException I/O exception
   */
  @Test(expected = org.basex.core.BaseXException.class)
  public final void commandErr() throws IOException {
    session.execute("1,<a/>+''");
  }

  /**
   * Creates new databases.
   * @throws IOException I/O exception
   */
  @Test
  public final void create() throws IOException {
    session.create(DB, new ArrayInput(""));
    check("", session.query("doc('" + DB + "')").execute());
    session.create(DB, new ArrayInput("<X/>"));
    check("<X/>", session.query("doc('" + DB + "')").execute());
  }

  /**
   * Stops because of invalid input.
   * @throws IOException I/O exception
   */
  @Test(expected = org.basex.core.BaseXException.class)
  public final void createErr() throws IOException {
    session.create(DB, new ArrayInput("<"));
  }

  /**
   * Stops because of an invalid database name.
   * @throws IOException I/O exception
   */
  @Test(expected = org.basex.core.BaseXException.class)
  public final void createNameErr() throws IOException {
    session.create("", new ArrayInput(""));
  }

  /**
   * Adds documents to a database.
   * @throws IOException I/O exception
   */
  @Test
  public final void add() throws IOException {
    session.execute("create db " + DB);
    session.add(DB, new ArrayInput("<X/>"));
    check("1", session.query("count(" + DBOPEN.args(DB) + ")").execute());
    for(int i = 0; i < 9; i++) session.add(DB, new ArrayInput("<X/>"));
    check("10", session.query("count(" + DBOPEN.args(DB) + ")").execute());
  }

  /**
   * Adds a file with an invalid file name.
   * @throws IOException I/O exception
   */
  @Test(expected = org.basex.core.BaseXException.class)
  public final void addNameErr() throws IOException {
    session.execute("create db " + DB);
    session.add("", new ArrayInput("<X/>"));
  }

  /**
   * Adds a file with missing input.
   * @throws IOException I/O exception
   */
  @Test(expected = org.basex.core.BaseXException.class)
  public final void addNoInput() throws IOException {
    session.execute("create db " + DB);
    session.add("", new ArrayInput(""));
  }

  /**
   * Replaces documents in a database.
   * @throws IOException I/O exception
   */
  @Test
  public final void replace() throws IOException {
    session.execute("create db " + DB);
    check("0", session.query("count(" + DBOPEN.args(DB) + ")").execute());
    session.replace(DB, new ArrayInput("<X/>"));
    check("1", session.query("count(" + DBOPEN.args(DB) + ")").execute());
    session.replace(DB + "2", new ArrayInput("<X/>"));
    check("2", session.query("count(" + DBOPEN.args(DB) + ")").execute());
    session.replace(DB + "2", new ArrayInput("<X/>"));
    check("2", session.query("count(" + DBOPEN.args(DB) + ")").execute());
  }

  /**
   * Replaces a file with an invalid file name.
   * @throws IOException I/O exception
   */
  @Test(expected = org.basex.core.BaseXException.class)
  public final void replaceNameErr() throws IOException {
    session.execute("create db " + DB);
    session.replace("", new ArrayInput("<X/>"));
  }

  /**
   * Adds a file with missing input.
   * @throws IOException I/O exception
   */
  @Test(expected = org.basex.core.BaseXException.class)
  public final void replaceNoInput() throws IOException {
    session.execute("create db " + DB);
    session.replace("", new ArrayInput(""));
  }

  /**
   * Stores binary content in the database.
   * @throws IOException I/O exception
   */
  @Test
  public final void store() throws IOException {
    session.execute("create db " + DB);
    session.store("X", new ArrayInput("!"));
    check("true", session.query(DBISRAW.args(DB, "X")).execute());
    session.store("X", new ArrayInput(""));
    check("", session.query(DBRETRIEVE.args(DB, "X")).execute());
    session.store("X", new ArrayInput(new byte[] { 0, 1, -1 }));
    check("0001FF", session.query(DBRETRIEVE.args(DB, "X")).execute());
    session.execute("drop db " + DB);
  }

  /** Stores binary content.
   * @throws IOException I/O exception */
  @Test
  public void storeBinary() throws IOException {
    session.execute("create db " + DB);
    session.store("X", new ArrayInput(new byte[] { -128, -2, -1, 0, 1, 127 }));
    check("-128 -2 -1 0 1 127",
        session.query(TO_BYTES.args(DBRETRIEVE.args(DB, "X"))).execute());
  }

  /**
   * Stores binary content in the database.
   * @throws IOException I/O exception
   */
  @Test(expected = org.basex.core.BaseXException.class)
  public final void storeNoDB() throws IOException {
    session.store("X", new ArrayInput("!"));
  }

  /**
   * Stores binary content in the database.
   * @throws IOException I/O exception
   */
  @Test(expected = org.basex.core.BaseXException.class)
  public final void storeInvalid() throws IOException {
    session.execute("create db " + DB);
    session.store("..", new ArrayInput("!"));
  }

  /** Retrieves binary content.
   * @throws IOException I/O exception */
  @Test
  public void retrieveBinary() throws IOException {
    session.execute("create db " + DB);
    session.store("X", new ArrayInput("\0"));
    check("\0", session.execute("retrieve X"));
  }

  /** Retrieves empty content.
   * @throws IOException I/O exception */
  @Test
  public void retrieveEmpty() throws IOException {
    session.execute("create db " + DB);
    session.store("X", new ArrayInput(""));
    check("", session.execute("retrieve X"));
  }

  /** Runs a query and retrieves the result as string.
   * @throws IOException I/O exception */
  @Test
  public void query() throws IOException {
    final Query query = session.query("1");
    check("1", query.execute());
  }

  /** Runs a query and retrieves the result as string.
   * @throws IOException I/O exception */
  @Test
  public void query2() throws IOException {
    final Query query = session.query("1");
    if(!query.more()) fail("No result returned");
    check("1", query.next());
  }

  /** Runs a query and retrieves the empty result as string.
   * @throws IOException I/O exception */
  @Test
  public void queryNoResult() throws IOException {
    final Query query = session.query("()");
    assertFalse("No result was expected.", query.more());
    query.close();
  }

  /** Tolerate multiple close calls.
   * @throws IOException I/O exception */
  @Test
  public void queryClose() throws IOException {
    final Query query = session.query("()");
    query.close();
    query.close();
  }

  /** Runs a query, using more().
   * @throws IOException I/O exception */
  @Test
  public void queryInit() throws IOException {
    final Query query = session.query("()");
    assertFalse("No result was expected.", query.more());
  }

  /** Runs a query and retrieves multiple results as string.
   * @throws IOException I/O exception */
  @Test
  public void queryMore() throws IOException {
    final Query query = session.query("1 to 3");
    int c = 0;
    while(query.more()) check(++c, query.next());
    query.close();
  }

  /** Queries binary content.
   * @throws IOException I/O exception */
  @Test
  public void queryNullBinary() throws IOException {
    session.execute("create db " + DB);
    session.store("X", new ArrayInput("\0"));
    check("\0", session.execute("xquery " + RAW + DBRETRIEVE.args(DB, "X")));
    check("\0", session.query(RAW + DBRETRIEVE.args(DB, "X")).execute());
    final Query q = session.query(RAW + DBRETRIEVE.args(DB, "X"));
    assertTrue(q.more());
    check("\0", q.next());
    assertFalse(q.more());
    assertNull(q.next());
  }

  /** Queries empty content.
   * @throws IOException I/O exception */
  @Test
  public void queryEmptyBinary() throws IOException {
    session.execute("create db " + DB);
    session.store("X", new ArrayInput(""));
    check("", session.execute("xquery " + RAW + DBRETRIEVE.args(DB, "X")));
    check("", session.query(RAW + DBRETRIEVE.args(DB, "X")).execute());
    final Query q = session.query(RAW + DBRETRIEVE.args(DB, "X"));
    assertTrue(q.more());
    check("", q.next());
    assertNull(q.next());
  }

  /** Queries empty content.
   * @throws IOException I/O exception */
  @Test
  public void queryEmptyString() throws IOException {
    final Query q = session.query("'',1");
    assertTrue(q.more());
    check("", q.next());
    assertTrue(q.more());
    check("1", q.next());
    assertNull(q.next());
  }

  /** Queries binary content (works only if output stream is specified).
   * @throws IOException I/O exception */
  @Test
  public void queryBinary() throws IOException {
    if(out == null) return;
    session.execute("create db " + DB);
    final byte[] tmp = { 0, 1, 2, 127, 0, -1, -2, -128 };
    session.store("X", new ArrayInput(tmp));
    final String retr = DBRETRIEVE.args(DB, "X");
    // check command
    session.execute("xquery " + RAW + retr + ',' + retr);
    assertTrue(eq(out.toArray(), concat(tmp, tmp)));
    out.reset();
    // check query execution
    session.query(RAW + retr + ',' + retr).execute();
    assertTrue(eq(out.toArray(), concat(tmp, tmp)));
    out.reset();
    // check iterator
    final Query q = session.query(RAW + retr + ',' + retr);
    q.next();
    assertTrue(eq(out.toArray(), tmp));
    out.reset();
    q.next();
    assertTrue(eq(out.toArray(), tmp));
    assertNull(q.next());
  }

  /** Runs a query, omitting more().
   * @throws IOException I/O exception */
  @Test
  public void queryNoMore() throws IOException {
    final Query query = session.query("1 to 2");
    check("1", query.next());
    check("2", query.next());
    assertNull(query.next());
    query.close();
  }

  /** Runs a query with additional serialization parameters.
   * @throws IOException I/O exception */
  @Test
  public void querySerial1() throws IOException {
    session.execute("set serializer wrap-prefix=db,wrap-uri=ns");
    final Query query = session.query(WRAPPER + "()");
    assertTrue("Result expected.", query.more());
    check("<db:results xmlns:db=\"ns\"/>", query.next());
    assertFalse("No result expected.", query.more());
  }

  /** Runs a query with additional serialization parameters.
   * @throws IOException I/O exception */
  @Test
  public void querySerial2() throws IOException {
    final Query query = session.query(WRAPPER + "1 to 2");
    assertTrue("Result expected.", query.more());
    check("<db:results xmlns:db=\"ns\">  <db:result>1</db:result>" +
        "  <db:result>2</db:result></db:results>", query.next());
  }

  /** Runs a query with an external variable declaration.
   * @throws IOException I/O exception */
  @Test
  public void queryBind() throws IOException {
    final Query query = session.query("declare variable $a external; $a");
    query.bind("$a", "5");
    check("5", query.next());
    query.close();
  }

  /** Runs a query with an external variable declaration.
   * @throws IOException exception
   */
  @Test(expected = org.basex.core.BaseXException.class)
  public void queryBind2() throws IOException {
    session.query("declare variable $a external; $a").next();
  }

  /** Runs a query with an external variable declaration.
   * @throws IOException I/O exception */
  @Test
  public void queryBindURI() throws IOException {
    final Query query = session.query(
        "declare variable $a external; $a");
    query.bind("$a", "X", "xs:anyURI");
    check("X", query.next());
    query.close();
  }

  /** Runs a query with an external variable declaration.
   * @throws IOException I/O exception */
  @Test
  public void queryBindInt() throws IOException {
    final Query query = session.query(
        "declare variable $a as xs:integer external; $a");
    query.bind("a", "5", "xs:integer");
    check("5", query.next());
    query.close();
  }

  /** Runs a query with an external variable declaration.
   * @throws IOException I/O exception */
  @Test
  public void queryBindDynamic() throws IOException {
    final Query query = session.query(
        "declare variable $a as xs:integer external; $a");
    query.bind("a", "1");
    check("1", query.execute());
    query.close();
  }

  /** Runs a query, omitting more().
   * @throws IOException I/O exception */
  @Test
  public void queryInfo() throws IOException {
    final Query query = session.query("1 to 2");
    query.execute();
    final String info = query.info();
    assertTrue("Total Time not contained in '" + info + "'.",
        info.contains(QUERYTOTAL));
    query.close();
  }

  /** Runs an erroneous query.
   * @throws IOException expected exception*/
  @Test(expected = org.basex.core.BaseXException.class)
  public void queryError() throws IOException {
    session.query("(").next();
  }

  /** Runs an erroneous query.
   * @throws IOException expected exception*/
  @Test(expected = org.basex.core.BaseXException.class)
  public void queryError2() throws IOException {
    session.query("(1,'a')[. eq 1]").execute();
  }

  /** Runs an erroneous query.
   * @throws IOException expected exception*/
  @Test(expected = org.basex.core.BaseXException.class)
  public void queryError3() throws IOException {
    final Query query = session.query("(1,'a')[. eq 1]");
    check("1", query.next());
    query.next();
  }

  /** Runs two queries in parallel.
   * @throws IOException I/O exception */
  @Test
  public void queryParallel() throws IOException {
    final Query query1 = session.query("1 to 2");
    final Query query2 = session.query("reverse(3 to 4)");
    check("1", query1.next());
    check("4", query2.next());
    check("2", query1.next());
    check("3", query2.next());
    assertNull(query1.next());
    assertNull(query2.next());
    query1.close();
    query2.close();
  }

  /** Runs 5 queries in parallel.
   * @throws IOException I/O exception */
  @Test
  public void queryParallel2() throws IOException {
    final int size = 8;
    final Query[] cqs = new Query[size];
    for(int q = 0; q < size; q++) cqs[q] = session.query(Integer.toString(q));
    for(int q = 0; q < size; q++) check(q, cqs[q].next());
    for(final Query query : cqs) query.close();
  }

  /** Binds maps to external variables via JSON.
   * @throws IOException I/O exception */
  @Test
  public void queryBindJson() throws IOException {
    final String var = "declare variable $x external;",
        map = "{\"foo\":[1,2,3],\"bar\":{\"a\":null,\"\":false}}";
    final String[][] tests = {
        {"for $k in map:keys($x) order by $k descending return $k", "foo bar"},
        {"every $k in map:keys($x('foo')) satisfies $k eq $x('foo')($k)",
          "true"},
        {"empty($x('bar')('a')) and not($x('bar')(''))", "true"},
    };
    for(final String[] test : tests) {
      final Query q = session.query(var + test[0]);
      try {
        q.bind("$x", map, "json");
        check(test[1], q.execute());
      } finally { q.close(); }
    }
  }

  /**
   * Checks if the most recent output equals the specified string.
   * @param exp expected string
   * @param ret string returned from the client API
   */
  protected final void check(final Object exp, final Object ret) {
    final String result = (out != null ? out : ret).toString();
    if(out != null) out.reset();
    assertEquals(exp.toString(), result.replaceAll("\\r|\\n", ""));
  }
}
