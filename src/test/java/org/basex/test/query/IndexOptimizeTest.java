package org.basex.test.query;

import static org.basex.query.func.Function.*;
import static org.junit.Assert.*;

import java.io.IOException;

import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.Add;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.Optimize;
import org.basex.core.cmd.Set;
import org.basex.io.out.ArrayOutput;
import org.basex.io.serial.Serializer;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.util.Util;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests if queries are rewritten for index access.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class IndexOptimizeTest {
  /** Database context. */
  private static final Context CONTEXT = new Context();
  /** Test database name. */
  private static final String NAME = Util.name(IndexOptimizeTest.class);

  /**
   * Creates a test database.
   * @throws Exception exception
   */
  @BeforeClass
  public static void start() throws Exception {
    new Set(Prop.FTINDEX, true).execute(CONTEXT);
    new Set(Prop.QUERYINFO, true).execute(CONTEXT);
  }

  /**
   * Drops the test database.
   * @throws Exception exception
   */
  @AfterClass
  public static void stop() throws Exception {
    new DropDB(NAME).execute(CONTEXT);
  }

  /**
   * Checks the open command.
   * Test method.
   * @throws Exception unexpected exception
   */
  @Test
  public void openDocTest() throws Exception {
    createDoc();
    new Open(NAME).execute(CONTEXT);
    check("//*[text() = '1']");
    check("data(//*[@* = 'y'])", "1");
    check("data(//@*[. = 'y'])", "y");
    check("//*[text() contains text '1']");
  }

  /**
   * Checks the open command.
   * Test method.
   * @throws Exception unexpected exception
   */
  @Test
  public void openCollTest() throws Exception {
    createColl();
    new Open(NAME).execute(CONTEXT);
    check("//*[text() = '1']");
    check("//*[text() contains text '1']");
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
  }

  /**
   * Checks the XQuery collection() function.
   * @throws Exception unexpected exception
   */
  @Test
  public void collTest() throws Exception {
    createColl();
    final String doc = COLL.args(NAME);
    check(doc + "//*[text() = '1']");
    check(doc + "//*[text() contains text '2']");
  }

  /**
   * Checks the XQuery db:open() function.
   * @throws Exception unexpected exception
   */
  @Test
  public void dbOpenTest() throws Exception {
    createColl();
    final String doc = DBOPEN.args(NAME);
    check(doc + "//*[text() = '1']");
    check(doc + "//*[text() <- '2']");
  }

  /**
   * Checks the XQuery db:open() function, using a specific path.
   * @throws Exception unexpected exception
   */
  @Test
  public void dbOpenExtTest() throws Exception {
    createColl();
    final String doc = DBOPEN.args(NAME, "two");
    check(doc + "//*[text() = '1']", "");
    check(doc + "//*[text() = '4']", "<a>4</a>");
  }

  /**
   * Checks full-text requests.
   * @throws Exception unexpected exception
   */
  @Test
  public void ftTest() throws Exception {
    createDoc();
    new Open(NAME).execute(CONTEXT);
    check("data(//*[text() <- '1'])", "1");
    check("data(//*[text() <- '1 2' any word])", "1 2 3");
    check("//*[text() <- {'2','4'} all]", "");
    check("//*[text() <- {'2','3'} all words]", "<a>2 3</a>");
    check("//*[text() <- {'2','4'} all words]", "");
  }

  /**
   * Checks index optimizations inside functions.
   * @throws Exception unexpected exception
   */
  @Test
  public void functionTest() throws Exception {
    createColl();
    final String doc = DBOPEN.args(NAME);
    // text: search term must be string
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
   * Creates a test database.
   * @throws Exception exception
   */
  private void createDoc() throws Exception {
    new CreateDB(NAME, "<xml><a x='y'>1</a><a>2 3</a></xml>").execute(CONTEXT);
    new Close().execute(CONTEXT);
  }

  /**
   * Creates a test collection.
   * @throws Exception exception
   */
  private void createColl() throws Exception {
    new CreateDB(NAME).execute(CONTEXT);
    new Add("one", "<xml><a>1</a><a>2 3</a></xml>").execute(CONTEXT);
    new Add("two", "<xml><a>4</a><a>5 6</a></xml>").execute(CONTEXT);
    new Optimize().execute(CONTEXT);
    new Close().execute(CONTEXT);
  }

  /**
   * Check if specified query was rewritten for index access.
   * @param query query to be tested
   */
  private void check(final String query) {
    check(query, null);
  }

  /**
   * Checks if specified query was rewritten for index access, and checks the
   * query result.
   * @param query query to be tested
   * @param result expected query result
   */
  private void check(final String query, final String result) {
    // compile query
    ArrayOutput plan = null;
    QueryProcessor qp = new QueryProcessor(query, CONTEXT);
    try {
      ArrayOutput ao = new ArrayOutput();
      Serializer ser = qp.getSerializer(ao);
      qp.execute().serialize(ser);
      qp.close();
      final String info = qp.info();
      if(result != null)
        assertEquals(result, ao.toString().replaceAll("\\r?\\n", ""));

      // fetch query plan
      plan = new ArrayOutput();
      qp.plan(Serializer.get(plan));

      qp = new QueryProcessor(plan + "/descendant-or-self::*" +
          "[self::IndexAccess|self::FTIndexAccess]", CONTEXT);
      ao = new ArrayOutput();
      ser = qp.getSerializer(ao);
      qp.execute().serialize(ser);

      // check if IndexAccess is used
      assertTrue("No index used:\nQuery: " + query + "\nInfo: " + info +
          "\nPlan: " + plan, !ao.toString().isEmpty());
    } catch(final QueryException ex) {
      fail(Util.message(ex) + "\nQuery: " + query + "\nPlan: " + plan);
    } catch(final IOException ex) {
      fail(Util.message(ex));
    }
  }
}
