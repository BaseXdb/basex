package org.basex.query;

import static org.basex.query.func.Function.*;
import static org.junit.Assert.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.cmd.Set;
import org.basex.io.out.*;
import org.basex.query.expr.*;
import org.basex.query.ft.*;
import org.basex.util.*;
import org.junit.*;
import org.junit.Test;

/**
 * This class tests if queries are rewritten for index access.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class IndexOptimizeTest extends AdvancedQueryTest {
  /**
   * Creates a test database.
   * @throws Exception exception
   */
  @BeforeClass
  public static void start() throws Exception {
    new DropDB(NAME).execute(context);
    new Set(MainOptions.FTINDEX, true).execute(context);
    new Set(MainOptions.QUERYINFO, true).execute(context);
  }

  /**
   * Drops the test database.
   * @throws Exception exception
   */
  @AfterClass
  public static void stop() throws Exception {
    new DropDB(NAME).execute(context);
  }

  /**
   * Checks the open command.
   * Test method.
   * @throws Exception unexpected exception
   */
  @Test
  public void openDocTest() throws Exception {
    createDoc();
    new Open(NAME).execute(context);
    check("//*[text() = '1']");
    check("data(//*[@* = 'y'])", "1");
    check("data(//@*[. = 'y'])", "y");
    check("//*[text() contains text '1']");
    check("//a[. = '1']");
    check("//xml[a = '1']");
  }

  /**
   * Checks the open command.
   * Test method.
   * @throws Exception unexpected exception
   */
  @Test
  public void openCollTest() throws Exception {
    createColl();
    new Open(NAME).execute(context);
    check("//*[text() = '1']");
    check("//*[text() contains text '1']");
    check("//a[. = '1']");
    check("//xml[a = '1']");
  }

  /**
   * Checks the XQuery doc() function.
   * @throws Exception unexpected exception
   */
  @Test
  public void docTest() throws Exception {
    createDoc();
    final String doc = DOC.args(NAME);
    check(doc + "//*[text() = '1']");
    check(doc + "//*[text() contains text '2']");
    check(doc + "//a[. = '1']");
    check(doc + "//xml[a = '1']");
  }

  /**
   * Checks the XQuery collection() function.
   * @throws Exception unexpected exception
   */
  @Test
  public void collTest() throws Exception {
    createColl();
    final String doc = COLLECTION.args(NAME);
    check(doc + "//*[text() = '1']");
    check(doc + "//*[text() contains text '2']");
    check(doc + "//a[. = '1']");
    check(doc + "//xml[a = '1']");
  }

  /**
   * Checks the XQuery db:open() function.
   * @throws Exception unexpected exception
   */
  @Test
  public void dbOpenTest() throws Exception {
    createColl();
    final String doc = _DB_OPEN.args(NAME);
    check(doc + "//*[text() = '1']");
    check(doc + "//*[text() contains text '2']");
    check(doc + "//a[. = '1']");
    check(doc + "//xml[a = '1']");
  }

  /**
   * Checks the XQuery db:open() function, using a specific path.
   * @throws Exception unexpected exception
   */
  @Test
  public void dbOpenExtTest() throws Exception {
    createColl();
    final String doc = _DB_OPEN.args(NAME, "two");
    check(doc + "//*[text() = '1']", "");
    check(doc + "//*[text() contains text '2']", "");
    check(doc + "//a[. = '1']", "");
    check(doc + "//xml[a = '1']", "");
    check(doc + "//*[text() = '4']", "<a>4</a>");
  }

  /**
   * Checks full-text requests.
   * @throws Exception unexpected exception
   */
  @Test
  public void ftTest() throws Exception {
    createDoc();
    new Open(NAME).execute(context);
    check("data(//*[text() contains text '1'])", "1");
    check("data(//*[text() contains text '1 2' any word])", "1 2 3");
    check("//*[text() contains text {'2','4'} all]", "");
    check("//*[text() contains text {'2','3'} all words]", "<a>2 3</a>");
    check("//*[text() contains text {'2','4'} all words]", "");
  }

  /**
   * Checks if a full-text index with language option is used.
   * @throws Exception unexpected exception
   */
  @Test
  public void ftTestLang() throws Exception {
    new Set(MainOptions.LANGUAGE, "de").execute(context);
    createDoc();
    new Open(NAME).execute(context);
    check("//text()[. contains text '1']");
    check("//text()[. contains text '1' using language 'de']");
    check("//text()[. contains text '1' using language 'German']");
  }

  /**
   * Checks index optimizations inside functions.
   * @throws Exception unexpected exception
   */
  @Test
  public void functionTest() throws Exception {
    createColl();
    // document access after inlining
    check("declare function local:x($d) { collection($d)//text()[. = '1'] };"
        + "local:x('" + NAME + "')", "1");
    check("declare function local:x($d, $s) { collection($d)//text()[. = $s] };"
        + "local:x('" + NAME + "', '1')", "1");

    // text: search term must be string
    final String doc = _DB_OPEN.args(NAME);
    check("declare function local:x() {" + doc +
        "//text()[. = '1'] }; local:x()", "1");
    check("declare function local:x($x as xs:string) {" + doc +
        "//text()[. = $x] }; local:x('1')", "1");
    // full-text: search term may can have any type
    check("declare function local:x() {" + doc +
        "//text()[. contains text '1'] }; local:x()", "1");
    check("declare function local:x($x) {" + doc +
        "//text()[. contains text { $x }] }; local:x('1')", "1");
  }

  /**
   * Checks predicate tests for empty strings.
   * @throws Exception unexpected exception
   */
  @Test
  public void empty() throws Exception {
    createDoc();
    new Open(NAME).execute(context);
    query("//*[text() = '']", "");
    query("//text()[. = '']", "");
    query("//*[. = '']", "<a/>");
    query("//a[. = '']", "<a/>");
    query("//a[. = <x/>]", "<a/>");

    query("//a[not(text() = '')]/text()", "12 3");
    query("//text()[not(. = '')]", "12 3");
    query("//a[not(. = '')]/text()", "12 3");
}

  /**
   * Creates a test database.
   * @throws Exception exception
   */
  private static void createDoc() throws Exception {
    new CreateDB(NAME, "<xml><a x='y'>1</a><a>2 3</a><a/></xml>").execute(context);
    new Close().execute(context);
  }

  /**
   * Creates a test collection.
   * @throws Exception exception
   */
  private static void createColl() throws Exception {
    new CreateDB(NAME).execute(context);
    new Add("one", "<xml><a>1</a><a>2 3</a></xml>").execute(context);
    new Add("two", "<xml><a>4</a><a>5 6</a></xml>").execute(context);
    new Optimize().execute(context);
    new Close().execute(context);
  }

  /**
   * Check if specified query was rewritten for index access.
   * @param query query to be tested
   */
  private static void check(final String query) {
    check(query, null);
  }

  /**
   * Checks if specified query was rewritten for index access, and checks the
   * query result.
   * @param query query to be tested
   * @param result expected query result
   */
  private static void check(final String query, final String result) {
    // compile query
    String plan = null;
    QueryProcessor qp = new QueryProcessor(query, context);
    try {
      ArrayOutput ao = qp.execute().serialize();
      if(result != null) assertEquals(result, ao.toString().replaceAll("\\r?\\n", ""));

      // fetch query plan
      plan = qp.plan().serialize().toString();
      qp.close();

      // check if index is used
      qp = new QueryProcessor(plan + "/descendant-or-self::*" +
          "[self::" + Util.className(ValueAccess.class) +
          "|self::" + Util.className(FTIndexAccess.class) + ']', context);
      ao = qp.execute().serialize();

      assertFalse("No index used:\n- Query: " + query + "\n- Plan: " + plan + "\n- " +
          qp.info().trim(), ao.toString().isEmpty());
    } catch(final QueryException ex) {
      fail(Util.message(ex) + "\n- Query: " + query + "\n- Plan: " + plan);
    } catch(final IOException ex) {
      fail(Util.message(ex));
    } finally {
      qp.close();
    }
  }
}
