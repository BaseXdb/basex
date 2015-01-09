package org.basex.build;

import static org.junit.Assert.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.MainOptions.MainParser;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.util.*;
import org.junit.*;
import org.junit.Test;

/**
 * Tests queries with path in it on collections.
 *
 * @author BaseX Team 2005-15, BSD License
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
    try(final QueryProcessor qp = new QueryProcessor(count, context)) {
      assertEquals(1, Integer.parseInt(qp.execute().toString()));
    }
  }

  /**
   * Checks the number of documents under the specified path.
   * @throws Exception exception
   */
  @Test
  public void documentTestWeek() throws Exception {
    final String count = "count(collection('" + WEEK1 + "/week/monday'))";
    try(final QueryProcessor qp = new QueryProcessor(count, context)) {
      assertEquals(3, Integer.parseInt(qp.execute().toString()));
    }
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
   * #905: Ensure that parser options will not affect doc() and collection().
   * May be moved to a separate test class in future.
   * @throws Exception exception
   */
  @Test
  public void docParsing() throws Exception {
    final IOFile path = new IOFile(sandbox(), "doc.xml");
    path.write(Token.token("<a/>"));
    context.options.set(MainOptions.PARSER, MainParser.JSON);
    try(final QueryProcessor qp = new QueryProcessor("doc('" + path + "')", context)) {
      assertEquals("<a/>", qp.execute().toString());
    }
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
  private static void weekTest() throws Exception {
    final String count = "count(collection('" + WEEK1 +
      "/week/monday')/root/monday/text[text() = 'text'])";
    try(final QueryProcessor qp = new QueryProcessor(count, context)) {
      assertEquals(3, Integer.parseInt(qp.execute().toString()));
    }
    // cross-check
    final String count2 = "count(collection('" + WEEK1 +
      "/week')/root/monday/text[text() = 'text'])";
    try(final QueryProcessor qp2 = new QueryProcessor(count2, context)) {
      assertEquals(4, Integer.parseInt(qp2.execute().toString()));
    }
  }

  /** Checks the results of the queries with the db week.
   * @throws Exception exception
   */
  private static void weekTest2() throws Exception {
    final String count = "count(collection('" + WEEK1 +
      "/week/monday')/root/monday/text[text() = 'text'])," +
      " count(collection('" + WEEK2 +
      "/week/tuesday')/root/monday/text[text() = 'text']) ";
    try(final QueryProcessor qp = new QueryProcessor(count, context)) {
      assertEquals("3 1", qp.execute().toString());
    }
  }
}
