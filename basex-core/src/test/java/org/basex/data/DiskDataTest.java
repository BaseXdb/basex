package org.basex.data;

import org.basex.core.cmd.*;
import org.junit.jupiter.api.*;

/**
 * Test index updates when using disk storage ({@link DiskData}).
 *
 * @author BaseX Team, BSD License
 * @author Dimitar Popov
 */
public final class DiskDataTest extends MemDataTest {
  @Override
  @BeforeEach public void setUp() {
    execute(new CreateDB(NAME, XMLSTR));
  }

  /**
   * Clean up method; executed after each test; drops the database.
   */
  @AfterEach public void cleanUp() {
    execute(new DropDB(NAME));
  }
}
