package org.basex.test.query;

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
 * @author BaseX Team 2005-11, BSD License
 * @author Lukas Kircher
 */
public final class NamespaceTest {
  /** Database context. */
  private static final Context CONTEXT = new Context();

  /** Test documents. */
  private static String[][] docs = {
    { "d1", "<x/>" },
    { "d2", "<x xmlns='xx'/>" },
    { "d3", "<a:x xmlns:a='aa'><b:y xmlns:b='bb'/></a:x>" },
    { "d4", "<a:x xmlns:a='aa'><a:y xmlns:b='bb'/></a:x>" },
    { "d5", "<a:x xmlns:a='aa'/>" },
    { "d6", "<a:x xmlns='xx' xmlns:a='aa'><a:y xmlns:b='bb'/></a:x>" },
    { "d7", "<x xmlns='xx'><y/></x>" },
    { "d8", "<a><b xmlns='B'/><c/></a>" },
    { "d9", "<a xmlns='A'><b><c/><d xmlns='D'/></b><e/></a>" },
    { "d10", "<a xmlns='A'><b><c/><d xmlns='D'><g xmlns='G'/></d></b><e/></a>"},
    { "d11", "<a xmlns='A'><b xmlns:ns1='AA'><d/></b><c xmlns:ns1='AA'>" +
    "<d/></c></a>" },
    { "d12", "<a><b/><c xmlns='B'/></a>" },
    { "d13", "<a><b xmlns='A'/></a>" },
    { "d14", "<a xmlns='A'><b xmlns='B'/><c xmlns='C'/></a>" },
    { "d15", "<a xmlns='A'><b xmlns='B'/><c xmlns='C'><d xmlns='D'/></c>" +
    "<e xmlns='E'/></a>" },
    { "d16", "<a><b/></a>" }
  };

  /**
   * Checks if namespace hierarchy structure is updated correctly on the
   * descendant axis after a NSNode has been inserted.
   */
  @Test
  public void insertIntoShiftPreValues() {
    query("insert node <b xmlns:ns='A'/> into doc('d12')/*:a/*:b", "");
    try {
      new Open("d12").execute(CONTEXT);
      assertEquals("\n" +
          "  Pre[3] xmlns:ns=\"A\" \n" +
          "  Pre[4] xmlns=\"B\" ",
          CONTEXT.data.ns.toString());
    } catch (final Exception ex) {
      fail(ex.getMessage());
    } finally {
      try {
        new Close().execute(CONTEXT);
      } catch(final BaseXException ex) { }
    }
  }

  /**
   * Checks if namespace hierarchy structure is updated correctly on the
   * descendant axis after a NSNode has been inserted.
   */
  @Test
  public void insertIntoShiftPreValues2() {
    query("insert node <c/> as first into doc('d13')/a", "");
    try {
      new Open("d13").execute(CONTEXT);
      assertEquals("\n" +
          "  Pre[3] xmlns=\"A\" ",
          CONTEXT.data.ns.toString());
    } catch (final Exception ex) {
      fail(ex.getMessage());
    } finally {
      try {
        new Close().execute(CONTEXT);
      } catch(final BaseXException ex) { }
    }
  }

  /**
   * Checks if namespace hierarchy structure is updated correctly on the
   * descendant axis after a NSNode has been inserted.
   */
  @Test
  public void insertIntoShiftPreValues3() {
    query("insert node <n xmlns='D'/> into doc('d14')/*:a/*:b", "");
    try {
      new Open("d14").execute(CONTEXT);
      assertEquals("\n" +
          "  Pre[1] xmlns=\"A\" \n" +
          "    Pre[2] xmlns=\"B\" \n" +
          "      Pre[3] xmlns=\"D\" \n" +
          "    Pre[4] xmlns=\"C\" ",
          CONTEXT.data.ns.toString());
    } catch (final Exception ex) {
      fail(ex.getMessage());
    } finally {
      try {
        new Close().execute(CONTEXT);
      } catch(final BaseXException ex) { }
    }
  }

  /**
   * Checks if namespace hierarchy structure is updated correctly on the
   * descendant axis after a NSNode has been deleted.
   */
  @Test
  public void deleteShiftPreValues() {
    query("delete node doc('d12')/a/b", "");
    try {
      new Open("d12").execute(CONTEXT);
      assertEquals("\n" +
          "  Pre[2] xmlns=\"B\" ",
          CONTEXT.data.ns.toString());
    } catch (final Exception ex) {
      fail(ex.getMessage());
    } finally {
      try {
        new Close().execute(CONTEXT);
      } catch(final BaseXException ex) { }
    }
  }

