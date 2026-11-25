package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.*;
import org.basex.api.client.*;
import org.basex.core.cmd.*;
import org.basex.core.users.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * This class tests the functions of the Admin Module.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class AdminModuleTest extends SandboxTest {
  /** Initialize tests. */
  @BeforeAll public static void init() {
    execute(new CreateDB(NAME));
  }

  /** Finalize tests. */
  @AfterAll public static void finish() {
    execute(new DropDB(NAME));
  }

  /**
   * Test method.
   * @throws IOException I/O exception
   */
  @Test public void closeSessions() throws IOException {
    final Function func = _ADMIN_CLOSE_SESSIONS;
    final String count = _ADMIN_SESSIONS.args() + " => count()";
    final BaseXServer server = createServer();
    try(ClientSession cs1 = createClient(); ClientSession cs2 = createClient()) {
      // close sessions by user name; own session is skipped
      assertEquals("", cs1.query(func.args(UserText.ADMIN)).execute());
      assertEquals("1", cs1.query(count).execute());
      assertThrows(IOException.class, () -> cs2.query(count).execute());

      // close sessions by address
      try(ClientSession cs3 = createClient()) {
        cs3.query(func.args("unknown")).execute();
        assertEquals("2", cs3.query(count).execute());
        cs3.query(_ADMIN_SESSIONS.args() + "/@address ! " + func.args(" .")).execute();
        assertEquals("1", cs3.query(count).execute());
      }
    } finally {
      stopServer(server);
    }
  }

  /** Test method. */
  @Test public void deleteLogs() {
    final Function func = _ADMIN_DELETE_LOGS;
    // no logging data exists in the sandbox
    error(func.args("2001-01-01"), ADMIN_DELETE_X);
  }

  /** Test method. */
  @Test public void logs() {
    final Function func = _ADMIN_LOGS;
    // no logging data exists in the sandbox
    query(func.args(), "");
    error(func.args("2001-01-01"), WHICHRES_X);
  }

  /** Test method. */
  @Test public void sessions() {
    final Function func = _ADMIN_SESSIONS;
    // can only be tested in client/server mode
    query(func.args(), "");
  }

  /** Test method. */
  @Test public void writeLog() {
    final Function func = _ADMIN_WRITE_LOG;
    // no logging data exists in the sandbox
    error(func.args("", "X "), ADMIN_TYPE_X);
    error(func.args("", "X\r"), ADMIN_TYPE_X);
  }
}
