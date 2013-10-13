package org.basex.test.build;

import static org.junit.Assert.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.query.*;
import org.basex.test.*;
import org.basex.util.*;
import org.junit.*;

/**
 * Tests queries with path in it on collections.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Andreas Weiler
 */
public final class PathTest extends SandboxTest {
  /** Test database name. */
  private static final String INPUT = Util.className(PathTest.class);
  /** Test database name. */
  private static final String WEEK1 = Util.className(PathTest.class) + '2';
  /** Test database name. */
  private static final String WEEK2 = Util.className(PathTest.class) + '3';
  /** Test file. */
  private static final String INPUTF = "src/test/resources/input.xml";
  /** Test file. */
  private static final String WEEK = "src/test/resources/week.zip";

  /**
   * Creates initial databases.
   * @throws BaseXException exception
   */
  @BeforeClass
  public static void before() throws BaseXException {
    new CreateDB(INPUT).execute(context);
    new Add("input", INPUTF).execute(context);
    new Add("input2", INPUTF).execute(context);
    new CreateDB(WEEK1, WEEK).execute(context);
    new CreateDB(WEEK2, WEEK).execute(context);
    new Close().execute(context);
  }

  /**
   * Drops the initial databases.
   * @throws BaseXException exception
   */
  @AfterClass
  public static void after() throws BaseXException {
    new DropDB(INPUT).execute(context);
    new DropDB(WEEK1).execute(context);
    new DropDB(WEEK2).execute(context);
  }

  /**
   * Checks the number of documents under the specified path.
   * @throws Exception exception
   */
  @Test
  public void documentTestInput() throws Exception {
    final String count = "count(collection('" + INPUT + "/input'))";
    final QueryProcessor qp = new QueryProcessor(count, context);
    assertEquals(1, Integer.parseInt(qp.execute().toString()));
    qp.close();
  }

  /**
   * Checks the number of documents under the specified path.
   * @throws Exception exception
   */
  @Test
  public void documentTestWeek() throws Exception {
    final String count = "count(collection('" + WEEK1 + "/week/monday'))";
    final QueryProcessor qp = new QueryProcessor(count, context);
    assertEquals(3, Integer.parseInt(qp.execute().toString()));
    qp.close();
  }

  /**
   * Checks the results of the query with index access.
   * @throws Exception exception
   */
  @Test
  public void withIndexTest() throws Exception {
    weekTest();
    weekTest2();
  }

  /**
   * Checks the results of the query without index access.
   * @throws Exception exception
   */
  @Test
  public void withoutIndexTest() throws Exception {
    new Open(WEEK1).execute(context);
    new DropIndex("text").execute(context);
    new Open(WEEK2).execute(context);
    new DropIndex("text").execute(context);
    weekTest();
    weekTest2();
  }

  /** Checks the results of the queries with the db week.
   * @throws Exception exception
   */
  static void weekTest() throws Exception {
    final String count = "count(collection('" + WEEK1 +
      "/week/monday')/root/monday/text[text() = 'text'])";
    final QueryProcessor qp = new QueryProcessor(count, context);
    assertEquals(3, Integer.parseInt(qp.execute().toString()));
    qp.close();
    // cross-check
    final String count2 = "count(collection('" + WEEK1 +
      "/week')/root/monday/text[text() = 'text'])";
    final QueryProcessor qp2 = new QueryProcessor(count2, context);
    assertEquals(4, Integer.parseInt(qp2.execute().toString()));
    qp2.close();
  }

  /** Checks the results of the queries with the db week.
   * @throws Exception exception
   */
  static void weekTest2() throws Exception {
    final String count = "count(collection('" + WEEK1 +
      "/week/monday')/root/monday/text[text() = 'text'])," +
      " count(collection('" + WEEK2 +
      "/week/tuesday')/root/monday/text[text() = 'text']) ";
    final QueryProcessor qp = new QueryProcessor(count, context);
    final String result = qp.execute().toString();
    assertEquals("3 1", result);
    qp.close();
  }
}
