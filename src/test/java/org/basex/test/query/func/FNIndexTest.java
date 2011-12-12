package org.basex.test.query.func;

import static org.basex.query.func.Function.*;

import org.basex.core.BaseXException;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.io.IO;
import org.basex.io.IOFile;
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
  private static final String FILE = "etc/test/input.xml";
  /** Test folder. */
  private static final String FLDR = "etc/test/dir";

  static {
    int fc = 0;
    for(final IOFile c : new IOFile(FLDR).children()) {
      if(c.name().endsWith(IO.XMLSUFFIX)) ++fc;
    }
  }

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
   * Test method for the db:details() function.
   */
  @Test
  public void ixFacet() {
    check(_INDEX_FACETS);
    
    final String tree = _INDEX_FACETS.args(DB, 1);
    query(tree + "/html/head/@count/data()", 1);
    query(tree + "/html/head/title/@type/data()", "categorical");
    query(tree + "//li/@count/data()", 2);
    
    final String flat = _INDEX_FACETS.args(DB, 2);
    query(flat + "/title/@count/data()", 1);
    query(flat + "/title/@type/data()", "categorical");
    query(flat + "//li/@count/data()", 2);
  }
}
