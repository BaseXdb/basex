package org.basex.http.dba;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.http.*;
import org.junit.jupiter.api.*;

/**
 * Base class for DBA tests: deploys the DBA into the sandbox webapp and holds a login session.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class DBATest extends WebappTest {
  /**
   * Deploys the DBA, starts the server and logs in.
   * @throws Exception exception
   */
  @BeforeAll public static void startDBA() throws Exception {
    init("dba");
    final String page = post("login", Map.of("_name", "admin", "_pass", NAME));
    assertFalse(page.contains("_pass"), "DBA login failed");
  }
}
