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
import org.basex.core.*;
import org.basex.http.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class represents a single RESTXQ function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class RestXqFunction {
  /** Pattern for a single template. */
  private static final Pattern TEMPLATE =
      Pattern.compile("\\s*\\{\\s*\\$(.+?)\\s*\\}\\s*");

  /** Supported methods. */
  EnumSet<HTTPMethod> methods = EnumSet.allOf(HTTPMethod.class);
  /** Serialization parameters. */
  final SerializerProp output = new SerializerProp();
  /** Associated function. */
  final UserFunc function;

  /** Query context. */
  private final QueryContext context;
  /** Consumed media types. */
  private final StringList consumes = new StringList();
  /** Returned media types. */
  private final StringList produces = new StringList();
  /** Query parameters. */
  private final ArrayList<RestXqParam> queryParams = new ArrayList<RestXqParam>();
  /** Query parameters. */
  private final ArrayList<RestXqParam> formParams = new ArrayList<RestXqParam>();
  /** Query parameters. */
  private final ArrayList<RestXqParam> headerParams = new ArrayList<RestXqParam>();
  /** Query parameters. */
  private final ArrayList<RestXqParam> cookieParams = new ArrayList<RestXqParam>();
  /** Path segments. */
  private String[] segments;
  /** Post/Put variable. */
  private QNm requestBody;
  /** Reference to servlet request. */
  private QNm request;
  /** Reference to servlet response. */
  private QNm response;
  /** Session id variable (deprecated). */
  private QNm sessionID;

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
    // parse all annotations
    final EnumSet<HTTPMethod> mth = EnumSet.noneOf(HTTPMethod.class);
    boolean found = false;
    for(int a = 0, as = function.ann.size(); a < as; a++) {
      final QNm name = function.ann.names[a];
      final Value value = function.ann.values[a];
      final byte[] local = name.local();
      final byte[] uri = name.uri();
      final boolean rexq = eq(uri, QueryText.RESTXQURI);
      if(rexq) {
        if(eq(PATH, local)) {
          // annotation "path"
          if(segments != null) error(ANN_TWICE, "%", name.string());
          segments = HTTPContext.toSegments(toString(value, name));
          for(final String s : segments) {
            if(s.trim().startsWith("{")) checkVariable(s, AtomType.AAT);
          }
        } else if(eq(CONSUMES, local)) {
          // annotation "consumes"
          strings(value, name, consumes);
        } else if(eq(PRODUCES, local)) {
          // annotation "produces"
          strings(value, name, produces);
        } else if(eq(QUERY_PARAM, local)) {
          // annotation "query-param"
          queryParams.add(param(value, name));
        } else if(eq(FORM_PARAM, local)) {
          // annotation "form-param"
          formParams.add(param(value, name));
        } else if(eq(HEADER_PARAM, local)) {
          // annotation "header-param"
          headerParams.add(param(value, name));
        } else if(eq(COOKIE_PARAM, local)) {
          // annotation "cookie-param"
          cookieParams.add(param(value, name));
        } else if(eq(SESSION_ID, local)) {
          if(sessionID != null) error(ANN_TWICE, "%", name.string());
          sessionID = checkVariable(toString(value, name));
        } else if(eq(REQUEST, local)) {
          if(request != null) error(ANN_TWICE, "%", name.string());
          request = checkVariable(toString(value, name));
        } else if(eq(RESPONSE, local)) {
          if(response != null) error(ANN_TWICE, "%", name.string());
          response = checkVariable(toString(value, name));
        } else {
          // method annotations
          final HTTPMethod m = HTTPMethod.get(string(local));
          if(m == null) error(ANN_UNKNOWN, "%", name.string());
          if(!value.isEmpty()) {
            // remember post/put variable
            if(requestBody != null) error(ANN_TWICE, "%", name.string());
            if(m != POST && m != PUT) error(METHOD_VALUE, m);
            requestBody = checkVariable(toString(value, name));
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
      if(segments == null) error(ANN_MISSING, PATH);
      for(final Var v : function.args) {
        if(!v.declared) error(VAR_UNDEFINED, v.name.string());
      }
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
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  void bind(final HTTPContext http) throws QueryException, IOException {
    // bind variables from segments
    for(int s = 0; s < segments.length; s++) {
      final String seg = segments[s];
      final Matcher m = TEMPLATE.matcher(seg);
      if(!m.find()) continue;
      final QNm qnm = new QNm(token(m.group(1)), context);
      bind(qnm, new Atm(http.segment(s)));
    }

    // bind request body from post/put method
    final Prop prop = context.context.prop;

    // cache request body
    final String ct = http.contentType();
    IOContent body = null;

    if(requestBody != null) {
      body = cache(http, body);
      try {
        // bind request body in the correct format
        body.name(http.method + IO.XMLSUFFIX);
        bind(requestBody, Parser.item(body, prop, ct));
      } catch(final IOException ex) {
        error(INPUT_CONV, ex);
      }
    }

    // bind session id (deprecated)
    if(sessionID != null) bind(sessionID, Str.get(http.req.getSession().getId()));
    // bind request
    if(request != null) bind(request, new Jav(http.req));
    // bind request
    if(response != null) bind(response, new Jav(http.res));

    // bind query parameters
    final Map<String, String[]> params = http.params();
    for(final RestXqParam rxp : queryParams) bind(rxp, params.get(rxp.key));

    // bind form parameters
    if(formParams.size() != 0) {
      if(MimeTypes.APP_FORM.equals(ct)) {
        // convert parameters encoded in a form
        body = cache(http, body);
        addParams(body.toString(), params);
      }
      for(final RestXqParam rxp : formParams) bind(rxp, params.get(rxp.key));
    }

    // bind header parameters
    for(final RestXqParam rxp : headerParams) {
      final StringList sl = new StringList();
      final Enumeration<?> en =  http.req.getHeaders(rxp.key);
      while(en.hasMoreElements()) {
        for(final String s : en.nextElement().toString().split(", *")) sl.add(s);
      }
      bind(rxp, sl.toArray());
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
      if(v == null) bind(rxp);
      else bind(rxp, v);
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

  // PRIVATE METHODS ====================================================================

  /**
   * Checks the specified template and adds a variable.
   * @param tmp template string
   * @return resulting variable
   * @throws QueryException query exception
   */
  private QNm checkVariable(final String tmp) throws QueryException {
    return checkVariable(tmp, AtomType.ITEM);
  }

  /**
   * Checks the specified template and adds a variable.
   * @param tmp template string
   * @param type allowed type
   * @return resulting variable
   * @throws QueryException query exception
   */
  private QNm checkVariable(final String tmp, final Type type) throws QueryException {
    final Var[] args = function.args;
    final Matcher m = TEMPLATE.matcher(tmp);
    if(!m.find()) error(INV_TEMPLATE, tmp);
    final byte[] vn = token(m.group(1));
    if(!XMLToken.isQName(vn)) error(INV_VARNAME, vn);
    final QNm qnm = new QNm(vn, context);
    int r = -1;
    while(++r < args.length && !args[r].name.eq(qnm));
    if(r == args.length) error(UNKNOWN_VAR, vn);
    if(args[r].declared) error(VAR_ASSIGNED, vn);
    final SeqType st = args[r].type;
    if(st != null && !st.type.instanceOf(type)) error(INV_VARTYPE, vn, type);
    args[r].declared = true;
    return qnm;
  }

  /**
   * Checks if the path matches the HTTP request.
   * @param http http context
   * @return result of check
   */
  boolean pathMatches(final HTTPContext http) {
    // check if number of segments match
    if(segments.length != http.depth()) return false;
    // check single segments
    for(int s = 0; s < segments.length; s++) {
      final String seg = segments[s].trim();
      if(!seg.equals(http.segment(s)) && !seg.startsWith("{")) return false;
    }
    return true;
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
   * @param values values to be bound; the parameter's default value is assigned
   *        if the argument is {@code null} or empty
   * @throws QueryException query exception
   */
  private void bind(final RestXqParam rxp, final String... values) throws QueryException {
    final Value val;
    if(values == null || values.length == 0) {
      val = rxp.value;
    } else {
      final ValueBuilder vb = new ValueBuilder();
      for(final String s : values) vb.add(new Atm(s));
      val = vb.value();
    }
    bind(rxp.name, val);
  }

  /**
   * Binds the specified value to a variable.
   * @param name variable name
   * @param value value to be bound
   * @throws QueryException query exception
   */
  private void bind(final QNm name, final Value value) throws QueryException {
    // skip nulled values
    if(value == null) return;

    Value v = value;
    for(final Var var : function.args) {
      if(!var.name.eq(name)) continue;
      // casts and binds the value
      if(var.type != null) v = var.type.promote(value, context, null);
      var.bind(v, context);
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
    if(!(value instanceof Str)) error(ANN_STRING, "%", name.string());
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
    for(int v = 0; v < vs; v++) {
      list.add(toString(value.itemAt(v), name));
    }
  }

  /**
   * Returns a parameter.
   * @param value value
   * @param name name
   * @return parameter
   * @throws QueryException HTTP exception
   */
  private RestXqParam param(final Value value, final QNm name) throws QueryException {
    // [CG] RESTXQ: allow identical field names?
    final long vs = value.size();
    if(vs < 2) error(ANN_PARAMS, "%", name.string());
    // name of parameter
    final String key = toString(value.itemAt(0), name);
    // variable template
    final QNm qnm = checkVariable(toString(value.itemAt(1), name));
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
