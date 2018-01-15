package org.basex.http.restxq;

import static org.basex.http.restxq.RestXqText.*;

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
  /** Permission path. */
  final String path;
  /** Permission variable (can be {@code null}). */
  final QNm var;

  /**
   * Sets a permission path.
   * @param path path
   * @param var name of variable (can be {@code null})
   */
  RestXqPerm(final String path, final QNm var) {
    this.path = path.startsWith("/") ? path : '/' + path;
    this.var = var;
  }

  /**
   * Returns a map with permission information.
   * @param func function for which permission should be checked
   * @return permission information
   * @throws QueryException query exception
   */
  Map map(final RestXqFunction func) throws QueryException {
    return Map.EMPTY.put(Str.get(ALLOW), StrSeq.get(func.allows), null);
  }

  /**
   * Checks if the path matches the HTTP request.
   * @param conn HTTP connection
   * @return result of check
   */
  boolean matches(final HTTPConnection conn) {
    return conn.path().startsWith(path);
  }

  @Override
  public int compareTo(final RestXqPerm rxp) {
    return path.length() - rxp.path.length();
  }
}
