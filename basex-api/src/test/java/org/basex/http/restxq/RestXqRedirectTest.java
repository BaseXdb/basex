package org.basex.http.restxq;

import org.junit.jupiter.api.*;

/**
 * This test contains RESTXQ redirections.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class RestXqRedirectTest extends RestXqTest {
  /**
   * Redirect request.
   * @throws Exception exception */
  @Test public void redirect() throws Exception {
    final String f =
      "declare %R:path('')  function m:a() { web:redirect('a') };" +
      "declare %R:path('a') function m:b() { 'R' };";
    get(f, "", "R");
  }

  /**
   * Forward request.
   * @throws Exception exception */
  @Test public void forward() throws Exception {
    final String f =
      "declare %R:path('')  function m:a() { web:forward('a') };" +
      "declare %R:path('a') function m:b() { 'F' };";
    get(f, "", "F");
  }
}
