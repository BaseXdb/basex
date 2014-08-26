package org.basex.http.restxq;

import org.basex.http.*;

/**
 * This class represents the path of a RESTXQ function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class RestXqPath implements Comparable<RestXqPath> {
  /** Path segments. */
  final String[] segment;
  /** Template flags. */
  private final boolean[] template;
  /** Number of segments. */
  final int size;

  /**
   * Constructor.
   * @param path path
   */
  RestXqPath(final String path) {
    segment = HTTPContext.toSegments(path);
    size = segment.length;
    template = new boolean[size];
    for(int s = 0; s < size; s++) template[s] = segment[s].trim().startsWith("{");
    HTTPContext.decode(segment);
  }

  /**
   * Checks if the path matches the HTTP request.
   * @param http http context
   * @return result of check
   */
  boolean matches(final HTTPContext http) {
    // check if number of segments match
    if(size != http.depth()) return false;
    // check single segments
    for(int s = 0; s < size; s++) {
      if(!segment[s].equals(http.segment(s)) && !isTemplate(s)) return false;
    }
    return true;
  }

  /**
   * Checks if the specified path segment is a template.
   * @param s offset of segment
   * @return result of check
   */
  boolean isTemplate(final int s) {
    return template[s];
  }

  @Override
  public int compareTo(final RestXqPath rxs) {
    final int d = size - rxs.size;
    if(d != 0) return d;
    for(int s = 0; s < size; s++) {
      final boolean wc1 = isTemplate(s), wc2 = rxs.isTemplate(s);
      if(wc1 != wc2) return wc1 ? 1 : -1;
    }
    return 0;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("/");
    for(int s = 0; s < size; s++) sb.append(segment[s]).append('/');
    return sb.toString();
  }
}
