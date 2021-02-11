package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Unit Module.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class UnitModuleTest extends SandboxTest {
  /** Test method. */
  @Test public void assrt() {
    final Function func = _UNIT_ASSERT;
    query(func.args(1), "");
    query(func.args(" (<a/>,<b/>)"), "");
    error(func.args(" ()"), UNIT_FAIL);
    error(func.args(" ()", "X"), UNIT_FAIL_X);
  }

  /** Test method. */
  @Test public void assertEquals() {
    final Function func = _UNIT_ASSERT_EQUALS;
    query(func.args(1, 1), "");
    error(func.args(1, 2), UNIT_FAIL_X_X_X);
  }

  /** Test method. */
  @Test public void faill() {
    final Function func = _UNIT_FAIL;
    error(func.args(1), UNIT_FAIL_X);
  }
}
