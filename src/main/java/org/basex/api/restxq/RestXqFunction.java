package org.basex.api.restxq;

import static javax.servlet.http.HttpServletResponse.*;
import static org.basex.api.restxq.RestXqText.*;

import java.util.*;
import java.util.regex.*;

import org.basex.api.*;
import org.basex.io.*;
import org.basex.io.serial.*;
import org.basex.query.*;
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

  /** Associated user function. */
  UserFunc funct;
  /** Associated file. */
  IOFile file;

  /** Serialization parameters. */
  final SerializerProp output = new SerializerProp();
  /** Paths. */
  final ObjList<String[]> paths = new ObjList<String[]>();
  /** Supported methods. */
  EnumSet<HTTPMethod> methods = EnumSet.allOf(HTTPMethod.class);
  /** Consumed media type. */
  StringList consumes = new StringList();
  /** Returned media type. */
  StringList produces = new StringList();

  /**
   * Constructor.
   * @param uf associated user function
   * @param in input file
   */
  RestXqFunction(final UserFunc uf, final IOFile in) {
    funct = uf;
    file = in;
  }

  /**
   * Checks a function for RESTFful annotations.
   * @return {@code true} if module contains relevant annotations
   * @throws HTTPException HTTP exception
   */
  boolean analyze() throws HTTPException {
    final EnumSet<HTTPMethod> mth = EnumSet.noneOf(HTTPMethod.class);

    // loop through all annotations
    boolean found = false;
    for(int a = 0; a < funct.ann.size(); a++) {
      final QNm name = funct.ann.names[a];
      final Value value = funct.ann.values[a];
      boolean f = true;
      if(name.eq(PATH)) {
        // get path string and check syntax of single steps
        final String v = string(value, SINGLE_STRING, PATH);
        final String[] steps = HTTPContext.toSteps(v);
        for(final String s : steps) {
          if(!STEP.matcher(s).matches()) error(STEP_SYNTAX, s);
        }
        paths.add(steps);
      } else if(name.eq(GET)) {
        mth.add(HTTPMethod.GET);
      } else if(name.eq(POST)) {
        mth.add(HTTPMethod.POST);
      } else if(name.eq(PUT)) {
        mth.add(HTTPMethod.PUT);
      } else if(name.eq(DELETE)) {
        mth.add(HTTPMethod.DELETE);
      } else if(name.eq(CONSUMES)) {
        consumes.add(string(value, SINGLE_STRING, CONSUMES));
      } else if(name.eq(PRODUCES)) {
        produces.add(string(value, SINGLE_STRING, PRODUCES));
      } else if(Token.eq(name.uri(), QueryText.OUTPUTURI)) {
        // output parameters
        final String key = Token.string(name.local());
        final String val = string(value, OUTPUT_STRING, key);
        if(output.get(key) == null) error(UNKNOWN_SER, key);
        output.set(key, val);
      } else {
        f = false;
      }
      found |= f;
    }
    if(!mth.isEmpty()) methods = mth;
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
          if(!path[p].equals(http.step(p)) && !path[p].startsWith("{")) continue PATHS;
        }
        // check consumed content type
        if(!consumes.empty() && !consumes.contains(http.req.getContentType())) continue;

        // check producing content type
        if(!produces.empty() && produces.contains(http.req.getHeader("Accept")))
          return true;

        return true;
      }
    }
    return false;
  }

  /**
   * Returns the specified value as an atomic string, or throws an exception.
   * @param value value
   * @param msg error message
   * @param ext error extension
   * @return string
   * @throws HTTPException HTTP exception
   */
  private String string(final Value value, final String msg, final Object... ext)
      throws HTTPException {

    if(!(value instanceof Str)) error(msg, ext);
    return ((Str) value).toJava();
  }

  /**
   * Creates an exception with the specified message.
   * @param msg message
   * @param ext error extension
   * @return instance
   * @throws HTTPException HTTP exception
   */
  private HTTPException error(final String msg, final Object... ext)
      throws HTTPException {
    throw new HTTPException(SC_NOT_IMPLEMENTED, STATIC_ERROR,
        Util.info(msg, ext), file.name(), funct);
  }
}
