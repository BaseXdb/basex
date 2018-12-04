package org.basex.data;

import org.basex.core.cmd.*;
import org.basex.util.*;
import org.junit.*;

/**
 * Test index updates when using disk storage ({@link DiskData}).
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Dimitar Popov
 */
public final class DiskDataTest extends MemDataTest {
  /** Test database name. */
  private final String dbName = Util.className(DiskDataTest.class);

  @Override
  @Before public void setUp() {
    execute(new CreateDB(dbName, XMLSTR));
  }

  /**
   * Clean up method; executed after each test; drops the database.
   */
  @After public void cleanUp() {
    execute(new DropDB(dbName));
  }
}
