package org.basex.http.restxq;

import static org.basex.http.util.WebText.*;

import org.basex.http.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;

/**
 * RESTXQ permissions.
 *
 * @author BaseX Team 2005-18, BSD License
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
  Map map(final RestXqFunction func, final HTTPConnection conn) throws QueryException {
    Map map = Map.EMPTY;
    map = map.put(Str.get(ALLOW), StrSeq.get(func.allows), null);
    map = map.put(Str.get(PATH), Str.get(conn.req.getPathInfo()), null);
    map = map.put(Str.get(METHOD), Str.get(conn.method), null);
    return map;
  }

  /**
   * Checks if the path matches the HTTP request.
   * @param conn HTTP connection
   * @return result of check
   */
  boolean matches(final HTTPConnection conn) {
    final String p = conn.path();
    return (p.endsWith("/") ? p : p + '/').startsWith(path);
  }

  @Override
  public int compareTo(final RestXqPerm rxp) {
    return path.length() - rxp.path.length();
  }
}
