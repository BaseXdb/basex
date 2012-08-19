package org.basex.http.restxq;

import java.util.*;

import org.basex.http.*;
import org.basex.util.*;

/**
 * This class represents the path segments of a RESTXQ function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class RestXqSegments implements Iterable<String>, Comparable<RestXqSegments> {
  /** Path segments. */
  final String[] segments;

  /**
   * Constructor.
   * @param path path
   */
  RestXqSegments(final String path) {
    segments = HTTPContext.toSegments(path);
  }

  /**
   * Returns the specified segment.
   * @param s offset
   * @return segment
   */
  String get(final int s) {
    return segments[s];
  }

  /**
   * Returns the number of segments.
   * @return number of segments
   */
  int size() {
    return segments.length;
  }

  /**
   * Checks if the specified path segment is a wildcard.
   * @param s offset of segment
   * @return result of check
   */
  boolean isWC(final int s) {
    return segments[s].trim().startsWith("{");
  }

  @Override
  public int compareTo(final RestXqSegments rxs) {
    final int d = size() - rxs.size();
    if(d != 0) return d;
    for(int s = 0; s < size(); s++) {
      final boolean wc1 = isWC(s);
      final boolean wc2 = rxs.isWC(s);
      if(wc1 != wc2) return wc1 ? 1 : -1;
    }
    return 0;
  }

  @Override
  public Iterator<String> iterator() {
    return new Iterator<String>() {
      private int c;
      @Override
      public boolean hasNext() { return c < segments.length; }
      @Override
      public String next() { return segments[c++]; }
      @Override
      public void remove() { Util.notexpected(); }
    };
  }

  @Override
  public String toString() {
    // returns a schematic representation of the segments
    final StringBuilder sb = new StringBuilder("/");
    for(int s = 0; s < size(); s++) {
      sb.append(isWC(s) ? "{...}" : segments[s]).append('/');
    }
    return sb.toString();
  }
}
