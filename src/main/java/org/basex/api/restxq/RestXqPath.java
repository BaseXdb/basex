package org.basex.api.restxq;

import static org.basex.api.restxq.RestXqText.*;
import static org.basex.util.Token.*;

import java.util.regex.*;

import org.basex.api.*;
import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.query.util.*;
import org.basex.util.*;

/**
 * This class stores the path of a RESTful function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class RestXqPath {
  /** Pattern for a single template. */
  private static final Pattern TEMPLATE = Pattern.compile("\\{\\$(.+)\\}");

  /** RESTful function. */
  private final RestXqFunction rxf;
  /** Path segments. */
  private final String[] segments;

  /**
   * Constructor.
   * Validates and adds the path of a RESTful function.
   * @param path value of the path annotation
   * @param rf RESTful function
   * @throws QueryException query exception
   */
  RestXqPath(final String path, final RestXqFunction rf) throws QueryException {
    rxf = rf;
    final Var[] args = rxf.function.args;

    // get path string and check syntax of single segments
    segments = HTTPContext.toSegments(path);
    for(final String s : segments) {
      if(s.startsWith("{")) {
        final Matcher m = TEMPLATE.matcher(s);
        if(!m.find()) rxf.error(INVALID_TEMPLATE, s);
        final byte[] vn = token(m.group(1));
        if(!XMLToken.isQName(vn)) rxf.error(INVALID_VAR, vn);
        final QNm qnm = new QNm(vn, rxf.context);
        int r = -1;
        while(++r < args.length) {
          if(args[r].name.eq(qnm)) break;
        }
        if(r == args.length) rxf.error(UNKNOWN_VAR, vn);
        if(args[r].declared) rxf.error(VAR_ASSIGNED, vn);
        if(!args[r].type.type.instanceOf(AtomType.AAT))
          rxf.error(VAR_ATOMIC, vn, AtomType.AAT);
        args[r].declared = true;
      }
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

  /**
   * Binds the annotated variables.
   * @param http http context
   * @throws QueryException query exception
   */
  void bind(final HTTPContext http) throws QueryException {
    // loop through all segments and bind variables
    final QueryContext qc = rxf.context;
    for(int s = 0; s < segments.length; s++) {
      final String seg = segments[s];
      final Matcher m = TEMPLATE.matcher(seg);
      if(!m.find()) continue;
      final QNm qnm = new QNm(token(m.group(1)), qc);
      final String val = http.segment(s);

      // finds the correct variable
      for(final Var var : rxf.function.args) {
        if(!var.name.eq(qnm)) continue;
        // creates an atomic value
        Item item = new Atm(token(val));
        // casts the value
        if(var.type != null) item = var.type.type.cast(item, qc, null);
        // binds the value
        var.bind(var.type != null ? item : item, qc);
        break;
      }
    }
  }
}
