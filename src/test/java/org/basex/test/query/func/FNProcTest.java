package org.basex.test.query.func;

import static org.basex.query.func.Function.*;

import org.basex.query.util.*;
import org.basex.test.query.*;
import org.junit.*;

/**
 * This class tests the XQuery process functions prefixed with "proc".
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNProcTest extends AdvancedQueryTest {
  /** Test method. */
  @Test
  public void system() {
    query(_PROC_SYSTEM.args("java", "-version"), "");
    error(_PROC_SYSTEM.args("java", "-version", "xx"), Err.BXPR_ENC);
  }

  /** Test method. */
  @Test
  public void execute() {
    query("count(" + _PROC_EXECUTE.args("java", "-version") + "/*)", "3");
  }
}
