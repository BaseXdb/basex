package org.basex.examples.perf;

import static org.junit.Assert.*;

import java.io.File;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.XQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Storage stress tests.
 * @author BaseX Team 2005-11, BSD License
 * @author Dimitar Popov
 */
public final class StorageStressTest {

  /** Database name. */
  private static final String DBNAME = "factbook";
  /** Database XML file. */
  private static final String DBFILE = "etc/xml/" + DBNAME + ".xml";
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
  private File tbl;

  /**
   * Set up method.
   * @throws BaseXException exception during creation of database
   */
  @Before
  public void setUp() throws BaseXException {
    ctx = new Context();
    new CreateDB(DBNAME, DBFILE).execute(ctx);
    tbl = new File(ctx.prop.dbpath(DBNAME).toString() + "/tbl.basex");
  }

  /**
   * Drops the JUnitTest database.
   * @throws BaseXException exception during drop of database
   */
  @After
  public void tearDown() throws BaseXException {
    new DropDB(DBNAME).execute(ctx);
  }

  /**
   * Test if the size of the table remains constant after insertion, deletion,
   * and re-insertion of the same record.
   * @throws BaseXException exception during query execution
   */
  @Test
  public void testTableSize() throws BaseXException {
    final long s = tbl.length();

    final String n = new XQuery(SELECT).execute(ctx);
    new XQuery(DELETE).execute(ctx);
    new XQuery(String.format(INSERT, n)).execute(ctx);
    new Close().execute(ctx);

    assertEquals("Database size changed: ", s, tbl.length());
  }
}
