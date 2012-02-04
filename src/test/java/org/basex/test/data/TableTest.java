package org.basex.test.data;

import static org.junit.Assert.*;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.XQuery;
import org.basex.data.DataText;
import org.basex.io.IOFile;
import org.basex.util.Util;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This class tests the stability of the data storage.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Dimitar Popov
 */
public final class TableTest {
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

  /** Context. */
  private Context ctx;
  /** Table file. */
  private IOFile tbl;

  /**
   * Set up method.
   * @throws BaseXException exception during creation of database
   */
  @Before
  public void setUp() throws BaseXException {
    ctx = new Context();
    new CreateDB(DB, DBFILE).execute(ctx);
    tbl = ctx.data().meta.dbfile(DataText.DATATBL);
  }

  /**
   * Drops the JUnitTest database.
   * @throws BaseXException exception during drop of database
   */
  @After
  public void tearDown() throws BaseXException {
    new DropDB(DB).execute(ctx);
  }

  /**
   * Test if the size of the table remains constant after insertion, deletion,
   * and re-insertion of the same record.
   * @throws BaseXException exception during query execution
   */
  @Test
  public void tableSize() throws BaseXException {
    final long s = tbl.length();

    final String n = new XQuery(SELECT).execute(ctx);
    new XQuery(DELETE).execute(ctx);
    new XQuery(String.format(INSERT, n)).execute(ctx);
    new Close().execute(ctx);

    assertEquals("Database size changed: ", s, tbl.length());
  }
}
