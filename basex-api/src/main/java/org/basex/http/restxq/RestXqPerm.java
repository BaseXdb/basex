package org.basex.http.restxq;

import static org.basex.http.web.WebText.*;

import org.basex.http.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.http.*;

/**
 * RESTXQ permissions.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class RestXqPerm implements Comparable<RestXqPerm> {
  /** Permission path (with heading and trailing slash). */
  private final String path;
  /** Permission variable (can be {@code null}). */
  final QNm var;

  /**
   * Sets a permission path.
   * @param path path
   * @param var name of variable (can be {@code null})
   */
  RestXqPerm(final String path, final QNm var) {
    this.path = ('/' + path + '/').replaceAll("^/+|/+$", "/");
    this.var = var;
  }

  /**
   * Returns a map with permission information.
   * @param func function for which permission should be checked
   * @param conn HTTP connection
   * @return permission information
   * @throws QueryException query exception
   */
  static XQMap map(final RestXqFunction func, final HTTPConnection conn) throws QueryException {
    final MapBuilder mb = new MapBuilder();
    mb.put(ALLOW, StrSeq.get(func.allows.toArray()));
    mb.put(PATH, conn.path());
    mb.put(METHOD, conn.method);
    mb.put(AUTHORIZATION, conn.request.getHeader(HttpText.AUTHORIZATION));
    return mb.finish();
  }

  /**
   * Checks if the path matches the HTTP request.
   * @param conn HTTP connection
   * @return result of check
   */
  boolean matches(final HTTPConnection conn) {
    final String p = conn.path();
    return (Strings.endsWith(p, '/') ? p : p + '/').startsWith(path);
  }

  @Override
  public int compareTo(final RestXqPerm rxp) {
    return path.length() - rxp.path.length();
  }
}
