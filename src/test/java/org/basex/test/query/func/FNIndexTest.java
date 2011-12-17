package org.basex.test.query.func;

import static org.basex.query.func.Function.*;

import org.basex.core.BaseXException;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.test.query.AdvancedQueryTest;
import org.basex.util.Util;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

/**
 * This class tests the XQuery index functions prefixed with "ix".
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Andreas Weiler
 */
public final class FNIndexTest extends AdvancedQueryTest {
  /** Name of test database. */
  private static final String DB = Util.name(FNIndexTest.class);
  /** Test file. */
  private static final String FILE = "src/test/resources/input.xml";

  /**
   * Initializes a test.
   * @throws BaseXException database exception
   */
  @Before
  public void initTest() throws BaseXException {
    new CreateDB(DB, FILE).execute(CONTEXT);
  }

  /**
   * Finishes the code.
   * @throws BaseXException database exception
   */
  @AfterClass
  public static void finish() throws BaseXException {
    new DropDB(DB).execute(CONTEXT);
  }

  /**
   * Test method for the indexFacets() function.
   */
  @Test
  public void indexFacets() {
    check(_INDEX_FACETS);

    final String tree = _INDEX_FACETS.args(DB);
    query(tree + "//element[@name='head']/@count/data()", 1);
    query(tree + "//element[@name='title']/text/@type/data()", "category");
    query(tree + "//element[@name='li']/text/@count/data()", 2);

    final String flat = _INDEX_FACETS.args(DB, "flat");
    query(flat + "//element[@name='title']/@count/data()", 1);
    query(flat + "//element[@name='title']/@type/data()", "category");
    query(flat + "//element[@name='li']/@count/data()", 2);
  }
}