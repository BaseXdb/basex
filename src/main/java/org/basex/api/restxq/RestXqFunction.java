package org.basex.api.restxq;

import static org.basex.api.restxq.RestXqText.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.api.*;
import org.basex.io.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.item.*;
import org.basex.query.util.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class represents a single RESTful function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class RestXqFunction {
  /** Query context. */
  QueryContext context;
  /** Associated user function. */
  UserFunc function;

  /** Serialization parameters. */
  final SerializerProp output = new SerializerProp();
  /** Supported methods. */
  private EnumSet<HTTPMethod> methods = EnumSet.allOf(HTTPMethod.class);
  /** Consumed media type. */
  private StringList consumes = new StringList();
  /** Returned media type. */
  private StringList produces = new StringList();
  /** Path. */
  private RestXqPath path;

  /**
   * Constructor.
   * @param uf associated user function
   * @param qc query context
   */
  RestXqFunction(final UserFunc uf, final QueryContext qc) {
    function = uf;
    context = qc;
  }

  /**
   * Checks a function for RESTFful annotations.
   * @return {@code true} if module contains relevant annotations
   * @throws QueryException query exception
   */
  boolean analyze() throws QueryException {
    final EnumSet<HTTPMethod> mth = EnumSet.noneOf(HTTPMethod.class);

    // loop through all annotations
    boolean found = false;
    for(int a = 0, as = function.ann.size(); a < as; a++) {
      final QNm name = function.ann.names[a];
      final Value value = function.ann.values[a];
      final byte[] local = name.local();
      final byte[] uri = name.uri();
      // later: change to equality
      boolean rexq = startsWith(uri, QueryText.REXQURI);
      if(rexq) {
        if(eq(PATH, local)) {
          path = new RestXqPath(toString(value, SINGLE_STRING, PATH), this);
        } else if(eq(GET, local)) {
          mth.add(HTTPMethod.GET);
        } else if(eq(POST, local)) {
          mth.add(HTTPMethod.POST);
        } else if(eq(PUT, local)) {
          mth.add(HTTPMethod.PUT);
        } else if(eq(DELETE, local)) {
          mth.add(HTTPMethod.DELETE);
        } else if(eq(CONSUMES, local)) {
          consumes.add(toString(value, SINGLE_STRING, CONSUMES));
        } else if(eq(PRODUCES, local)) {
          produces.add(toString(value, SINGLE_STRING, PRODUCES));
        } else {
          error(NOT_SUPPORTED, name);
        }
      } else if(eq(uri, QueryText.OUTPUTURI)) {
        // output parameters
        final String key = string(local);
        final String val = toString(value, OUTPUT_STRING, key);
        if(output.get(key) == null) error(UNKNOWN_SER, key);
        output.set(key, val);
      }
      found |= rexq;
    }
    if(!mth.isEmpty()) methods = mth;

    if(found) {
      if(path == null) error(PATH_UNDEFINED, PATH);
      for(final Var v : function.args) {
        if(!v.declared) error(VAR_UNDEFINED, v.name.string());
      }
    }
    return found;
  }

  /**
   * Checks if the function matches the HTTP request.
   * @param http http context
   * @return instance
   */
  boolean matches(final HTTPContext http) {
    // check method, path, consumed and produced media type
    return methods.contains(http.method) && path.matches(http) &&
        consumes(http) && produces(http);
  }

  /**
   * Binds the annotated variables.
   * @param http http context
   * @throws QueryException query exception
   */
  void bind(final HTTPContext http) throws QueryException {
    path.bind(http);
  }

  /**
   * Checks if the consumed content type matches.
   * @param http http context
   * @return instance
   */
  private boolean consumes(final HTTPContext http) {
    // return true if no type is given
    if(consumes.isEmpty()) return true;

    final String cons = http.req.getContentType();
    for(int c = 0; c < consumes.size(); c++) {
      if(MimeTypes.matches(consumes.get(c), cons)) return true;
    }
    return false;
  }

  /**
   * Checks if the produced content type matches.
   * @param http http context
   * @return instance
   */
  private boolean produces(final HTTPContext http) {
    // return true if no type is given
    if(produces.isEmpty()) return true;

    final String[] prod = http.produces();
    for(int p = 0; p < produces.size(); p++) {
      for(final String pr : prod) {
        if(MimeTypes.matches(produces.get(p), pr)) return true;
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
   * @throws QueryException HTTP exception
   */
  private String toString(final Value value, final String msg, final Object... ext)
      throws QueryException {

    if(!(value instanceof Str)) error(msg, ext);
    return ((Str) value).toJava();
  }

  /**
   * Creates an exception with the specified message.
   * @param msg message
   * @param ext error extension
   * @return instance
   * @throws QueryException query exception
   */
  QueryException error(final String msg, final Object... ext) throws QueryException {
    throw new QueryException(function.input, Err.REXQERROR, Util.info(msg, ext));
  }
}
