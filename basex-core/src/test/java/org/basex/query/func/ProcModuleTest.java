package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.junit.*;

/**
 * This class tests the functions of the Process Module.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class ProcModuleTest extends AdvancedQueryTest {
  /** Test method. */
  @Test
  public void system() {
    query(_PROC_SYSTEM.args("java", "-version"), "");
    error(_PROC_SYSTEM.args("java", "-version", "xx"), BXPR_ENC_X);
  }

  /** Test method. */
  @Test
  public void execute() {
    query("count(" + _PROC_EXECUTE.args("java", "-version") + "/*)", "3");
  }
}
