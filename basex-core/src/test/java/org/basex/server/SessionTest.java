package org.basex.server;

import static org.basex.query.func.Function.*;
import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import java.io.*;

import org.basex.*;
import org.basex.api.client.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.junit.*;
import org.junit.Test;

/**
 * This class tests the client/server query API.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class SessionTest extends SandboxTest {
  /** Raw output method. */
  private static final String RAW = "declare option output:method 'raw';";
  /** Output stream. */
  ArrayOutput out;
  /** Serialization parameters to wrap query result with an element. */
  private static final String WRAPPER =
    "declare option output:wrap-prefix 'db';" +
    "declare option output:wrap-uri 'ns';";
  /** Client session. */
  Session session;

  /** Stops a session. */
  @After
  public final void stopSession() {
    try {
      if(cleanup) session.execute(new DropDB(NAME));
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
    assertEqual("A", session.execute("xquery 'A'"));
  }

  /** Runs a query command and wraps the result.
   * @throws IOException I/O exception */
  @Test
  public final void commandSerial1() throws IOException {
    session.execute("set serializer wrap-prefix=db,wrap-uri=ns");
    assertEqual("<db:results xmlns:db=\"ns\"/>", session.execute("xquery ()"));
  }

  /** Runs a query command and wraps the result.
   * @throws IOException I/O exception */
  @Test
  public final void commandSerial2() throws IOException {
    assertEqual("<db:results xmlns:db=\"ns\">" +
          "  <db:result>1</db:result>" +
          "</db:results>",
          session.execute("xquery " + WRAPPER + '1'));
  }

  /** Runs an erroneous query command.
   * @throws IOException I/O exception */
  @Test(expected = BaseXException.class)
  public final void commandError() throws IOException {
    session.execute("xquery (");
  }

  /**
   * Runs a query command and retrieves the result as string.
   * @throws IOException I/O exception
   */
  @Test(expected = BaseXException.class)
  public final void commandErr() throws IOException {
    session.execute("1,<a/>+''");
  }

  /**
   * Creates new databases.
   * @throws IOException I/O exception
   */
  @Test
  public final void create() throws IOException {
    session.create(NAME, new ArrayInput(""));
    assertEqual("", session.query("db:open('" + NAME + "')").execute());
    session.create(NAME, new ArrayInput("<X/>"));
    assertEqual("<X/>", session.query("db:open('" + NAME + "')").execute());
  }

  /**
   * Stops because of invalid input.
   * @throws IOException I/O exception
   */
  @Test(expected = BaseXException.class)
  public final void createErr() throws IOException {
    session.create(NAME, new ArrayInput("<"));
  }

  /**
   * Stops because of an invalid database name.
   * @throws IOException I/O exception
   */
  @Test(expected = BaseXException.class)
  public final void createNameErr() throws IOException {
    session.create("", new ArrayInput(""));
  }

  /**
   * Adds documents to a database.
   * @throws IOException I/O exception
   */
  @Test
  public final void add() throws IOException {
    session.execute("create db " + NAME);
    session.add(NAME, new ArrayInput("<X/>"));
    assertEqual("1", session.query("count(" + _DB_OPEN.args(NAME) + ')').execute());
    for(int i = 0; i < 9; i++) session.add(NAME, new ArrayInput("<X/>"));
    assertEqual("10", session.query("count(" + _DB_OPEN.args(NAME) + ')').execute());
  }

  /**
   * Adds a file with an invalid file name.
   * @throws IOException I/O exception
   */
  @Test(expected = BaseXException.class)
  public final void addNameErr() throws IOException {
    session.execute("create db " + NAME);
    session.add("", new ArrayInput("<X/>"));
  }

  /**
   * Adds a file with missing input.
   * @throws IOException I/O exception
   */
  @Test(expected = BaseXException.class)
  public final void addNoInput() throws IOException {
    session.execute("create db " + NAME);
    session.add("", new ArrayInput(""));
  }

  /**
   * Replaces documents in a database.
   * @throws IOException I/O exception
   */
  @Test
  public final void replace() throws IOException {
    session.execute("create db " + NAME);
    assertEqual("0", session.query("count(" + _DB_OPEN.args(NAME) + ')').execute());
    session.replace(NAME, new ArrayInput("<X/>"));
    assertEqual("1", session.query("count(" + _DB_OPEN.args(NAME) + ')').execute());
    session.replace(NAME + '2', new ArrayInput("<X/>"));
    assertEqual("2", session.query("count(" + _DB_OPEN.args(NAME) + ')').execute());
    session.replace(NAME + '2', new ArrayInput("<X/>"));
    assertEqual("2", session.query("count(" + _DB_OPEN.args(NAME) + ')').execute());
  }

  /**
   * Replaces a file with an invalid file name.
   * @throws IOException I/O exception
   */
  @Test(expected = BaseXException.class)
  public final void replaceNameErr() throws IOException {
    session.execute("create db " + NAME);
    session.replace("", new ArrayInput("<X/>"));
  }

  /**
   * Adds a file with missing input.
   * @throws IOException I/O exception
   */
  @Test(expected = BaseXException.class)
  public final void replaceNoInput() throws IOException {
    session.execute("create db " + NAME);
    session.replace("", new ArrayInput(""));
  }

  /**
   * Stores binary content in the database.
   * @throws IOException I/O exception
   */
  @Test
  public final void store() throws IOException {
    session.execute("create db " + NAME);
    session.store("X", new ArrayInput("!"));
    assertEqual("true", session.query(_DB_IS_RAW.args(NAME, "X")).execute());
    session.store("X", new ArrayInput(""));
    assertEqual("", session.query(_DB_RETRIEVE.args(NAME, "X")).execute());
    session.store("X", new ArrayInput(new byte[] { 0, 1, -1 }));
    assertEqual("0001FF", session.query(
        "xs:hexBinary(" + _DB_RETRIEVE.args(NAME, "X") + ')').execute());
    session.execute("drop db " + NAME);
  }

  /** Stores binary content.
   * @throws IOException I/O exception */
  @Test
  public void storeBinary() throws IOException {
    session.execute("create db " + NAME);
    session.store("X", new ArrayInput(new byte[] { -128, -2, -1, 0, 1, 127 }));
    assertEqual("-128 -2 -1 0 1 127", session.query(
        _CONVERT_BINARY_TO_BYTES.args(_DB_RETRIEVE.args(NAME, "X"))).execute());
  }

  /**
   * Stores binary content in the database.
   * @throws IOException I/O exception
   */
  @Test(expected = BaseXException.class)
  public final void storeNoDB() throws IOException {
    session.store("X", new ArrayInput("!"));
  }

  /**
   * Stores binary content in the database.
   * @throws IOException I/O exception
   */
  @Test(expected = BaseXException.class)
  public final void storeInvalid() throws IOException {
    session.execute("create db " + NAME);
    session.store("..", new ArrayInput("!"));
  }

  /** Retrieves binary content.
   * @throws IOException I/O exception */
  @Test
  public void retrieveBinary() throws IOException {
    session.execute("create db " + NAME);
    session.store("X", new ArrayInput("\0"));
    assertEqual("\0", session.execute("retrieve X"));
  }

  /** Retrieves empty content.
   * @throws IOException I/O exception */
  @Test
  public void retrieveEmpty() throws IOException {
    session.execute("create db " + NAME);
    session.store("X", new ArrayInput(""));
    assertEqual("", session.execute("retrieve X"));
  }

  /** Runs a query and retrieves the result as string.
   * @throws IOException I/O exception */
  @Test
  public void query() throws IOException {
    final Query query = session.query("1");
    assertEqual("1", query.execute());
  }

  /** Runs a query and retrieves the result as string.
   * @throws IOException I/O exception */
  @Test
  public void query2() throws IOException {
    final Query query = session.query("1");
    if(!query.more()) fail("No result returned");
    assertEqual("1", query.next());
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
    while(query.more()) assertEqual(++c, query.next());
    query.close();
  }

  /** Queries binary content.
   * @throws IOException I/O exception */
  @Test
  public void queryNullBinary() throws IOException {
    session.execute("create db " + NAME);
    session.store("X", new ArrayInput("\0"));
    assertEqual("\0", session.execute("xquery " + RAW + _DB_RETRIEVE.args(NAME, "X")));
    assertEqual("\0", session.query(RAW + _DB_RETRIEVE.args(NAME, "X")).execute());
    final Query q = session.query(RAW + _DB_RETRIEVE.args(NAME, "X"));
    assertTrue(q.more());
    assertEqual("\0", q.next());
    assertFalse(q.more());
  }

  /** Queries empty content.
   * @throws IOException I/O exception */
  @Test
  public void queryEmptyBinary() throws IOException {
    session.execute("create db " + NAME);
    session.store("X", new ArrayInput(""));
    assertEqual("", session.execute("xquery " + RAW + _DB_RETRIEVE.args(NAME, "X")));
    assertEqual("", session.query(RAW + _DB_RETRIEVE.args(NAME, "X")).execute());
    final Query q = session.query(RAW + _DB_RETRIEVE.args(NAME, "X"));
    assertTrue(q.more());
    assertEqual("", q.next());
    assertNull(q.next());
  }

  /** Queries empty content.
   * @throws IOException I/O exception */
  @Test
  public void queryEmptyString() throws IOException {
    final Query q = session.query("'',1");
    assertTrue(q.more());
    assertEqual("", q.next());
    assertTrue(q.more());
    assertEqual("1", q.next());
    assertNull(q.next());
  }

  /** Queries binary content (works only if output stream is specified).
   * @throws IOException I/O exception */
  @Test
  public void queryBinary() throws IOException {
    if(out == null) return;
    session.execute("create db " + NAME);
    final byte[] tmp = { 0, 1, 2, 127, 0, -1, -2, -128 };
    session.store("X", new ArrayInput(tmp));
    final String retr = _DB_RETRIEVE.args(NAME, "X");
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
    assertEqual("1", query.next());
    assertEqual("2", query.next());
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
    assertEqual("<db:results xmlns:db=\"ns\"/>", query.next());
    assertFalse("No result expected.", query.more());
  }

  /** Runs a query with additional serialization parameters.
   * @throws IOException I/O exception */
  @Test
  public void querySerial2() throws IOException {
    final Query query = session.query(WRAPPER + "1 to 2");
    assertTrue("Result expected.", query.more());
    assertEqual("<db:results xmlns:db=\"ns\">  <db:result>1</db:result>" +
        "  <db:result>2</db:result></db:results>", query.next());
  }

  /** Runs a query with an external variable declaration.
   * @throws IOException I/O exception */
  @Test
  public void queryBind() throws IOException {
    final Query query = session.query("declare variable $a external; $a");
    query.bind("$a", "4");
    assertEqual("4", query.execute());
    query.bind("$a", "5");
    assertEqual("5", query.next());
    query.bind("$a", "6");
    assertEqual("6", query.next());
    query.close();
  }

  /** Runs a query with an external variable declaration.
   * @throws IOException exception
   */
  @Test(expected = BaseXException.class)
  public void queryBind2() throws IOException {
    session.query("declare variable $a external; $a").next();
  }

  /** Runs a query with an external variable declaration.
   * @throws IOException I/O exception */
  @Test
  public void queryBindURI() throws IOException {
    final Query query = session.query("declare variable $a external; $a");
    query.bind("$a", "X", "xs:anyURI");
    assertEqual("X", query.next());
    query.close();
  }

  /** Runs a query with an external variable declaration.
   * @throws IOException I/O exception */
  @Test
  public void queryBindEmptySequence() throws IOException {
    final Query query = session.query("declare variable $a external; $a");
    query.bind("a", "()", "empty-sequence()");
    assertNull(query.next());
    query.close();
  }

  /** Runs a query with an external variable declaration.
   * @throws IOException I/O exception */
  @Test
  public void queryBindInt() throws IOException {
    Query query = session.query("declare variable $a as xs:integer external; $a");
    query.bind("a", "5", "xs:integer");
    assertEqual("5", query.next());
    query.close();

    query = session.query("declare variable $a external; $a");
    query.bind("a", Int.get(1), "xs:integer");
    assertEqual("1", query.next());
    query.close();
  }

  /** Runs a query with an external variable declaration.
   * @throws IOException I/O exception */
  @Test
  public void queryBindSequence() throws IOException {
    Query query = session.query("declare variable $a external; $a");
    query.bind("a", "1\u00012", "xs:integer");
    assertEqual("1", query.next());
    assertEqual("2", query.next());
    query.close();

    query = session.query("declare variable $a external; $a");
    query.bind("a", "09\u0002xs:hexBinary\u00012", "xs:integer");
    assertEqual("09", query.next());
    assertEqual("2", query.next());
    query.close();

    query = session.query("declare variable $a external; $a");
    query.bind("a", Seq.get(new Item[] { Int.get(1), Str.get("X") }));
    assertEqual("1", query.next());
    assertEqual("X", query.next());
    query.close();

    query = session.query("declare variable $a external; $a");
    query.bind("a", IntSeq.get(new long[] { 1, 2 }, AtomType.INT));
    assertEqual("1", query.next());
    assertEqual("2", query.next());
    query.close();

    query = session.query("declare variable $a external; $a");
    query.bind("a", IntSeq.get(new long[] { 1, 2 }, AtomType.INT), "xs:integer");
    assertEqual("1", query.next());
    assertEqual("2", query.next());
    query.close();
  }

  /** Runs a query with an external variable declaration.
   * @throws IOException I/O exception */
  @Test
  public void queryBindDynamic() throws IOException {
    final Query query = session.query("declare variable $a as xs:integer external; $a");
    query.bind("a", "1");
    assertEqual("1", query.execute());
    query.close();
  }

  /** Binds a document node to an external variable.
   * @throws IOException I/O exception */
  @Test
  public void queryBindDoc() throws IOException {
    final Query query = session.query("declare variable $a external; $a//text()");
    query.bind("$a", "<a>XML</a>", "document-node()");
    assertEqual("XML", query.execute());
  }

  /** Runs a query with a bound context value.
   * @throws IOException I/O exception */
  @Test
  public void queryContext() throws IOException {
    final Query query = session.query(".");
    query.context("5");
    assertEqual("5", query.next());
    query.close();
  }

  /** Runs a query with a bound context value.
   * @throws IOException I/O exception */
  @Test
  public void queryContextInt() throws IOException {
    final Query query = session.query(". * 2");
    query.context("6", "xs:integer");
    assertEqual("12", query.next());
    query.close();
  }

  /** Runs a query with a bound context value.
   * @throws IOException I/O exception */
  @Test
  public void queryContextVar() throws IOException {
    final Query query = session.query("declare variable $a := .; $a");
    query.context("<a/>", "element()");
    assertEqual("<a/>", query.next());
    query.close();
  }

  /** Runs a query, omitting more().
   * @throws IOException I/O exception */
  @Test
  public void queryInfo() throws IOException {
    final Query query = session.query("1 to 2");
    query.execute();
    query.close();
  }

  /** Runs a query and checks the serialization parameters.
   * @throws IOException I/O exception */
  @Test
  public void queryOptions() throws IOException {
    final Query query = session.query("declare option output:encoding 'US-ASCII';()");
    query.execute();
    final SerializerOptions sp = new SerializerOptions();
    sp.parse(query.options());
    assertEquals("US-ASCII", sp.get(SerializerOptions.ENCODING));
    query.close();
  }

  /** Runs a query and checks the updating flag.
   * @throws IOException I/O exception */
  @Test
  public void queryUpdating() throws IOException {
    // test non-updating query
    Query query = session.query("12345678");
    assertFalse(query.updating());
    assertEqual("12345678", query.execute());
    assertFalse(query.updating());
    query.close();

    // test updating query
    query = session.query("insert node <a/> into <b/>");
    assertTrue(query.updating());
    assertEqual("", query.execute());
    assertTrue(query.updating());
    query.close();
  }

  /** Runs an erroneous query.
   * @throws IOException expected exception*/
  @Test(expected = BaseXException.class)
  public void queryError() throws IOException {
    session.query("(").next();
  }

  /** Runs an erroneous query.
   * @throws IOException expected exception */
  @Test(expected = BaseXException.class)
  public void queryError2() throws IOException {
    session.query("(1,'a')[. eq 1]").execute();
  }

  /** Runs an erroneous query.
   * @throws IOException expected exception*/
  @Test(expected = BaseXException.class)
  public void queryError3() throws IOException {
    final Query query = session.query("(1,'a')[. eq 1]");
    assertEqual("1", query.next());
    query.next();
  }

  /** Runs two queries in parallel.
   * @throws IOException I/O exception */
  @Test
  public void queryParallel() throws IOException {
    final Query query1 = session.query("1 to 2");
    final Query query2 = session.query("reverse(3 to 4)");
    assertEqual("1", query1.next());
    assertEqual("4", query2.next());
    assertEqual("2", query1.next());
    assertEqual("3", query2.next());
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
    for(int q = 0; q < size; q++) assertEqual(q, cqs[q].next());
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
        assertEqual(test[1], q.execute());
      } finally { q.close(); }
    }
  }

  /** Runs a query and retrieves XML entities as string.
   * @throws IOException I/O exception */
  @Test
  public void queryEntities() throws IOException {
    final Query query = session.query("'&amp;&lt;&gt;&apos;&quot;'");
    assertEqual("&<>'\"", query.next());
  }

  /**
   * Checks if the most recent output equals the specified string.
   * @param exp expected string
   * @param ret string returned from the client API
   */
  private void assertEqual(final Object exp, final Object ret) {
    final String result = (out != null ? out : ret).toString();
    if(out != null) out.reset();
    assertEquals(exp.toString(), result.replaceAll("\\r|\\n", ""));
  }
}
