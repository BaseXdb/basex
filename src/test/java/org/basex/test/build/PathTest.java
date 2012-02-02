package org.basex.test.build;

import static org.junit.Assert.*;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.Add;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.DropIndex;
import org.basex.core.cmd.Open;
import org.basex.query.QueryProcessor;
import org.basex.util.Util;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests queries with path in it on collections.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Andreas Weiler
 */
public final class PathTest {
  /** Database context. */
  private static final Context CONTEXT = new Context();
  /** Test database name. */
  private static final String INPUT = Util.name(PathTest.class);
  /** Test database name. */
  private static final String WEEK1 = Util.name(PathTest.class) + "2";
  /** Test database name. */
  private static final String WEEK2 = Util.name(PathTest.class) + "3";
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
    new CreateDB(INPUT).execute(CONTEXT);
    new Add("input", INPUTF).execute(CONTEXT);
    new Add("input2", INPUTF).execute(CONTEXT);
    new CreateDB(WEEK1, WEEK).execute(CONTEXT);
    new CreateDB(WEEK2, WEEK).execute(CONTEXT);
    new Close().execute(CONTEXT);
  }

  /**
   * Drops the initial databases.
   * @throws BaseXException exception
   */
  @AfterClass
  public static void after() throws BaseXException {
    new DropDB(INPUT).execute(CONTEXT);
    new DropDB(WEEK1).execute(CONTEXT);
    new DropDB(WEEK2).execute(CONTEXT);
    CONTEXT.close();
  }

  /**
   * Checks the number of documents under the specified path.
   * @throws Exception exception
   */
  @Test
  public void documentTestInput() throws Exception {
    final String count = "count(collection('" + INPUT + "/input'))";
    final QueryProcessor qp = new QueryProcessor(count, CONTEXT);
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
    final QueryProcessor qp = new QueryProcessor(count, CONTEXT);
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
    new Open(WEEK1).execute(CONTEXT);
    new DropIndex("text").execute(CONTEXT);
    new Open(WEEK2).execute(CONTEXT);
    new DropIndex("text").execute(CONTEXT);
    weekTest();
    weekTest2();
  }

  /** Checks the results of the queries with the db week.
   * @throws Exception exception
   */
  void weekTest() throws Exception {
    final String count = "count(collection('" + WEEK1 +
      "/week/monday')/root/monday/text[text() = 'text'])";
    final QueryProcessor qp = new QueryProcessor(count, CONTEXT);
    assertEquals(3, Integer.parseInt(qp.execute().toString()));
    qp.close();
    // cross-check
    final String count2 = "count(collection('" + WEEK1 +
      "/week')/root/monday/text[text() = 'text'])";
    final QueryProcessor qp2 = new QueryProcessor(count2, CONTEXT);
    assertEquals(4, Integer.parseInt(qp2.execute().toString()));
    qp2.close();
  }

  /** Checks the results of the queries with the db week.
   * @throws Exception exception
   */
  void weekTest2() throws Exception {
    final String count = "count(collection('" + WEEK1 +
      "/week/monday')/root/monday/text[text() = 'text'])," +
      " count(collection('" + WEEK2 +
      "/week/tuesday')/root/monday/text[text() = 'text']) ";
    final QueryProcessor qp = new QueryProcessor(count, CONTEXT);
    final String result = qp.execute().toString();
    assertEquals("3 1", result);
    qp.close();
  }
}
