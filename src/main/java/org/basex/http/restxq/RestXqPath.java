package org.basex.http.restxq;

import java.util.*;

import org.basex.http.*;
import org.basex.util.*;

/**
 * This class represents the path of a RESTXQ function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class RestXqPath implements Iterable<String>, Comparable<RestXqPath> {
  /** Path segments. */
  final String[] segment;
  /** Number of segments. */
  final int size;

  /**
   * Constructor.
   * @param path path
   */
  RestXqPath(final String path) {
    segment = HTTPContext.toSegments(path);
    size = segment.length;
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
  private boolean isTemplate(final int s) {
    return segment[s].trim().startsWith("{");
  }

  @Override
  public int compareTo(final RestXqPath rxs) {
    final int d = size - rxs.size;
    if(d != 0) return d;
    for(int s = 0; s < size; s++) {
      final boolean wc1 = isTemplate(s);
      final boolean wc2 = rxs.isTemplate(s);
      if(wc1 != wc2) return wc1 ? 1 : -1;
    }
    return 0;
  }

  @Override
  public Iterator<String> iterator() {
    return new Iterator<String>() {
      private int c;
      @Override
      public boolean hasNext() { return c < size; }
      @Override
      public String next() { return segment[c++]; }
      @Override
      public void remove() { Util.notexpected(); }
    };
  }

  @Override
  public String toString() {
    // returns a schematic representation of the segments
    final StringBuilder sb = new StringBuilder("/");
    for(int s = 0; s < size; s++) {
      sb.append(isTemplate(s) ? "{...}" : segment[s]).append('/');
    }
    return sb.toString();
  }
}
