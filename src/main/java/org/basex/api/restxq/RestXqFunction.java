package org.basex.api.restxq;

import static javax.servlet.http.HttpServletResponse.*;
import static org.basex.api.restxq.RestXqText.*;

import java.util.*;
import java.util.regex.*;

import org.basex.api.*;
import org.basex.io.*;
import org.basex.query.func.*;
import org.basex.query.item.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class represents a single RESTful function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class RestXqFunction {
  /** Step pattern. */
  // [CG] RestXq: step needs to be further revised:
  // - spaces allowed? omit curly brackets? support correct QName syntax?
  private static final Pattern STEP = Pattern.compile("^(\\w+|\\{\\$[\\w:]+\\})$");

  /** Paths. */
  final ObjList<String[]> paths = new ObjList<String[]>();
  /** Supported methods. */
  EnumSet<HTTPMethod> methods = EnumSet.allOf(HTTPMethod.class);
  /** Associated user function. */
  UserFunc funct;
  /** Consumed media type. */
  String consumes;
  /** Returned media type. */
  String produces;

  /**
   * Constructor.
   * @param uf associated user function
   */
  RestXqFunction(final UserFunc uf) {
    funct = uf;
  }

  /**
   * Checks a function for RESTFful annotations.
   * @param file input file
   * @return {@code true} if module contains relevant annotations
   * @throws HTTPException HTTP exception
   */
  boolean update(final IOFile file) throws HTTPException {
    final EnumSet<HTTPMethod> rxm = EnumSet.noneOf(HTTPMethod.class);

    // loop through all annotations
    boolean found = false;
    for(int a = 0; a < funct.ann.size(); a++) {
      final QNm name = funct.ann.names[a];
      final Value val = funct.ann.values[a];
      boolean f = true;
      if(name.eq(PATH)) {
        // check path string
        if(!(val instanceof Str)) error(Util.info(SINGLE_STRING, PATH), funct, file);
        // check syntax of single steps
        final String[] steps = HTTPContext.toSteps(((Str) val).toJava());
        for(final String s : steps) {
          if(!STEP.matcher(s).matches()) error(Util.info(STEP_SYNTAX, s), funct, file);
        }
        paths.add(steps);
      } else if(name.eq(GET)) {
        rxm.add(HTTPMethod.GET);
      } else if(name.eq(POST)) {
        rxm.add(HTTPMethod.POST);
      } else if(name.eq(PUT)) {
        rxm.add(HTTPMethod.PUT);
      } else if(name.eq(DELETE)) {
        rxm.add(HTTPMethod.DELETE);
      } else if(name.eq(CONSUMES)) {
        if(!(val instanceof Str)) error(Util.info(SINGLE_STRING, CONSUMES), funct, file);
        consumes = ((Str) val).toJava();
      } else if(name.eq(PRODUCES)) {
        if(!(val instanceof Str)) error(Util.info(SINGLE_STRING, PRODUCES), funct, file);
        produces = ((Str) val).toJava();
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
          if(!path[p].equals(http.step(p)) && !path[p].startsWith("{")) {
            continue PATHS;
          }
        }
        return true;
      }
    }
    return false;
  }

  /**
   * Creates an exception with the specified message.
   * @param msg message
   * @param func function to be parsed
   * @param file input file
   * @return instance
   * @throws HTTPException HTTP exception
   */
  private HTTPException error(final String msg, final UserFunc func, final IOFile file)
      throws HTTPException {
    throw new HTTPException(SC_NOT_IMPLEMENTED, STATIC_ERROR, msg, file.name(), func);
  }
}
