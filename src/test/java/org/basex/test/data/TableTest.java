package org.basex.test.data;

import static org.junit.Assert.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.test.*;
import org.basex.util.*;
import org.junit.*;

/**
 * This class tests the stability of the data storage.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Dimitar Popov
 */
public final class TableTest extends SandboxTest {
  /** Database name. */
  private static final String DB = Util.name(TableTest.class);
  /** Database XML file. */
  private static final String DBFILE = "src/test/resources/factbook.zip";
  /** Select Germany. */
  private static final String SELECT = "//country[@name='Germany']";
  /** Delete Germany. */
  private static final String DELETE = "delete node " + SELECT;
  /** Re-insert Germany. */
  private static final String INSERT = "insert node %1$s "
      + "after //country[@name='France']";

  /** Table file. */
  private IOFile tbl;

  /**
   * Set up method.
   * @throws BaseXException exception during creation of database
   */
  @Before
  public void setUp() throws BaseXException {
    new CreateDB(DB, DBFILE).execute(CONTEXT);
    tbl = CONTEXT.data().meta.dbfile(DataText.DATATBL);
  }

  /**
   * Drops the JUnitTest database.
   * @throws BaseXException exception during drop of database
   */
  @After
  public void tearDown() throws BaseXException {
    new DropDB(DB).execute(CONTEXT);
  }

  /**
   * Test if the size of the table remains constant after insertion, deletion,
   * and re-insertion of the same record.
   * @throws BaseXException exception during query execution
   */
  @Test
  public void tableSize() throws BaseXException {
    final long s = tbl.length();

    final String n = new XQuery(SELECT).execute(CONTEXT);
    new XQuery(DELETE).execute(CONTEXT);
    new XQuery(String.format(INSERT, n)).execute(CONTEXT);
    new Close().execute(CONTEXT);

    assertEquals("Database size changed: ", s, tbl.length());
  }
}
