package org.basex.http.restxq;

import org.basex.http.*;
import org.basex.query.*;
import org.basex.query.item.*;

/**
 * This class stores the path of a RESTful function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class RestXqPath {
  /** RESTful function. */
  private final RestXqFunction rxf;
  /** Path segments. */
  final String[] segments;

  /**
   * Constructor.
   * Validates and adds the path of a RESTful function.
   * @param path value of the path annotation
   * @param rf RESTful function
   * @throws QueryException query exception
   */
  RestXqPath(final String path, final RestXqFunction rf) throws QueryException {
    rxf = rf;
    // get path string and check syntax of single segments
    segments = HTTPContext.toSegments(path);
    for(final String s : segments) {
      if(s.startsWith("{")) rxf.checkVariable(s, AtomType.AAT);
    }
  }

  /**
   * Checks if the path matches the HTTP request.
   * @param http http context
   * @return instance
   */
  boolean matches(final HTTPContext http) {
    // check if number of segments match
    if(segments.length != http.depth()) return false;

    // check single segments
    for(int s = 0; s < segments.length; s++) {
      final String seg = segments[s];
      if(!seg.equals(http.segment(s)) && !seg.startsWith("{")) return false;
    }
    return true;
  }
}
