package org.basex.test.collections;

import static org.junit.Assert.*;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.Add;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Open;
import org.basex.query.QueryProcessor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests queries with path in it on collections.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Andreas Weiler
 */
public class PathTest {
  
  /** Database context. */
  private static final Context CTX = new Context();
  /** Test database name. */
  private static final String INPUT = PathTest.class.getSimpleName();
  /** Test database name. */
  private static final String WEEK1 = PathTest.class.getSimpleName() + "2";
  /** Test database name. */
  private static final String WEEK2 = PathTest.class.getSimpleName() + "3";
  /** Test file. */
  private static final String INPUTF = "etc/xml/input.xml";
  /** Test file. */
  private static final String WEEK = "etc/xml/week.zip";
  
  /**
   * Creates initial databases.
   * @throws BaseXException exception
   */
  @BeforeClass
  public static void before() throws BaseXException {
    new CreateDB(INPUT).execute(CTX);
    new CreateDB(WEEK1, WEEK).execute(CTX);
    new CreateDB(WEEK2, WEEK).execute(CTX);
    new Open(INPUT).execute(CTX);
    new Add(INPUTF, "input").execute(CTX);
    new Add(INPUTF, "input2").execute(CTX);
  }
  
  /**
   * Drops the initial databases.
   * @throws BaseXException exception
   */
  @AfterClass
  public static void after() throws BaseXException {
    new DropDB(INPUT).execute(CTX);
    new DropDB(WEEK1).execute(CTX);
    new DropDB(WEEK2).execute(CTX);
    CTX.close();
  }
  
  /**
   * Checks the results of the query without index access.
   * @throws BaseXException exception
   * @throws Exception exception
   */
  @Test
  public void withoutIndexTestInput() throws Exception {
    String count = "count(collection('" + INPUT + "/input'))";
    final QueryProcessor qp = new QueryProcessor(count, CTX);
    assertEquals(1, Integer.parseInt(qp.execute().toString()));
    qp.close();
  }
  
  /**
   * Checks the results of the query without index access.
   * @throws BaseXException exception
   * @throws Exception exception
   */
  @Test
  public void withoutIndexTestWeek() throws Exception {
    String count = "count(collection('" + WEEK1 + "/monday'))";
    final QueryProcessor qp = new QueryProcessor(count, CTX);
    assertEquals(3, Integer.parseInt(qp.execute().toString()));
    qp.close();
  }
  
  /**
   * Checks the results of the query with index access.
   */
  @Test
  public void withIndexTest() {
    
  }
  
  /**
   * Checks the results of the query with access of two collections.
   */
  @Test
  public void with2CollectionsTest() {
    
  }
}
