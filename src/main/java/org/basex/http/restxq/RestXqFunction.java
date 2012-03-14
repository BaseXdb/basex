package org.basex.http.restxq;

import static org.basex.http.HTTPMethod.*;
import static org.basex.http.restxq.RestXqText.*;
import static org.basex.util.Token.*;

import java.io.*;
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
  private static final Pattern TEMPLATE = Pattern.compile("\\{\\$(.+)\\}");

  /** Supported methods. */
  EnumSet<HTTPMethod> methods = EnumSet.allOf(HTTPMethod.class);
  /** Serialization parameters. */
  final SerializerProp output = new SerializerProp();
  /** Associated user function. */
  final UserFunc function;

  /** Query context. */
  private final QueryContext context;
  /** Consumed media type. */
  private final StringList consumes = new StringList();
  /** Returned media type. */
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
      // later: change to equality
      final boolean rexq = startsWith(uri, QueryText.REXQURI);
      if(rexq) {
        if(eq(PATH, local)) {
          // annotation "path"
          if(segments != null) error(ANN_TWICE, "%", name.string());
          segments = HTTPContext.toSegments(toString(value, name));
          for(final String s : segments) {
            if(s.startsWith("{")) checkVariable(s, AtomType.AAT);
          }
        } else if(eq(CONSUMES, local)) {
          // [CG] RESTXQ, consumes/produces: "take a SINGLE mandatory string literal"?
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
      final Matcher m = RestXqFunction.TEMPLATE.matcher(seg);
      if(!m.find()) continue;
      final QNm qnm = new QNm(token(m.group(1)), context);
      bind(qnm, new Atm(http.segment(s)));
    }

    // bind request body from post/put method
    final Prop prop = context.context.prop;

    // cache request body
    final String ct = http.req.getContentType();
    final BufferInput bi = new BufferInput(http.in);
    IOContent cache = null;

    if(requestBody != null) {
      cache = new IOContent(bi.content());
      cache.name(http.method + IO.XMLSUFFIX);
      Item item = null;
      try {
        // retrieve the request body in the correct format
        item = Parser.item(cache, prop, ct);
      } catch(final IOException ex) {
        error(INPUT_CONV, ex);
      }
      bind(requestBody, item);
    }

    // bind query parameters
    for(final RestXqParam rxp : queryParams) {
      final String v = http.req.getParameter(rxp.key);
      bind(rxp.name, v == null ? rxp.item : new Atm(v));
    }
    /* bind form parameters
    final boolean post = MimeTypes.APP_FORM.equals(ct);
    if(formParams.size() != 0) {
      for(final RestXqParam rxp : formParams) {
        final String v;
        if(post) {
          v = null;
        } else {
          v = http.req.getParameter(rxp.key);
        }
        bind(rxp.name, v == null ? rxp.item : new Atm(v));
      }
    }*/
    // bind header parameters
    for(final RestXqParam rxp : headerParams) {
      final String v = http.req.getHeader(rxp.key);
      bind(rxp.name, v == null ? rxp.item : new Atm(v));
    }
    // bind cookie parameters
    final Cookie[] cookies = http.req.getCookies();
    if(cookies != null) {
      for(final RestXqParam rxp : cookieParams) {
        for(final Cookie c : cookies) {
          if(!rxp.key.equals(c.getName())) continue;
          final String v = c.getValue();
          bind(rxp.name, v == null ? rxp.item : new Atm(v));
        }
      }
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
    throw new QueryException(function.input, Err.REXQERROR, Util.info(msg, ext));
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
      final String seg = segments[s];
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
    final String ct = http.req.getContentType();
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
   * Binds the specified item to a variable.
   * @param name variable name
   * @param item item to be bound
   * @throws QueryException query exception
   */
  private void bind(final QNm name, final Item item) throws QueryException {
    // skip nulled items
    if(item == null) return;

    Item it = item;
    for(final Var var : function.args) {
      if(!var.name.eq(name)) continue;
      // casts and binds the value
      if(var.type != null) it = var.type.type.cast(item, context, null);
      var.bind(it, context);
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
   * [CG] RESTXQ: allow identical field names?
   * @param value value
   * @param name name
   * @return parameter
   * @throws QueryException HTTP exception
   */
  private RestXqParam param(final Value value, final QNm name) throws QueryException {
    final long vs = value.size();
    if(vs < 2 || vs > 3) error(ANN_PARAMS, "%", name.string());
    // name of parameter
    final String key = toString(value.itemAt(0), name);
    // variable template
    final QNm qnm = checkVariable(toString(value.itemAt(1), name));
    // default value
    final Item val = vs == 3 ? value.itemAt(2) : null;
    return new RestXqParam(qnm, key, val);
  }
}
