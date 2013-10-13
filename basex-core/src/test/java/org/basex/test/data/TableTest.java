package org.basex.test.data;

import static org.junit.Assert.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.test.*;
import org.junit.*;

/**
 * This class tests the stability of the data storage.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Dimitar Popov
 */
public final class TableTest extends SandboxTest {
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
    new CreateDB(NAME, DBFILE).execute(context);
    tbl = context.data().meta.dbfile(DataText.DATATBL);
  }

  /**
   * Drops the JUnitTest database.
   * @throws BaseXException exception during drop of database
   */
  @After
  public void tearDown() throws BaseXException {
    new DropDB(NAME).execute(context);
  }

  /**
   * Test if the size of the table remains constant after insertion, deletion,
   * and re-insertion of the same record.
   * @throws BaseXException exception during query execution
   */
  @Test
  public void tableSize() throws BaseXException {
    final long s = tbl.length();

    final String n = new XQuery(SELECT).execute(context);
    new XQuery(DELETE).execute(context);
    new XQuery(String.format(INSERT, n)).execute(context);
    new Close().execute(context);

    assertEquals("Database size changed: ", s, tbl.length());
  }
}
