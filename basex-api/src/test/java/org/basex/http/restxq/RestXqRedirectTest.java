package org.basex.http.restxq;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

/**
 * This test contains RESTXQ redirections.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class RestXqRedirectTest extends RestXqTest {
  /**
   * Redirect request.
   * @throws Exception exception */
  @Test public void redirect() throws Exception {
    get("R", "declare %R:path('')  function m:a() { web:redirect('a') };" +
            "declare %R:path('a') function m:b() { 'R' };", "");
  }

  /**
   * Forward request.
   * @throws Exception exception */
  @Test public void forward() throws Exception {
    get("F", "declare %R:path('')  function m:a() { web:forward('a') };" +
            "declare %R:path('a') function m:b() { 'F' };", "");
  }

  /**
   * Forward to an invalid location: must raise a RESTXQ error, not leak an unchecked exception.
   * @throws Exception exception */
  @Test public void forwardInvalid() throws Exception {
    for(final String location : new String[] { "http://localhost/a", "//host/a", "a//b", "a#f",
        "a b", "a%2", "a%2Fb", "a%00b", "a|b", "a{b}" }) {
      register("declare %R:path('') function m:a() { web:forward('" + location + "') };");
      final String body = get(500, "");
      assertTrue(body.contains("basex:restxq"), location + ": " + body);
      assertFalse(body.contains("Unexpected error"), location + ": " + body);
    }
  }
}
