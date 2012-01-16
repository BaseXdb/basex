package org.basex.test.data;

import org.basex.core.BaseXException;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.data.DiskData;
import org.basex.util.Util;
import org.junit.After;
import org.junit.Before;

/**
 * Test index updates when using disk storage ({@link DiskData}).
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Dimitar Popov
 */
public class DiskDataTest extends MemDataTest {
  /** Test database name. */
  private final String dbname = Util.name(DiskDataTest.class);

  @Override
  @Before
  public void setUp() throws BaseXException {
    new CreateDB(dbname, XMLSTR).execute(CTX);
  }

  /**
   * Clean up method; executed after each test; drops the database.
   * @throws BaseXException the database cannot be dropped
   */
  @After
  public void cleanUp() throws BaseXException {
    new DropDB(dbname).execute(CTX);
  }
}
