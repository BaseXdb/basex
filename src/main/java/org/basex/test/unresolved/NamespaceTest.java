package org.basex.test.unresolved;

import static org.junit.Assert.*;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.Set;
import org.basex.core.cmd.XQuery;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests namespaces.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public class NamespaceTest {
  /** Database context. */
  private static Context context;

  /** Test documents. */
  private static String[][] docs = {
    { "d1", "<a xmlns='A'><b><c/><d xmlns='D'/></b><e/></a>" },
    { "d2", "<a xmlns='A'><b><c/><d xmlns='D'><g xmlns='G'/></d></b><e/></a>" }
  };

  /** 
   * Test query.
   * Detects malformed namespace hierarchy.
   */
  @Test
  public final void nsHierarchy() {
    query("insert node <f xmlns='F'/> into doc('d1')//*:e", "");
    try {
      new Open("d1").execute(context);
      assertEquals("\n" +
          "  Pre[1] xmlns=\"A\" \n" +
          "    Pre[4] xmlns=\"D\" \n" +
          "    Pre[6] xmlns=\"F\" ",
          context.data.ns.toString());
    } catch (Exception e) {
      fail(e.getMessage());
    } finally {
      try {
        new Close().execute(context);
      } catch(BaseXException e) { }
    }
  }
  
  /**
   * Test query.
   * Detects malformed namespace hierarchy.
   */
  @Test
  public final void nsHierarchy2() {
    query("insert node <f xmlns='F'/> into doc('d2')//*:e", "");
    try {
      new Open("d2").execute(context);
      assertEquals("\n" +
          "  Pre[1] xmlns=\"A\" \n" +
          "    Pre[4] xmlns=\"D\" \n" +
          "      Pre[5] xmlns=\"G\" \n" +
          "    Pre[7] xmlns=\"F\" ",
          context.data.ns.toString());
    } catch (Exception e) {
      fail(e.getMessage());
    } finally {
      try {
        new Close().execute(context);
      } catch(BaseXException e) { }
    }
  }

  /** 
   * Test query.
   * Detects malformed namespace hierarchy.
   */
  @Test
  public final void nsInAtt() {
    query("data(<a a='{namespace-uri-for-prefix('x', <b/>)}' xmlns:x='X'/>/@a)",
        "X");
  }

  /** 
   * Test query.
   * Detects malformed namespace hierarchy.
   */
  @Test
  public final void nsInBraces() {
    query("<a xmlns:x='X'>{namespace-uri-for-prefix('x', <b/>)}</a>/text()",
        "X");
  }

  /** 
   * Test query.
   * Detects malformed namespace hierarchy.
   */
  @Test
  public final void newPrefix() {
    query("<a>{ attribute {QName('http://bla', 'att')} {} }</a>",
        "<a xmlns:ns1='http://bla' ns1:att=''/>");
  }

  /** 
   * Test query.
   * Detects malformed namespace hierarchy.
   */
  @Test
  public final void newPrefix2() {
    query("<a xmlns:ns1='ns1'><b xmlns='ns1'>" +
        "  <c>{attribute {QName('ns1', 'att1')} {}," +
        "    attribute {QName('ns2', 'att2')} {}}</c>" +
        "</b></a>",
        "<a xmlns:ns1='ns1'><b xmlns='ns1'>" +
        "  <c xmlns:ns2='ns2' ns1:att1='' ns2:att2=''/>" +
        "</b></a>");
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
   * Runs a query and matches the result against the expected output.
   * @param query query
   * @param expected expected output
   */
  private void query(final String query, final String expected) {
    query(null, query, expected);
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
      fail(ex.getMessage());
    }
  }
}
