package org.basex.http.restxq;

import org.basex.http.*;
import org.basex.query.QueryException;
import org.basex.query.value.item.QNm;
import org.basex.util.InputInfo;

import java.util.List;
import java.util.Map;

/**
 * This class represents the path of a RESTXQ function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class RestXqPath implements Comparable<RestXqPath> {
  private final String path;
  /** Path matcher. */
  private final PathMatcher matcher;

  /**
   * Constructor.
   * @param path path
   * @param info input info
   */
  RestXqPath(final String path, final InputInfo info) throws QueryException {
    this.path = path;
    this.matcher = PathMatcher.parse(path, info);
  }

  /**
   * Checks if the path matches the HTTP request.
   * @param http http context
   * @return result of check
   */
  boolean matches(final HTTPContext http) {
    return matcher.matches(http.req.getPathInfo());
  }

  /**
   * Get template variable names.
   * @return list of qualified variable names
   */
  List<QNm> getVariableNames() {
    return matcher.variableNames;
  }

  /**
   * Get variable values for the given HTTP context path.
   * @param http HTTP context
   * @return map with variable values
   */
  Map<QNm, String> getVariableValues(final HTTPContext http) {
    return matcher.getVariableValues(http.req.getPathInfo());
  }

  /**
   * Checks if the specified path segment is a template.
   * @param s offset of segment
   * @return result of check
   */
  private boolean isTemplate(final int s) {
    return matcher.variablePositions.testBit(s);
  }

  @Override
  public int compareTo(final RestXqPath rxs) {
    final int d = matcher.segmentCount - rxs.matcher.segmentCount;
    if(d != 0) return d;
    for(int s = 0; s < matcher.segmentCount; s++) {
      final boolean wc1 = isTemplate(s), wc2 = rxs.isTemplate(s);
      if(wc1 != wc2) return wc1 ? 1 : -1;
    }
    return 0;
  }

  @Override
  public String toString() {
    return path;
  }
}
