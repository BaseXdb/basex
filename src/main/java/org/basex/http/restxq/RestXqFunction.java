package org.basex.http.restxq;

import static org.basex.http.HTTPMethod.*;
import static org.basex.http.restxq.RestXqText.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

import javax.servlet.http.*;

import org.basex.build.*;
import org.basex.http.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class represents a single RESTXQ function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class RestXqFunction implements Comparable<RestXqFunction> {
  /** Pattern for a single template. */
  private static final Pattern TEMPLATE =
      Pattern.compile("\\s*\\{\\s*\\$(.+?)\\s*\\}\\s*");

  /** Supported methods. */
  EnumSet<HTTPMethod> methods = EnumSet.allOf(HTTPMethod.class);
  /** Serialization parameters. */
  final SerializerProp output = new SerializerProp();
  /** Associated function. */
  final UserFunc function;
  /** Associated module. */
  final RestXqModule module;
  /** Path. */
  RestXqPath path;

  /** Query context. */
  private final QueryContext context;
  /** Consumed media types. */
  private final StringList consumes = new StringList();
  /** Returned media types. */
  private final StringList produces = new StringList();
  /** Query parameters. */
  private final ArrayList<RestXqParam> queryParams = new ArrayList<RestXqParam>();
  /** Form parameters. */
  private final ArrayList<RestXqParam> formParams = new ArrayList<RestXqParam>();
  /** Header parameters. */
  private final ArrayList<RestXqParam> headerParams = new ArrayList<RestXqParam>();
  /** Cookie parameters. */
  private final ArrayList<RestXqParam> cookieParams = new ArrayList<RestXqParam>();

  /** Post/Put variable. */
  private QNm requestBody;

  /**
   * Constructor.
   * @param uf associated user function
   * @param qc query context
   * @param m associated module
   */
  RestXqFunction(final UserFunc uf, final QueryContext qc, final RestXqModule m) {
    function = uf;
    context = qc;
    module = m;
  }

  /**
   * Processes the HTTP request.
   * Parses new modules and discards obsolete ones.
   * @param http HTTP context
   * @throws Exception exception
   */
  void process(final HTTPContext http) throws Exception {
    try {
      module.process(http, this);
    } catch(final QueryException ex) {
      if(ex.file() == null) ex.info(function.info);
      throw ex;
    }
  }

  /**
   * Checks a function for RESTFful annotations.
   * @return {@code true} if module contains relevant annotations
   * @throws QueryException query exception
   */
  boolean analyze() throws QueryException {
    // parse all annotations
    final EnumSet<HTTPMethod> mth = EnumSet.noneOf(HTTPMethod.class);
    final boolean[] declared = new boolean[function.args.length];
    boolean found = false;
    final int as = function.ann.size();
    for(int a = 0; a < as; a++) {
      final QNm name = function.ann.names[a];
      final Value value = function.ann.values[a];
      final byte[] local = name.local();
      final byte[] uri = name.uri();
      final boolean rexq = eq(uri, QueryText.RESTXQURI);
      if(rexq) {
        if(eq(PATH, local)) {
          // annotation "path"
          if(path != null) error(ANN_TWICE, "%", name.string());
          path = new RestXqPath(toString(value, name));
          for(final String s : path) {
            if(s.trim().startsWith("{")) checkVariable(s, AtomType.AAT, declared);
          }
        } else if(eq(CONSUMES, local)) {
          // annotation "consumes"
          strings(value, name, consumes);
        } else if(eq(PRODUCES, local)) {
          // annotation "produces"
          strings(value, name, produces);
        } else if(eq(QUERY_PARAM, local)) {
          // annotation "query-param"
          queryParams.add(param(value, name, declared));
        } else if(eq(FORM_PARAM, local)) {
          // annotation "form-param"
          formParams.add(param(value, name, declared));
        } else if(eq(HEADER_PARAM, local)) {
          // annotation "header-param"
          headerParams.add(param(value, name, declared));
        } else if(eq(COOKIE_PARAM, local)) {
          // annotation "cookie-param"
          cookieParams.add(param(value, name, declared));
        } else {
          // method annotations
          final HTTPMethod m = HTTPMethod.get(string(local));
          if(m == null) error(ANN_UNKNOWN, "%", name.string());
          if(!value.isEmpty()) {
            // remember post/put variable
            if(requestBody != null) error(ANN_TWICE, "%", name.string());
            if(m != POST && m != PUT) error(METHOD_VALUE, m);
            requestBody = checkVariable(toString(value, name), declared);
          }
          if(mth.contains(m)) error(ANN_TWICE, "%", name.string());
          mth.add(m);
        }
      } else if(eq(uri, QueryText.OUTPUTURI)) {
        // serialization parameters
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
      for(int i = 0; i < declared.length; i++)
        if(!declared[i]) error(VAR_UNDEFINED, function.args[i]);
    }
    return found;
  }

  /**
   * Checks if an HTTP request matches this function and its constraints.
   * @param http http context
   * @return result of check
   */
  boolean matches(final HTTPContext http) {
    // check method, path, consumed and produced media type
    return methods.contains(http.method) && pathMatches(http) &&
        consumes(http) && produces(http);
  }

  /**
   * Binds the annotated variables.
   * @param http http context
   * @param arg argument array
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  void bind(final HTTPContext http, final Expr[] arg) throws QueryException, IOException {
    // bind variables from segments
    for(int s = 0; s < path.size; s++) {
      final Matcher m = TEMPLATE.matcher(path.segment[s]);
      if(!m.find()) continue;
      final QNm qnm = new QNm(token(m.group(1)), context);
      bind(qnm, arg, new Atm(http.segment(s)));
    }

    // cache request body
    final String ct = http.contentType();
    IOContent body = null;

    if(requestBody != null) {
      body = cache(http, null);
      try {
        // bind request body in the correct format
        body.name(http.method + IO.XMLSUFFIX);
        bind(requestBody, arg, Parser.item(body, context.context.prop, ct));
      } catch(final IOException ex) {
        error(INPUT_CONV, ex);
      }
    }

    // bind query parameters
    final Map<String, String[]> params = http.params();
    for(final RestXqParam rxp : queryParams) bind(rxp, arg, params.get(rxp.key));

    // bind form parameters
    if(!formParams.isEmpty()) {
      if(MimeTypes.APP_FORM.equals(ct)) {
        // convert parameters encoded in a form
        body = cache(http, body);
        addParams(body.toString(), params);
      }
      for(final RestXqParam rxp : formParams) bind(rxp, arg, params.get(rxp.key));
    }

    // bind header parameters
    for(final RestXqParam rxp : headerParams) {
      final StringList sl = new StringList();
      final Enumeration<?> en =  http.req.getHeaders(rxp.key);
      while(en.hasMoreElements()) {
        for(final String s : en.nextElement().toString().split(", *")) sl.add(s);
      }
      bind(rxp, arg, sl.toArray());
    }

    // bind cookie parameters
    final Cookie[] ck = http.req.getCookies();
    for(final RestXqParam rxp : cookieParams) {
      String v = null;
      if(ck != null) {
        for(final Cookie c : ck) {
          if(rxp.key.equals(c.getName())) v = c.getValue();
        }
      }
      if(v == null) bind(rxp, arg);
      else bind(rxp, arg, v);
    }
  }

  /**
   * Creates an exception with the specified message.
   * @param msg message
   * @param ext error extension
   * @return exception
   * @throws QueryException query exception
   */
  QueryException error(final String msg, final Object... ext) throws QueryException {
    throw new QueryException(function.info, Err.BASX_RESTXQ, Util.info(msg, ext));
  }

  @Override
  public int compareTo(final RestXqFunction rxf) {
    return path.compareTo(rxf.path);
  }

  // PRIVATE METHODS ====================================================================

  /**
   * Checks the specified template and adds a variable.
   * @param tmp template string
   * @param declared variable declaration flags
   * @return resulting variable
   * @throws QueryException query exception
   */
  private QNm checkVariable(final String tmp, final boolean[] declared)
      throws QueryException {
    return checkVariable(tmp, AtomType.ITEM, declared);
  }

  /**
   * Checks the specified template and adds a variable.
   * @param tmp template string
   * @param type allowed type
   * @param declared variable declaration flags
   * @return resulting variable
   * @throws QueryException query exception
   */
  private QNm checkVariable(final String tmp, final Type type, final boolean[] declared)
      throws QueryException {
    final Var[] args = function.args;
    final Matcher m = TEMPLATE.matcher(tmp);
    if(!m.find()) error(INV_TEMPLATE, tmp);
    final byte[] vn = token(m.group(1));
    if(!XMLToken.isQName(vn)) error(INV_VARNAME, vn);
    final QNm qnm = new QNm(vn, context);
    int r = -1;
    while(++r < args.length && !args[r].name.eq(qnm));
    if(r == args.length) error(UNKNOWN_VAR, vn);
    if(declared[r]) error(VAR_ASSIGNED, vn);
    final SeqType st = args[r].declaredType();
    if(args[r].checksType() && !st.type.instanceOf(type)) error(INV_VARTYPE, vn, type);
    declared[r] = true;
    return qnm;
  }

  /**
   * Checks if the path matches the HTTP request.
   * @param http http context
   * @return result of check
   */
  private boolean pathMatches(final HTTPContext http) {
    return path.matches(http);
  }

  /**
   * Checks if the consumed content type matches.
   * @param http http context
   * @return result of check
   */
  private boolean consumes(final HTTPContext http) {
    // return true if no type is given
    if(consumes.isEmpty()) return true;
    // return true if no content type is specified by the user
    final String ct = http.contentType();
    if(ct == null) return true;
    // check if any combination matches
    for(final String c : consumes) {
      if(MimeTypes.matches(c, ct)) return true;
    }
    return false;
  }

  /**
   * Checks if the produced content type matches.
   * @param http http context
   * @return result of check
   */
  private boolean produces(final HTTPContext http) {
    // return true if no type is given
    if(produces.isEmpty()) return true;
    // check if any combination matches
    for(final String pr : http.produces()) {
      for(final String p : produces) {
        if(MimeTypes.matches(p, pr)) return true;
      }
    }
    return false;
  }

  /**
   * Binds the specified parameter to a variable.
   * @param rxp parameter
   * @param args argument array
   * @param values values to be bound; the parameter's default value is assigned
   *        if the argument is {@code null} or empty
   * @throws QueryException query exception
   */
  private void bind(final RestXqParam rxp, final Expr[] args, final String... values)
      throws QueryException {
    final Value val;
    if(values == null || values.length == 0) {
      val = rxp.value;
    } else {
      final ValueBuilder vb = new ValueBuilder();
      for(final String s : values) vb.add(new Atm(s));
      val = vb.value();
    }
    bind(rxp.name, args, val);
  }

  /**
   * Binds the specified value to a variable.
   * @param name variable name
   * @param args argument array
   * @param value value to be bound
   * @throws QueryException query exception
   */
  private void bind(final QNm name, final Expr[] args, final Value value)
      throws QueryException {
    // skip nulled values
    if(value == null) return;

    for(int i = 0; i < function.args.length; i++) {
      final Var var = function.args[i];
      if(!var.name.eq(name)) continue;
      // casts and binds the value
      args[i] = var.checkType(value, context, null);
      break;
    }
  }

  /**
   * Returns the specified value as an atomic string.
   * @param value value
   * @param name name
   * @return string
   * @throws QueryException HTTP exception
   */
  private String toString(final Value value, final QNm name) throws QueryException {
    if(!(value instanceof Str)) error(ANN_STRING, "%", name.string(), value);
    return ((Str) value).toJava();
  }

  /**
   * Adds items to the specified list.
   * @param value value
   * @param name name
   * @param list list to add values to
   * @throws QueryException HTTP exception
   */
  private void strings(final Value value, final QNm name, final StringList list)
      throws QueryException {

    final long vs = value.size();
    for(int v = 0; v < vs; v++) list.add(toString(value.itemAt(v), name));
  }

  /**
   * Returns a parameter.
   * @param value value
   * @param name name
   * @param declared variable declaration flags
   * @return parameter
   * @throws QueryException HTTP exception
   */
  private RestXqParam param(final Value value, final QNm name, final boolean[] declared)
      throws QueryException {
    // [CG] RESTXQ: allow identical field names?
    final long vs = value.size();
    if(vs < 2) error(ANN_PARAMS, "%", name.string(), 2);
    // name of parameter
    final String key = toString(value.itemAt(0), name);
    // variable template
    final QNm qnm = checkVariable(toString(value.itemAt(1), name), declared);
    // default value
    final ValueBuilder vb = new ValueBuilder();
    for(int v = 2; v < vs; v++) vb.add(value.itemAt(v));
    return new RestXqParam(qnm, key, vb.value());
  }

  // PRIVATE STATIC METHODS =============================================================

  /**
   * Caches the request body, if not done yet.
   * @param http http context
   * @param cache cache existing cache reference
   * @return cache
   * @throws IOException I/O exception
   */
  private static IOContent cache(final HTTPContext http, final IOContent cache)
      throws IOException {

    if(cache != null) return cache;
    final BufferInput bi = new BufferInput(http.req.getInputStream());
    final IOContent io = new IOContent(bi.content());
    io.name(http.method + IO.XMLSUFFIX);
    return io;
  }

  /**
   * Adds parameters from the passed on request body.
   * @param body request body
   * @param params map parameters
   */
  private static void addParams(final String body, final Map<String, String[]> params) {
    for(final String nv : body.split("&")) {
      final String[] parts = nv.split("=", 2);
      if(parts.length < 2) continue;
      try {
        params.put(parts[0], new String[] { URLDecoder.decode(parts[1], Token.UTF8) });
      } catch(final Exception ex) {
        Util.notexpected(ex);
      }
    }
  }
}
