package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import java.util.*;

import org.basex.core.cmd.*;
import org.basex.query.*;
import org.basex.server.*;
import org.junit.*;
import org.junit.Test;

/**
 * This class tests the functions of the Admin Module.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class AdminModuleTest extends AdvancedQueryTest {
  /**
   * Init method.
   */
  @BeforeClass
  public static void init() {
    execute(new CreateDB(NAME));
  }

  /**
   * Init method.
   */
  @AfterClass
  public static void finish() {
    execute(new DropDB(NAME));
  }

  /** Test method. */
  @Test
  public void sessions() {
    // can only be tested in client/server mode
    query(_ADMIN_SESSIONS.args(), "");
  }

  /** Test method. */
  @Test
  public void logs() {
    // no logging data exists in the sandbox
    query(_ADMIN_LOGS.args(), "");
    error(_ADMIN_LOGS.args("2001-01-01"), WHICHRES_X);
  }

  /** Test method. */
  @Test
  public void deleteLogs() {
    // no logging data exists in the sandbox
    error(_ADMIN_DELETE_LOGS.args(Log.name(new Date())), BXAD_TODAY);
    error(_ADMIN_DELETE_LOGS.args("2001-01-01"), WHICHRES_X);
  }
}