  /**
   * Checks if namespace hierarchy structure is updated correctly on the
   * descendant axis after a NSNode has been deleted.
   */
  @Test
  public void deleteShiftPreValues2() {
    query("delete node doc('d14')/*:a/*:b", "");
    try {
      new Open("d14").execute(CONTEXT);
      assertEquals("\n" +
          "  Pre[1] xmlns=\"A\" \n" +
          "    Pre[2] xmlns=\"C\" ",
          CONTEXT.data.ns.toString());
    } catch (final Exception ex) {
      fail(ex.getMessage());
    } finally {
      try {
        new Close().execute(CONTEXT);
      } catch(final BaseXException ex) { }
    }
  }

  /**
   * Checks if namespace hierarchy structure is updated correctly on the
   * descendant axis after a NSNode has been deleted.
   */
  @Test
  public void deleteShiftPreValues3() {
    query("delete node doc('d15')/*:a/*:c", "");
    try {
      new Open("d15").execute(CONTEXT);
      assertEquals("\n" +
          "  Pre[1] xmlns=\"A\" \n" +
          "    Pre[2] xmlns=\"B\" \n" +
          "    Pre[3] xmlns=\"E\" ",
          CONTEXT.data.ns.toString());
    } catch (final Exception ex) {
      fail(ex.getMessage());
    } finally {
      try {
        new Close().execute(CONTEXT);
      } catch(final BaseXException ex) { }
    }
  }

  /**
   * Checks if namespace hierarchy structure is updated correctly on the
   * descendant axis after a NSNode has been deleted.
   */
  @Test
  public void deleteShiftPreValues4() {
    query("delete node doc('d16')/a/b", "");
    try {
      new Open("d16").execute(CONTEXT);
      assertEquals("",
          CONTEXT.data.ns.toString());
    } catch (final Exception ex) {
      fail(ex.getMessage());
    } finally {
      try {
        new Close().execute(CONTEXT);
      } catch(final BaseXException ex) { }
    }
  }

  /**
   * Tests for correct namespace hierarchy, esp. if namespace nodes
   * on the following axis of an insert/delete operation are
   * updated correctly.
   */
  @Test
  public void delete1() {
    query("delete node doc('d11')/*:a/*:b",
        "doc('d11')/*:a",
        "<a xmlns='A'><c xmlns:ns1='AA'><d/></c></a>");
  }

  /** Test query. */
  @Test
  public void copy1() {
    query(
        "copy $c := <x:a xmlns:x='xx'><b/></x:a>/b modify () return $c",
        "<b xmlns:x='xx'/>");
  }

  /** Test query.
   * Detects corrupt namespace hierarchy.
   */
  @Test
  public void copy2() {
    query(
        "declare namespace a='aa'; copy $c:=doc('d4') modify () return $c//a:y",
        "<a:y xmlns:b='bb' xmlns:a='aa'/>");
  }

  /** Test query.
   * Detects missing prefix declaration.
   */
  @Test
  public void copy3() {
    query(
        "declare namespace a='aa'; copy $c:=doc('d4')//a:y " +
        "modify () return $c",
        "<a:y xmlns:b='bb' xmlns:a='aa'/>");
  }

  /** Test query.
   * Detects duplicate namespace declaration in MemData instance.
   */
  @Test
  public void copy4() {
    query(
        "copy $c := <a xmlns='test'><b><c/></b><d/></a> " +
        "modify () return $c",
        "<a xmlns='test'><b><c/></b><d/></a>");
  }

  /** Test query.
   *  Detects bogus namespace after insert.
   */
  @Test
  public void bogusDetector() {
    query(
        "insert node <a xmlns='test'><b><c/></b><d/></a> into doc('d1')/x",
        "declare namespace na = 'test';doc('d1')/x/na:a",
        "<a xmlns='test'><b><c/></b><d/></a>");
  }

  /** Test query.
   * Detects empty default namespace in serializer.
   */
  @Test
  public void emptyDefaultNamespace() {
    query("<ns:x xmlns:ns='X'><y/></ns:x>",
        "<ns:x xmlns:ns='X'><y/></ns:x>");
  }

  /** Test query.
   * Detects duplicate default namespace in serializer.
   */
  @Test
  public void duplicateDefaultNamespace() {
    query("<ns:x xmlns:ns='X'><y/></ns:x>",
        "<ns:x xmlns:ns='X'><y/></ns:x>");
  }

  /**
   * Test query.
   * Detects malformed namespace hierarchy.
   */
  @Test
  public void nsHierarchy() {
    query("insert node <f xmlns='F'/> into doc('d9')//*:e", "");
    try {
      new Open("d9").execute(CONTEXT);
      assertEquals("\n" +
          "  Pre[1] xmlns=\"A\" \n" +
          "    Pre[4] xmlns=\"D\" \n" +
          "    Pre[6] xmlns=\"F\" ",
          CONTEXT.data.ns.toString());
    } catch (final Exception ex) {
      fail(ex.getMessage());
    } finally {
      try {
        new Close().execute(CONTEXT);
      } catch(final BaseXException ex) { }
    }
  }

