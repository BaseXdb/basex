package org.basex.http.web;

import java.util.*;

import org.basex.query.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * This class represents the path template of a web function.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WebPath implements Comparable<WebPath> {
  /** Path. */
  private final String path;
  /** Path matcher. */
  private final WebPathMatcher matcher;

  /**
   * Constructor.
   * @param path path template
   * @param info input info (can be {@code null})
   * @param err error code to raise on a malformed template (RESTXQ or WebSocket)
   * @throws QueryException query exception
   */
  public WebPath(final String path, final InputInfo info, final QueryError err)
      throws QueryException {
    this.path = path;
    matcher = WebPathMatcher.parse(path, info, err);
  }

  /**
   * Checks if the path template matches the specified path.
   * @param pth path to compare to
   * @return result of check
   */
  public boolean matches(final String pth) {
    return matcher.matches(pth);
  }

  /**
   * Gets the variable values for the specified path.
   * @param pth path
   * @return map with variable values
   */
  public QNmMap<String> values(final String pth) {
    return matcher.values(pth);
  }

  /**
   * Returns the names of the template variables.
   * @return list of qualified variable names
   */
  public List<QNm> varNames() {
    return matcher.varNames;
  }

  @Override
  public int compareTo(final WebPath wp) {
    return matcher.compareTo(wp.matcher);
  }

  @Override
  public String toString() {
    return path;
  }
}
