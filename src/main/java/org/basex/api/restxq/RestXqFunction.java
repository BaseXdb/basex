package org.basex.api.restxq;

import static org.basex.api.restxq.RestXqText.*;
import static org.basex.api.HTTPMethod.*;
import static org.basex.util.Token.*;

import java.util.*;
import java.util.regex.*;

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
  /** Pattern for a single template. */
  static final Pattern TEMPLATE = Pattern.compile("\\{\\$(.+)\\}");

  /** Serialization parameters. */
  final SerializerProp output = new SerializerProp();
  /** Query context. */
  final QueryContext context;
  /** Associated user function. */
  final UserFunc function;
  /** Consumed media type. */
  final StringList consumes = new StringList();
  /** Returned media type. */
  final StringList produces = new StringList();
  /** Supported methods. */
  EnumSet<HTTPMethod> methods = EnumSet.allOf(HTTPMethod.class);
  /** Path. */
  RestXqPath path;

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
      final boolean rexq = startsWith(uri, QueryText.REXQURI);
      if(rexq) {
        if(eq(PATH, local)) {
          path = new RestXqPath(toString(value, name), this);
        } else if(eq(CONSUMES, local)) {
          consumes.add(toString(value, name));
        } else if(eq(PRODUCES, local)) {
          produces.add(toString(value, name));
        } else {
          final HTTPMethod m = HTTPMethod.get(string(local));
          if(m == null) error(NOT_SUPPORTED, "%", name.string());
          if(!value.isEmpty()) {
            if(m != POST && m != PUT) error(METHOD_VALUE, m);
            checkVariable(toString(value, name), AtomType.ITEM);
          }
          mth.add(m);
        }
      } else if(eq(uri, QueryText.OUTPUTURI)) {
        // output parameters
        final String key = string(local);
        final String val = toString(value, name);
        if(output.get(key) == null) error(UNKNOWN_SER, key);
        output.set(key, val);
      }
      found |= rexq;
    }
    if(!mth.isEmpty()) methods = mth;

    if(found) {
      if(path == null) error(ANN_MISSING, PATH);
      for(final Var v : function.args) {
        if(!v.declared) error(VAR_UNDEFINED, v.name.string());
      }
    }
    return found;
  }

  /**
   * Checks the specified template and adds a variable.
   * @param template template string
   * @param type allowed type
   * @throws QueryException query exception
   */
  void checkVariable(final String template, final Type type) throws QueryException {
    final Var[] args = function.args;
    final Matcher m = TEMPLATE.matcher(template);
    if(!m.find()) error(INVALID_TEMPLATE, template);
    final byte[] vn = token(m.group(1));
    if(!XMLToken.isQName(vn)) error(INVALID_VAR, vn);
    final QNm qnm = new QNm(vn, context);
    int r = -1;
    while(++r < args.length) {
      if(args[r].name.eq(qnm)) break;
    }
    if(r == args.length) error(UNKNOWN_VAR, vn);
    if(args[r].declared) error(VAR_ASSIGNED, vn);
    final SeqType st = args[r].type;
    if(st != null && !st.type.instanceOf(type)) error(VAR_TYPE, vn, type);
    args[r].declared = true;
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
    // loop through all segments and bind variables
    final QueryContext qc = context;
    for(int s = 0; s < path.segments.length; s++) {
      final String seg = path.segments[s];
      final Matcher m = RestXqFunction.TEMPLATE.matcher(seg);
      if(!m.find()) continue;
      final QNm qnm = new QNm(token(m.group(1)), qc);
      final String val = http.segment(s);

      // finds the correct variable
      for(final Var var : function.args) {
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

  // PRIVATE METHODS ====================================================================

  /**
   * Checks if the consumed content type matches.
   * @param http http context
   * @return instance
   */
  private boolean consumes(final HTTPContext http) {
    // return true if no type is given
    if(consumes.isEmpty()) return true;
    // return true if no content type is specified by the user
    final String cons = http.req.getContentType();
    if(cons == null) return true;

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
   * @param name name
   * @return string
   * @throws QueryException HTTP exception
   */
  private String toString(final Value value, final QNm name) throws QueryException {
    if(!(value instanceof Str)) error(SINGLE_STRING, "%", name.string());
    return ((Str) value).toJava();
  }
}