  /**
   * Test query.
   * Detects malformed namespace hierarchy.
   */
  @Test
  public void nsHierarchy2() {
    query("insert node <f xmlns='F'/> into doc('d10')//*:e", "");
    try {
      new Open("d10").execute(CONTEXT);
      assertEquals("\n" +
          "  Pre[1] xmlns=\"A\" \n" +
          "    Pre[4] xmlns=\"D\" \n" +
          "      Pre[5] xmlns=\"G\" \n" +
          "    Pre[7] xmlns=\"F\" ",
          CONTEXT.data.ns.toString());
    } catch (final Exception ex) {
      fail(ex.getMessage());
    } finally {
      try {
        new Close().execute(CONTEXT);
      } catch(final BaseXException ex) { }
    }
  }

  /** Test query. */
  @Test
  public void copy5() {
    query(
        "copy $c := <n><a:y xmlns:a='aa'/><a:y xmlns:a='aa'/></n> " +
        "modify () return $c",
    "<n><a:y xmlns:a='aa'/><a:y xmlns:a='aa'/></n>");
  }

  /** Test query. */
  @Test
  public void insertD2intoD1() {
    query(
        "insert node doc('d2') into doc('d1')/x",
        "doc('d1')",
        "<x><x xmlns='xx'/></x>");
  }

  /** Test query. */
  @Test
  public void insertD3intoD1() {
    query(
        "insert node doc('d3') into doc('d1')/x",
        "doc('d1')/x/*",
        "<a:x xmlns:a='aa'><b:y xmlns:b='bb'/></a:x>");
  }

  /** Test query. */
  @Test
  public void insertD3intoD1b() {
    query(
        "insert node doc('d3') into doc('d1')/x",
        "doc('d1')/x/*/*",
        "<b:y xmlns:b='bb' xmlns:a='aa'/>");
  }

  /** Test query.
   * Detects missing prefix declaration.
   */
  @Test
  public void insertD4intoD1() {
    query(
        "declare namespace a='aa'; insert node doc('d4')/a:x/a:y " +
        "into doc('d1')/x",
        "doc('d1')/x",
        "<x><a:y xmlns:a='aa' xmlns:b='bb'/></x>");
  }

  /** Test query.
   * Detects duplicate prefix declaration at pre=0 in MemData instance after
   * insert.
   * Though result correct, prefix
   * a is declared twice. -> Solution?
   */
  @Test
  public void insertD4intoD5() {
    query(
        "declare namespace a='aa';insert node doc('d4')//a:y " +
        "into doc('d5')/a:x",
        "declare namespace a='aa';doc('d5')//a:y",
        "<a:y xmlns:b='bb' xmlns:a='aa'/>");
  }

  /** Test query.
   * Detects duplicate namespace declarations in MemData instance.
   */
  @Test
  public void insertD7intoD1() {
    query(
        "declare namespace x='xx';insert node doc('d7')/x:x into doc('d1')/x",
        "doc('d1')/x",
        "<x><x xmlns='xx'><y/></x></x>");
  }

  /** Test query.
   * Detects general problems with namespace references.
   */
  @Test
  public void insertD6intoD4() {
    query(
        "declare namespace a='aa';insert node doc('d6') into doc('d4')/a:x",
        "declare namespace a='aa';doc('d4')/a:x/a:y",
        "<a:y xmlns:b='bb' xmlns:a='aa'/>");
  }

  /** Test query.
   * Detects general problems with namespace references.
   */
  @Test
  public void insertTransform1() {
    query(
        "declare default element namespace 'xyz';" +
        "copy $foo := <foo/> modify insert nodes (<bar/>, <baz/>)" +
        "into $foo return $foo",
        "<foo xmlns='xyz'><bar/><baz/></foo>");
  }

  /** Test query.
   * Detects wrong namespace references.
   */
  @Test
  public void uriStack() {
    query(
        "doc('d8')",
        "<a><b xmlns='B'/><c/></a>");
  }

  /**
   * Creates the database context.
   * @throws BaseXException database exception
   */
  @BeforeClass
  public static void start() throws BaseXException {
    // turn off pretty printing
    new Set(Prop.SERIALIZER, "indent=no").execute(CONTEXT);
  }

  /**
   * Creates all test databases.
   * @throws BaseXException database exception
   */
  @Before
  public void startTest() throws BaseXException {
    // create all test databases
    for(final String[] doc : docs) {
      new CreateDB(doc[0], doc[1]).execute(CONTEXT);
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
      new DropDB(doc[0]).execute(CONTEXT);
    }
    CONTEXT.close();
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
      if(first != null) new XQuery(first).execute(CONTEXT);
      final String result = new XQuery(second).execute(CONTEXT);

      // quotes are replaced by apostrophes to simplify comparison
      final String res = result.replaceAll("\\\"", "'");
      final String exp = expected.replaceAll("\\\"", "'");
      if(!exp.equals(res)) fail("\n" + res + "\n" + exp + " expected");
    } catch(final BaseXException ex) {
      fail(ex.getMessage());
    }
  }
}
