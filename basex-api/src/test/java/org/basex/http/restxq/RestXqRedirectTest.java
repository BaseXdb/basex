package org.basex.http.restxq;

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
   * Redirect to an absolute path from an encoded path.
   * @throws Exception exception */
  @Test public void redirectEncoded() throws Exception {
    get("R", "declare %R:path('a b') function m:a() { web:redirect('/b') };" +
            "declare %R:path('b') function m:b() { 'R' };", "a%20b");
  }

  /**
   * Base URI of an encoded path.
   * @throws Exception exception */
  @Test public void baseUriEncoded() throws Exception {
    get("true", "declare %R:path('a b') function m:a() {" +
            "  rest:base-uri() = rest:uri() => replace('/a%20b$', '') };", "a%20b");
  }
}
