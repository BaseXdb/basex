package org.basex.api.restxq;

import static javax.servlet.http.HttpServletResponse.*;
import static org.basex.api.restxq.RestXqText.*;

import java.util.*;
import org.basex.api.*;
import org.basex.io.*;
import org.basex.query.func.*;
import org.basex.query.item.*;
import org.basex.util.list.*;


/**
 * This class represents a single RESTful function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class RestXqFunction {
  /** Paths. */
  final ObjList<String[]> paths = new ObjList<String[]>();
  /** Supported methods. */
  EnumSet<HTTPMethod> methods = EnumSet.allOf(HTTPMethod.class);

  /**
   * Checks a function for RESTFful annotations.
   * Constructor.
   * @param func function to be parsed
   * @param file input file
   * @return {@code true} if module contains relevant annotations
   * @throws HTTPException HTTP exception
   */
  boolean update(final UserFunc func, final IOFile file) throws HTTPException {
    final EnumSet<HTTPMethod> rxm = EnumSet.noneOf(HTTPMethod.class);

    // loop through all annotations
    boolean found = false;
    for(int a = 0; a < func.ann.size(); a++) {
      // [CG] RestXq: handle REST annotations that do not match the specification
      final QNm name = func.ann.names[a];
      boolean f = true;
      if(name.eq(PATH)) {
        final Value v = func.ann.values[a];
        if(!(v instanceof Str)) {
          throw new HTTPException(SC_NOT_IMPLEMENTED, ERR_PATH_ANN, file.name(), func);
        }
        final String[] steps = HTTPContext.toSteps(((Str) v).toJava());

        paths.add(steps);

      } else if(name.eq(GET)) {
        rxm.add(HTTPMethod.GET);
      } else if(name.eq(POST)) {
        rxm.add(HTTPMethod.POST);
      } else if(name.eq(PUT)) {
        rxm.add(HTTPMethod.PUT);
      } else if(name.eq(DELETE)) {
        rxm.add(HTTPMethod.DELETE);
      } else {
        f = false;
      }
      found |= f;
    }
    if(!rxm.isEmpty()) methods = rxm;
    return found;
  }

  /**
   * Checks if the function supports the specified path and method.
   * @param http http context
   * @return instance
   */
  boolean supports(final HTTPContext http) {
    // check method
    if(methods.contains(http.method)) {
      // check path
      PATHS:
      for(final String[] path : paths) {
        if(path.length != http.depth()) continue;
        // check single steps
        for(int p = 0; p < path.length; p++) {
          // [CG] RestXq: handle incorrect path annotations
          if(!path[p].equals(http.step(p)) && !path[p].startsWith("{")) {
            continue PATHS;
          }
        }
        return true;
      }
    }
    return false;
  }
}
