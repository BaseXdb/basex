package org.basex.test.unresolved;

import static org.junit.Assert.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Set;
import org.basex.core.cmd.XQuery;
import org.basex.data.Data;
import org.basex.io.IOContent;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.item.ANode;
import org.basex.query.item.DBNode;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodeCache;
import org.basex.query.util.Compare;
import org.basex.util.Util;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests namespaces.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class NamespaceTest {
  /** Database context. */
  private static Context context;

  /** Test documents. */
  private static String[][] docs = {
    { "d1", "<a xmlns:ns0='A'><b><d xmlns:ns1='D'>" +
        "<g xmlns:ns2='G'/></d></b></a>" },
    { "d2", "<n/>" }
  };

  /**
   * Test query.
   * Detects malformed namespace hierarchy.
   */
  @Test
  public void xuty0004() {
    try {
      new QueryProcessor("declare variable $input-context external;" +
          "let $source as node()* := (" +
          "    <status>on leave</status>," +
          "    <!-- for 6 months -->" +
          "  )," +
          "  $target := $input-context/works[1]/employee[1]" +
          "return insert nodes $source into $target", context).execute();
    } catch(final QueryException ex) {
      assertEquals("XUTY0004", string(ex.qname().local()));
    }
    fail("should throw XUTY0004");
  }

  /**
   * [LK] Namespaces: Test query.
   * Tests preserve, no-inherit for copy expression. Related to XQUTS
   * id-insert-expr-081-no-inherit.xq. Tests if no-inherit has a persistent
   * effect. Is it actually supposed to?
   * The <new/> tag is inserted into a fragment f using no-inherit and copy.
   * The resulting fragment is inserted into a database. The
   * namespaces in scope with prefix 'ns' are finally checked for the
   * inserted <new/> tag. If the result is non-empty we may have a problem -
   * being not able propagate the no-inherit flag to our table.
   */
  @Test
  public void copyPreserveNoInheritPersistent() {
    query("declare copy-namespaces preserve,no-inherit;" +
        "declare namespace my = 'ns';" +
        "let $v :=" +
        "(copy $c := <my:n><my:a/></my:n>" +
        "modify insert node <new/> into $c " +
        "return $c)" +
        "return insert node $v into doc('d2')/n",
        "namespace-uri-for-prefix('my', doc('d2')//*:new)",
        "");
  }

  /**
   * Extension to the JUnit methods to test for deep equality of XML fragments.
   * @param exp expected fragment
   * @param actual actual fragment
   */
  public static void assertDeepEquals(final String exp, final String actual) {
    try {
      if(!Compare.deep(getIter(exp), getIter(actual), null))
        assertEquals(exp, actual);
    } catch(final QueryException ex) { fail(Util.message(ex)); }
  }

  /**
   * Creates an iterator for the given XML fragment.
   * @param xml XML fragment
   * @return iterator
   */
  private static Iter getIter(final String xml) {
    try {
      final Data ex = CreateDB.mainMem(new IOContent(token(xml)), context);
      return new NodeCache(new ANode[] { new DBNode(ex, 0) }, 1);
    } catch(final IOException ex) { fail(Util.message(ex)); }
    return null;
  }

  /**
   * Creates the database context.
   * @throws BaseXException database exception
   */
  @BeforeClass
  public static void start() throws BaseXException {
    context = new Context();
    // turn off pretty printing
    new Set(Prop.SERIALIZER, "indent=no").execute(context);
  }

  /**
   * Creates all test databases.
   * @throws BaseXException database exception
   */
  @Before
  public void startTest() throws BaseXException {
    // create all test databases
    for(final String[] doc : docs) {
      new CreateDB(doc[0], doc[1]).execute(context);
    }
  }
  /**
   * Removes test databases and closes the database context.
   * @throws BaseXException database exception
   */
  @AfterClass
  public static void finish() throws BaseXException {
    // drop all test databases
    for(final String[] doc : docs) {
      new DropDB(doc[0]).execute(context);
    }
    context.close();
  }

  /**
   * Runs an updating query and matches the result of the second query
   * against the expected output.
   * @param first first query
   * @param second second query
   * @param expected expected output
   */
  private void query(final String first, final String second,
      final String expected) {

    try {
      if(first != null) new XQuery(first).execute(context);
      final String result = new XQuery(second).execute(context);
      // quotes are replaced by apostrophes to simplify comparison
      assertEquals(expected.replaceAll("\\\"", "'"),
          result.replaceAll("\\\"", "'"));
    } catch(final BaseXException ex) {
      fail(Util.message(ex));
    }
  }
}
