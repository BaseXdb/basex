package org.basex.server;

import static org.basex.query.func.Function.*;
import static org.basex.util.Token.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.*;
import org.basex.api.client.*;
import org.basex.api.dom.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * This class tests the client/server query API.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class SessionTest extends SandboxTest {
  /** Output stream. */
  ArrayOutput out;
  /** Client session. */
  Session session;

  /** Stops a session. */
  @AfterEach public final void stopSession() {
    try {
      session.execute(new DropDB(NAME));
      session.close();
    } catch(final IOException ex) {
      fail(Util.message(ex));
    }
  }

  /**
   * Runs a query command and retrieves the result as string.
   * @throws IOException I/O exception
   */
  @Test public final void command() throws IOException {
    assertEqual("A", session.execute("xquery 'A'"));
  }

  /** Runs an erroneous query command. */
  @Test public final void commandError() {
    assertThrows(BaseXException.class, () -> session.execute("xquery ("));
  }

  /**
   * Runs a query command and retrieves the result as string.
   */
  @Test public final void commandErr() {
    assertThrows(BaseXException.class, () -> session.execute("1,<a/>+''"));
  }

  /**
   * Creates new databases.
   * @throws IOException I/O exception
   */
  @Test public final void create() throws IOException {
    session.create(NAME, new ArrayInput(""));
    assertEqual("", session.query(_DB_OPEN.args(NAME)).execute());
    session.create(NAME, new ArrayInput("<X/>"));
    assertEqual("<X/>", session.query(_DB_OPEN.args(NAME)).execute());
  }

  /**
   * Stops because of invalid input.
   */
  @Test public final void createErr() {
    assertThrows(BaseXException.class, () -> session.create(NAME, new ArrayInput("<")));
  }

  /**
   * Stops because of an invalid database name.
   */
  @Test public final void createNameErr() {
    assertThrows(BaseXException.class, () -> session.create("", new ArrayInput("")));
  }

  /**
   * Adds documents to a database.
   * @throws IOException I/O exception
   */
  @Test public final void add() throws IOException {
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
  @Test public final void addNameErr() throws IOException {
    session.execute("create db " + NAME);
    assertThrows(BaseXException.class, () -> session.add("", new ArrayInput("<X/>")));
  }

  /**
   * Adds a file with missing input.
   * @throws IOException I/O exception
   */
  @Test public final void addNoInput() throws IOException {
    session.execute("create db " + NAME);
    assertThrows(BaseXException.class, () -> session.add("", new ArrayInput("")));
  }

  /**
   * Replaces documents in a database.
   * @throws IOException I/O exception
   */
  @Test public final void replace() throws IOException {
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
  @Test public final void replaceNameErr() throws IOException {
    session.execute("create db " + NAME);
    assertThrows(BaseXException.class, () -> session.replace("", new ArrayInput("<X/>")));
  }

  /**
   * Adds a file with missing input.
   * @throws IOException I/O exception
   */
  @Test public final void replaceNoInput() throws IOException {
    session.execute("create db " + NAME);
    assertThrows(BaseXException.class, () -> session.replace("", new ArrayInput("")));
  }

  /**
   * Stores binary content in the database.
   * @throws IOException I/O exception
   */
  @Test public final void store() throws IOException {
    session.execute("create db " + NAME);
    session.store("X", new ArrayInput("!"));
    assertEqual("true", session.query(_DB_IS_RAW.args(NAME, "X")).execute());
    session.store("X", new ArrayInput(""));
    assertEqual("", session.query(_DB_RETRIEVE.args(NAME, "X")).execute());
    session.store("X", new ArrayInput(new byte[] { 0, 1, -1 }));
    assertEqual("AAH/", session.query("string(" + _DB_RETRIEVE.args(NAME, "X") + ')').execute());
    session.execute("drop db " + NAME);
  }

  /** Stores binary content.
   * @throws IOException I/O exception */
  @Test public void storeBinary() throws IOException {
    session.execute("create db " + NAME);
    session.store("X", new ArrayInput(new byte[] { -128, -2, -1, 0, 1, 127 }));
    final Query query = session.query(_CONVERT_BINARY_TO_BYTES.args(_DB_RETRIEVE.args(NAME, "X")));
    assertEqual("-128\n-2\n-1\n0\n1\n127", query.execute());
  }

  /**
   * Stores binary content in the database.
   */
  @Test public final void storeNoDB() {
    assertThrows(BaseXException.class, () -> session.store("X", new ArrayInput("!")));
  }

  /**
   * Stores binary content in the database.
   * @throws IOException I/O exception
   */
  @Test public final void storeInvalid() throws IOException {
    session.execute("create db " + NAME);
    assertThrows(BaseXException.class, () -> session.store("..", new ArrayInput("!")));
  }

  /** Retrieves binary content.
   * @throws IOException I/O exception */
  @Test public void retrieveBinary() throws IOException {
    session.execute("create db " + NAME);
    session.store("X", new ArrayInput("\0"));
    assertEqual("\0", session.execute("retrieve X"));
  }

  /** Retrieves empty content.
   * @throws IOException I/O exception */
  @Test public void retrieveEmpty() throws IOException {
    session.execute("create db " + NAME);
    session.store("X", new ArrayInput(""));
    assertEqual("", session.execute("retrieve X"));
  }

  /** Runs a query and retrieves the result as string.
   * @throws IOException I/O exception */
  @Test public void query() throws IOException {
    final Query query = session.query("1");
    assertEqual("1", query.execute());
  }

  /** Runs a query and retrieves the result as string.
   * @throws IOException I/O exception */
  @Test public void query2() throws IOException {
    final Query query = session.query("1");
    if(!query.more()) fail("No result returned");
    assertEqual("1", query.next());
  }

  /** Runs a query and retrieves the empty result as string.
   * @throws IOException I/O exception */
  @Test public void queryNoResult() throws IOException {
    try(Query query = session.query("()")) {
      assertFalse(query.more(), "No result was expected.");
    }
  }

  /** Tolerate multiple close calls.
   * @throws IOException I/O exception */
  @Test public void queryClose() throws IOException {
    try(Query query = session.query("()")) {
      query.close();
    }
  }

  /** Runs a query, using more().
   * @throws IOException I/O exception */
  @Test public void queryInit() throws IOException {
    final Query query = session.query("()");
    assertFalse(query.more(), "No result was expected.");
  }

  /** Runs a query and retrieves multiple results as string.
   * @throws IOException I/O exception */
  @Test public void queryMore() throws IOException {
    try(Query query = session.query("1 to 3")) {
      int c = 0;
      while(query.more()) assertEqual(Integer.toString(++c), query.next());
    }
  }

  /** Queries binary content.
   * @throws IOException I/O exception */
  @Test public void queryNullBinary() throws IOException {
    session.execute("create db " + NAME);
    session.store("X", new ArrayInput("\0"));
    assertEqual("\0", session.execute("xquery " + _DB_RETRIEVE.args(NAME, "X")));
    assertEqual("\0", session.query(_DB_RETRIEVE.args(NAME, "X")).execute());
    final Query q = session.query(_DB_RETRIEVE.args(NAME, "X"));
    assertTrue(q.more());
    assertEqual("\0", q.next());
    assertFalse(q.more());
  }

  /** Queries empty content.
   * @throws IOException I/O exception */
  @Test public void queryEmptyBinary() throws IOException {
    session.execute("create db " + NAME);
    session.store("X", new ArrayInput(""));
    assertEqual("", session.execute("xquery " + _DB_RETRIEVE.args(NAME, "X")));
    assertEqual("", session.query(_DB_RETRIEVE.args(NAME, "X")).execute());
    final Query q = session.query(_DB_RETRIEVE.args(NAME, "X"));
    assertTrue(q.more());
    assertEqual("", q.next());
    assertNull(q.next());
  }

  /** Queries empty content.
   * @throws IOException I/O exception */
  @Test public void queryEmptyString() throws IOException {
    final Query q = session.query("'',1");
    assertTrue(q.more());
    assertEqual("", q.next());
    assertTrue(q.more());
    assertEqual("1", q.next());
    assertNull(q.next());
  }

  /** Queries binary content (works only if output stream is specified).
   * @throws IOException I/O exception */
  @Test public void queryBinary() throws IOException {
    if(out == null) return;
    session.execute("create db " + NAME);
    final byte[] tmp = { 0, 1, 2, 127, 0, -1, -2, -128 };
    session.store("X", new ArrayInput(tmp));
    final String retr = _DB_RETRIEVE.args(NAME, "X");
    // check command
    session.execute("xquery " + retr + ',' + retr);
    assertArrayEquals(concat(tmp, token(Prop.NL), tmp), out.next());
    // check query execution
    session.query(retr + ',' + retr).execute();
    assertArrayEquals(concat(tmp, token(Prop.NL), tmp), out.next());
    // check iterator
    final Query q = session.query(retr + ',' + retr);
    q.next();
    assertArrayEquals(tmp, out.next());
    q.next();
    assertArrayEquals(tmp, out.next());
    assertNull(q.next());
  }

  /** Runs a query, omitting more().
   * @throws IOException I/O exception */
  @Test public void queryNoMore() throws IOException {
    try(Query query = session.query("1 to 2")) {
      assertEqual("1", query.next());
      assertEqual("2", query.next());
      assertNull(query.next());
    }
  }

  /** Runs a query with an external variable declaration.
   * @throws IOException I/O exception */
  @Test public void queryBind() throws IOException {
    try(Query query = session.query("declare variable $a external; $a")) {
      query.bind("$a", "4");
      assertEqual("4", query.execute());
      query.bind("$a", "5");
      assertEqual("5", query.next());
      query.bind("$a", "6");
      assertEqual("6", query.next());
    }
  }

  /** Runs a query with an external variable declaration. */
  @Test public void queryBind2() {
    assertThrows(BaseXException.class,
      () -> session.query("declare variable $a external; $a").next());
  }

  /** Runs a query with an external variable declaration.
   * @throws IOException I/O exception */
  @Test public void queryBindURI() throws IOException {
    try(Query query = session.query("declare variable $a external; $a")) {
      query.bind("$a", "X", "xs:anyURI");
      assertEqual("X", query.next());
    }
  }

  /** Runs a query with an external variable declaration.
   * @throws IOException I/O exception */
  @Test public void queryBindEmptySequence() throws IOException {
    try(Query query = session.query("declare variable $a external; $a")) {
      query.bind("a", "()", "empty-sequence()");
      assertNull(query.next());
    }
  }

  /** Runs a query with an external variable declaration.
   * @throws IOException I/O exception */
  @Test public void queryBindInt() throws IOException {
    try(Query query = session.query("declare variable $a as xs:integer external; $a")) {
      query.bind("a", "5", "xs:integer");
      assertEqual("5", query.next());
    }

    try(Query query = session.query("declare variable $a external; $a")) {
      query.bind("a", Int.ONE, "xs:integer");
      assertEqual("1", query.next());
    }
  }

  /** Runs a query with an external variable declaration.
   * @throws IOException I/O exception */
  @Test public void queryBindSequence() throws IOException {
    try(Query query = session.query("declare variable $a external; $a")) {
      query.bind("a", "1\u00012", "xs:integer");
      assertEqual("1", query.next());
      assertEqual("2", query.next());
    }

    try(Query query = session.query("declare variable $a external; $a")) {
      query.bind("a", "09\u0002xs:hexBinary\u00012", "xs:integer");
      assertEqual("\t", query.next());
      assertEqual("2", query.next());
    }

    try(Query query = session.query("declare variable $a external; $a")) {
      query.bind("a", new ItemList().add(Int.ONE).add(Str.get("X")).value());
      assertEqual("1", query.next());
      assertEqual("X", query.next());
    }

    try(Query query = session.query("declare variable $a external; $a")) {
      query.bind("a", IntSeq.get(new long[] { 1, 2 }, AtomType.INT));
      assertEqual("1", query.next());
      assertEqual("2", query.next());
    }

    try(Query query = session.query("declare variable $a external; $a")) {
      query.bind("a", IntSeq.get(new long[] { 1, 2 }, AtomType.INT), "xs:integer");
      assertEqual("1", query.next());
      assertEqual("2", query.next());
    }
  }

  /** Runs a query with an external variable declaration.
   * @throws IOException I/O exception */
  @Test public void queryBindDynamic() throws IOException {
    try(Query query = session.query("declare variable $a as xs:integer external; $a")) {
      query.bind("a", "1");
      assertEqual("1", query.execute());
    }
  }

  /** Binds a document node to an external variable.
   * @throws IOException I/O exception */
  @Test public void queryBindDoc() throws IOException {
    try(Query query = session.query("declare variable $a external; $a//text()")) {
      query.bind("$a", "<a>XML</a>", "document-node()");
      assertEqual("XML", query.execute());
    }
  }

  /** Binds a node to an external variable.
   * @throws IOException I/O exception */
  @Test public void queryBindBXNode() throws IOException {
    try(Query query = session.query("declare variable $a as element() external; $a")) {
      query.bind("$a", BXNode.get(new FElem("a")));
      assertEqual("<a/>", query.execute());
    }

    final String string = "declare variable $a external; $a";
    try(Query query = session.query(string)) {
      query.bind("$a", BXNode.get(new FElem("a")));
      assertEqual("<a/>", query.execute());
    }

    try(Query query = session.query(string)) {
      query.bind("$a", BXNode.get(new FDoc().add(new FElem("a"))));
      assertEqual("<a/>", query.execute());
    }

    try(Query query = session.query(string)) {
      query.bind("$a", BXNode.get(new FTxt("a")));
      assertEqual("a", query.execute());
    }

    try(Query query = session.query(string)) {
      query.bind("$a", BXNode.get(new FPI("a", "b")));
      assertEqual("<?a b?>", query.execute());
    }

    try(Query query = session.query(string)) {
      query.bind("$a", BXNode.get(new FComm("a")));
      assertEqual("<!--a-->", query.execute());
    }
  }

  /** Runs a query with a bound context value.
   * @throws IOException I/O exception */
  @Test public void queryContext() throws IOException {
    try(Query query = session.query(".")) {
      query.context("5");
      assertEqual("5", query.next());
    }
  }

  /** Runs a query with a bound context value.
   * @throws IOException I/O exception */
  @Test public void queryContextInt() throws IOException {
    try(Query query = session.query(". * 2")) {
      query.context("6", "xs:integer");
      assertEqual("12", query.next());
    }
  }

  /** Runs a query with a bound context value.
   * @throws IOException I/O exception */
  @Test public void queryContextVar() throws IOException {
    try(Query query = session.query("declare variable $a := .; $a")) {
      query.context("<a/>", "element()");
      assertEqual("<a/>", query.next());
    }
  }

  /** Runs a query, omitting more().
   * @throws IOException I/O exception */
  @Test public void queryInfo() throws IOException {
    try(Query query = session.query("1 to 2")) {
      query.execute();
    }
  }

  /** Runs a query and checks the serialization parameters.
   * @throws IOException I/O exception */
  @Test public void queryOptions() throws IOException {
    try(Query query = session.query(SerializerOptions.ENCODING.arg("US-ASCII") + "()")) {
      query.execute();
      final SerializerOptions sp = new SerializerOptions();
      sp.assign(query.options());
      assertEquals("US-ASCII", sp.get(SerializerOptions.ENCODING));
    }
  }

  /** Runs a query and checks the updating flag.
   * @throws IOException I/O exception */
  @Test public void queryUpdating() throws IOException {
    // test non-updating query
    try(Query query = session.query("12345678")) {
      assertFalse(query.updating());
      assertEqual("12345678", query.execute());
      assertFalse(query.updating());
    }

    // test updating query
    try(Query query = session.query("insert node <a/> into <b/>")) {
      assertTrue(query.updating());
      assertEqual("", query.execute());
      assertTrue(query.updating());
    }
  }

  /** Runs an erroneous query. */
  @Test public void queryError() {
    assertThrows(BaseXException.class, () -> session.query("(").next());
  }

  /** Runs an erroneous query. */
  @Test public void queryError2() {
    assertThrows(BaseXException.class, () -> session.query("(1,'a')[. eq 1]").execute());
  }

  /** Runs an erroneous query.
   * @throws IOException I/O exception */
  @Test public void queryError3() throws IOException {
    final Query query = session.query("(1,'a')[. eq 1]");
    assertThrows(BaseXException.class, query::next);
  }

  /** Runs two queries in parallel.
   * @throws IOException I/O exception */
  @Test public void queryParallel() throws IOException {
    try(Query query1 = session.query("1 to 2"); Query query2 = session.query("reverse(3 to 4)")) {
      assertEqual("1", query1.next());
      assertEqual("4", query2.next());
      assertEqual("2", query1.next());
      assertEqual("3", query2.next());
      assertNull(query1.next());
      assertNull(query2.next());
    }
  }

  /** Runs 5 queries in parallel.
   * @throws IOException I/O exception */
  @Test public void queryParallel2() throws IOException {
    final int size = 8;
    final Query[] cqs = new Query[size];
    for(int q = 0; q < size; q++) cqs[q] = session.query(Integer.toString(q));
    for(int q = 0; q < size; q++) assertEqual(Integer.toString(q), cqs[q].next());
    for(final Query query : cqs) query.close();
  }

  /** Binds maps to external variables via JSON.
   * @throws IOException I/O exception */
  @Test public void queryBindJson() throws IOException {
    final String var = "declare variable $x external;",
        map = "{\"foo\":[1,2,3],\"bar\":{\"a\":null,\"\":false}}";
    final String[][] tests = {
        { "for $k in map:keys($x) order by $k descending return $k", "foo\nbar" },
        { "every $k in $x('foo')?* satisfies $k eq $x('foo')(xs:integer($k))", "true" },
        { "empty($x('bar')('a')) and not($x('bar')(''))", "true" },
    };
    for(final String[] test : tests) {
      try(Query qu = session.query(var + test[0])) {
        qu.bind("$x", map, "json");
        assertEqual(test[1], qu.execute());
      }
    }
  }

  /** Runs a query and retrieves XML entities as string.
   * @throws IOException I/O exception */
  @Test public void queryEntities() throws IOException {
    final Query query = session.query("'&amp;&lt;&gt;&apos;&quot;'");
    assertEqual("&<>'\"", query.next());
  }

  /** Runs a query and retrieves a map.
   * @throws IOException I/O exception */
  @Test public void queryJSON() throws IOException {
    final Query query = session.query(SerializerOptions.INDENT.arg("no") + "map { 'a': '&amp;' }");
    assertEqual("map{\"a\":\"&amp;\"}", query.next());
  }

  /**
   * Checks if the most recent output equals the specified string.
   * @param exp expected string
   * @param rtrn string returned from the client API
   */
  protected void assertEqual(final String exp, final String rtrn) {
    final String result = (out != null ? out : rtrn).toString();
    if(out != null) out.reset();
    assertEquals(exp, normNL(result));
  }
}
