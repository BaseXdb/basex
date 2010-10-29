package org.basex.test.storage;

import static org.junit.Assert.*;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.XQuery;
import org.basex.util.Util;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the ID -> PRE mapping facility.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Leo Woerteler
 */
public class LogListTest {
  /** Database context. */
  private static Context context;

  /** Test document. */
  private static final String DOC = "<a><b><c/></b><d/></a>";

  /*
   * Tests if the LogList is activated.
   * [LW] removed to work both for default and log list
   * @throws Exception exception
  @Test
  public final void activated() throws Exception {
    final Field on = Data.class.getDeclaredField("IDPREMAPON");
    on.setAccessible(true);
    //
    //assertTrue("LogList isn't activated", on.getBoolean(null));
  }
   */

  /**
   * Tests if inserts are correctly reflected in the mapping.
   * @throws Exception exception
   */
  @Test
  public final void insert() throws Exception {
    test("insert node <e/> as first into //b",
        "insert node <f/> as first into //c");
  }

  /** Creates the database context. */
  @BeforeClass
  public static void start() {
    context = new Context();
  }

  /**
   * Creates all test databases.
   * @throws BaseXException database exception
   */
  @Before
  public void startTest() throws BaseXException {
    new CreateDB(Util.name(this), DOC).execute(context);
  }

  /**
   * Removes test databases and closes the database context.
   * @throws BaseXException database exception
   */
  @AfterClass
  public static void finish() throws BaseXException {
    new DropDB(Util.name(LogListTest.class)).execute(context);
    context.close();
  }

  /**
   * Executes the given XQueries on the current DB and checks the mapping.
   * @param qs queries
   * @throws BaseXException database exception
   */
  private static void test(final String... qs) throws BaseXException {
    for(final String q : qs) new XQuery(q).execute(context);
    checkMapping();
  }

  /**
   * Checks if for all PRE values existing in the document pre(id(PRE)) == PRE
   * holds.
   */
  private static void checkMapping() {
    for(int pre = 0; pre < context.data.meta.size; ++pre) {
      final int id = context.data.id(pre);
      assertEquals("Wrong PRE value for ID " + id + ":", pre,
          context.data.pre(id));
    }
  }
}
