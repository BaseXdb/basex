package org.basex.http.restxq;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.core.*;
import org.basex.http.*;
import org.junit.jupiter.api.*;

/**
 * This test contains RESTXQ modules that cannot be parsed.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RestXqModuleTest extends RestXqTest {
  /**
   * Endpoints of intact modules are still served.
   * @throws Exception exception
   */
  @Test public void brokenModule() throws Exception {
    register("declare %R:path('ok') function m:f() { 'x' };");
    add("declare %R:path('broken') function m:g($unbound) { $unbound };");

    assertEquals("x", get(200, "ok"));
    // the parse error is reported for requests that match no function
    assertContains(get(500, "broken"), "No binding defined for $unbound.");
    assertContains(get(500, "unknown"), "No binding defined for $unbound.");
  }

  /**
   * Parse errors are hidden if {@link StaticOptions#RESTXQERRORS} is disabled.
   * @throws Exception exception
   */
  @Test public void hiddenErrors() throws Exception {
    register("declare %R:path('ok') function m:f() { 'x' };");
    add("declare %R:path('broken') function m:g($unbound) { $unbound };");

    final StaticOptions sopts = HTTPContext.get().context().soptions;
    sopts.set(StaticOptions.RESTXQERRORS, false);
    try {
      assertEquals("x", get(200, "ok"));
      assertEquals("Service not found.", get(404, "broken"));
    } finally {
      sopts.set(StaticOptions.RESTXQERRORS, true);
    }
  }
}
