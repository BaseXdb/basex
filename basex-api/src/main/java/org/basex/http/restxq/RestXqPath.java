package org.basex.http.restxq;

import java.util.*;

import org.basex.http.*;
import org.basex.http.web.*;
import org.basex.query.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * This class represents the path of a RESTXQ function.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class RestXqPath extends WebPath implements Comparable<RestXqPath> {
  /** Path matcher. */
  private final RestXqPathMatcher matcher;

  /**
   * Constructor.
   * @param path path
   * @param ii input info
   * @throws QueryException query exception
   */
  RestXqPath(final String path, final InputInfo ii) throws QueryException {
    super(path);
    matcher = RestXqPathMatcher.parse(path, ii);
  }

  /**
   * Checks if the path matches the HTTP request.
   * @param conn HTTP connection
   * @return result of check
   */
  boolean matches(final HTTPConnection conn) {
    return matcher.matches(conn.path());
  }

  /**
   * Returns the names of the template variables.
   * @return list of qualified variable names
   */
  List<QNm> varNames() {
    return matcher.varNames;
  }

  /**
   * Gets the variable values for the given HTTP context path.
   * @param conn HTTP connection
   * @return map with variable values
   */
  QNmMap<String> values(final HTTPConnection conn) {
    return matcher.values(conn.path());
  }

  /**
   * Checks if the specified path segment is a template.
   * @param s offset of segment
   * @return result of check
   */
  private boolean isTemplate(final int s) {
    return matcher.varsPos.testBit(s);
  }

  @Override
  public int compareTo(final RestXqPath rxp) {
    // compare number of path segments: path with less segments is less specific
    final int sl = matcher.segments, d = rxp.matcher.segments - sl;
    if(d != 0) return d;

    // look for templates: segment with template is less specific
    for(int s = 0; s < sl; s++) {
      final boolean t1 = isTemplate(s), t2 = rxp.isTemplate(s);
      if(t1 != t2) return t1 ? 1 : -1;
    }

    // identical specifity
    return 0;
  }
}
