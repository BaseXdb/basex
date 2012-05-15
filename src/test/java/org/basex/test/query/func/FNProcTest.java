package org.basex.test.query.func;

import static org.basex.query.func.Function.*;

import org.basex.test.query.*;
import org.junit.*;

/**
 * This class tests the XQuery process functions prefixed with "proc".
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNProcTest extends AdvancedQueryTest {
  /**
   * Test method for the proc:system() function.
   */
  @Test
  public void procSystem() {
    check(_PROC_SYSTEM);
    query(_PROC_SYSTEM.args("java", "-version"), "");
  }

  /**
   * Test method for the proc:execute() function.
   */
  @Test
  public void procExecute() {
    check(_PROC_EXECUTE);
    query("count(" + _PROC_EXECUTE.args("java", "-version") + "/*)", "3");
  }
}
