package org.basex.http.restxq;

import static org.basex.http.HTTPMethod.*;
import static org.basex.http.restxq.RestXqText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.Set;
import java.util.regex.*;

import javax.servlet.http.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.io.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.path.*;
import org.basex.query.path.Test.Kind;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class represents a single RESTXQ function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class RestXqFunction implements Comparable<RestXqFunction> {
  /** Single template pattern. */
  private static final Pattern TEMPLATE = Pattern.compile("\\s*\\{\\s*\\$(.+?)\\s*\\}\\s*");
  /** EQName pattern. */
  private static final Pattern EQNAME = Pattern.compile("^Q\\{(.*?)\\}(.*)$");

  /** Supported methods. */
  final Set<String> methods = new HashSet<>();
  /** Serialization parameters. */
  final SerializerOptions output;
  /** Associated function. */
  final StaticFunc function;
  /** Associated module. */
  private final RestXqModule module;
  /** Path. */
  RestXqPath path;

  /** Error. */
  private RestXqError error;
  /** Query parameters. */
  private final ArrayList<RestXqParam> errorParams = new ArrayList<>();

  /** Query parameters. */
  final ArrayList<RestXqParam> queryParams = new ArrayList<>();
  /** Form parameters. */
  final ArrayList<RestXqParam> formParams = new ArrayList<>();
  /** Header parameters. */
  final ArrayList<RestXqParam> headerParams = new ArrayList<>();
  /** Cookie parameters. */
  private final ArrayList<RestXqParam> cookieParams = new ArrayList<>();

  /** Query context. */
  private final QueryContext qc;
  /** Consumed media types. */
  private final StringList consumes = new StringList();
  /** Returned media types. */
  private final StringList produces = new StringList();
  /** Post/Put variable. */
  private QNm requestBody;

  /**
   * Constructor.
   * @param function associated user function
   * @param qc query context
   * @param module associated module
   */
  RestXqFunction(final StaticFunc function, final QueryContext qc, final RestXqModule module) {
    this.function = function;
    this.qc = qc;
    this.module = module;
    output = qc.serParams();
  }

  /**
   * Processes the HTTP request.
   * Parses new modules and discards obsolete ones.
   * @param http HTTP context
   * @param exc optional query exception
   * @throws Exception exception
   */
  void process(final HTTPContext http, final QueryException exc) throws Exception {
    try {
      module.process(http, this, exc);
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
  boolean parse() throws QueryException {
    // parse all annotations
    final boolean[] declared = new boolean[function.args.length];
    boolean found = false;
    final int as = function.ann.size();
    for(int a = 0; a < as; a++) {
      final QNm name = function.ann.names[a];
      final Value value = function.ann.values[a];
      final InputInfo info = function.ann.infos[a];
      final byte[] local = name.local();
      final byte[] uri = name.uri();
      final boolean rexq = eq(uri, QueryText.RESTURI);
      if(rexq) {
        if(eq(PATH, local)) {
          // annotation "path"
          if(path != null) throw error(info, ANN_TWICE, "%", name.string());
          try {
            path = new RestXqPath(toString(value, name));
          } catch(final IllegalArgumentException ex) {
            throw error(info, ex.getMessage());
          }
          for(int s = 0; s < path.size; s++) {
            if(path.isTemplate(s)) checkVariable(path.segment[s], AtomType.AAT, declared);
          }
        } else if(eq(ERROR, local)) {
          // annotation "error"
          error(value, name);
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
        } else if(eq(ERROR_PARAM, local)) {
          // annotation "error-param"
          errorParams.add(param(value, name, declared));
        } else if(eq(METHOD, local)) {
          // annotation "method"
          if(value.size() < 1) throw error(function.info, ANN_ATLEAST, "%", name.string(), 1);
          final String mth = toString(value.itemAt(0), name).toUpperCase(Locale.ENGLISH);
          final Value body = value.size() > 1 ? value.itemAt(1) : null;
          addMethod(mth, body, name, declared, info);
        } else {
          // method annotations
          final String mth = string(local);
          if(get(mth) == null) throw error(info, ANN_UNKNOWN, "%", name.string());
          addMethod(mth, value, name, declared, info);
        }
      } else if(eq(uri, QueryText.OUTPUTURI)) {
        // serialization parameters
        try {
          output.assign(string(local), toString(value, name));
        } catch(final BaseXException ex) {
          throw error(info, UNKNOWN_SER, local);
        }
      }
      found |= rexq;
    }

    if(found) {
      if(path == null && error == null)
        throw error(function.info, ANN_MISSING, '%', PATH, '%', ERROR);

      for(int i = 0; i < declared.length; i++) {
        if(declared[i]) continue;
        throw error(function.info, VAR_UNDEFINED, function.args[i].name.string());
      }
    }
    return found;
  }

  /**
   * Add a HTTP method to the list of supported methods by this RESTXQ function.
   * @param method HTTP method as a string
   * @param body variable to which the HTTP request body to be bound (optional)
   * @param ann RESTXQ annotation specifying the HTTP method
   * @param declared variable declaration flags
   * @param info input info
   * @throws QueryException query exception
   */
  private void addMethod(final String method, final Value body, final QNm ann,
      final boolean[] declared, final InputInfo info) throws QueryException {

    if(body != null && !body.isEmpty()) {
      final HTTPMethod m = get(method);
      if(m != null && !m.body) throw error(info, METHOD_VALUE, m);
      if(requestBody != null) throw error(info, ANN_BODYVAR);
      requestBody = checkVariable(toString(body, ann), declared);
    }
    if(methods.contains(method)) throw error(info, ANN_TWICE, "%", ann.string());
    methods.add(method);
  }

  /**
   * Checks if an HTTP request matches this function and its constraints.
   * @param http http context
   * @param err error code
   * @return result of check
   */
  boolean matches(final HTTPContext http, final QNm err) {
    // check method, consumed and produced media type, and path or error
    return (methods.isEmpty() || methods.contains(http.method)) && consumes(http) &&
        produces(http) && (err == null ? path != null && path.matches(http) :
          error != null && error.matches(err));
  }

  /**
   * Binds the annotated variables.
   * @param http http context
   * @param arg argument array
   * @param err optional query error
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  void bind(final HTTPContext http, final Expr[] arg, final QueryException err)
      throws QueryException, IOException {

    // bind variables from segments
    if(path != null) {
      for(int s = 0; s < path.size; s++) {
        final Matcher m = TEMPLATE.matcher(path.segment[s]);
        if(m.find()) {
          final QNm qnm = new QNm(token(m.group(1)), function.sc);
          if(function.sc.elemNS != null && eq(qnm.uri(), function.sc.elemNS)) qnm.uri(EMPTY);
          bind(qnm, arg, new Atm(http.segment(s)));
        }
      }
    }

    // bind request body in the correct format
    if(requestBody != null) {
      try {
        bind(requestBody, arg, http.params.content());
      } catch(final IOException ex) {
        throw error(INPUT_CONV, ex);
      }
    }

    // bind query and form parameters
    for(final RestXqParam rxp : queryParams) bind(rxp, arg, http.params.query().get(rxp.key));
    for(final RestXqParam rxp : formParams) bind(rxp, arg, http.params.form().get(rxp.key));

    // bind header parameters
    for(final RestXqParam rxp : headerParams) {
      final TokenList tl = new TokenList();
      final Enumeration<?> en =  http.req.getHeaders(rxp.key);
      while(en.hasMoreElements()) {
        for(final String s : en.nextElement().toString().split(", *")) tl.add(s);
      }
      bind(rxp, arg, StrSeq.get(tl));
    }

    // bind cookie parameters
    final Cookie[] ck = http.req.getCookies();
    for(final RestXqParam rxp : cookieParams) {
      Value val = Empty.SEQ;
      if(ck != null) {
        for(final Cookie c : ck) {
          if(rxp.key.equals(c.getName())) val = Str.get(c.getValue());
        }
      }
      bind(rxp, arg, val);
    }

    // bind errors
    final Map<String, Value> errs = new HashMap<>();
    if(err != null) {
      final Value[] values = Catch.values(err);
      for(int v = 0; v < Catch.NAMES.length; v++) {
        errs.put(string(Catch.NAMES[v].local()), values[v]);
      }
    }
    for(final RestXqParam rxp : errorParams) bind(rxp, arg, errs.get(rxp.key));
  }

  /**
   * Creates an exception with the specified message.
   * @param msg message
   * @param ext error extension
   * @return exception
   */
  QueryException error(final String msg, final Object... ext) {
    return error(function.info, msg, ext);
  }

  /**
   * Creates an exception with the specified message.
   * @param info input info
   * @param msg message
   * @param ext error extension
   * @return exception
   */
  private static QueryException error(final InputInfo info, final String msg, final Object... ext) {
    return BASX_RESTXQ_X.get(info, Util.info(msg, ext));
  }

  @Override
  public int compareTo(final RestXqFunction rxf) {
    return path == null ? error.compareTo(rxf.error) : path.compareTo(rxf.path);
  }

  // PRIVATE METHODS ====================================================================

  /**
   * Checks the specified template and adds a variable.
   * @param tmp template string
   * @param declared variable declaration flags
   * @return resulting variable
   * @throws QueryException query exception
   */
  private QNm checkVariable(final String tmp, final boolean... declared) throws QueryException {
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
  private QNm checkVariable(final String tmp, final Type type, final boolean... declared)
      throws QueryException {

    final Var[] args = function.args;
    final Matcher m = TEMPLATE.matcher(tmp);
    if(!m.find()) throw error(INV_TEMPLATE, tmp);
    final byte[] vn = token(m.group(1));
    if(!XMLToken.isQName(vn)) throw error(INV_VARNAME, vn);
    final QNm name = new QNm(vn);
    if(name.hasPrefix()) name.uri(function.sc.ns.uri(name.prefix()));
    int r = -1;
    while(++r < args.length && !args[r].name.eq(name));
    if(r == args.length) throw error(UNKNOWN_VAR, vn);
    if(declared[r]) throw error(VAR_ASSIGNED, vn);
    final SeqType st = args[r].declaredType();
    if(args[r].checksType() && !st.type.instanceOf(type)) throw error(INV_VARTYPE, vn, type);
    declared[r] = true;
    return name;
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
   * @param value values to be bound; the parameter's default value is assigned
   *        if the argument is {@code null} or empty
   * @throws QueryException query exception
   */
  private void bind(final RestXqParam rxp, final Expr[] args, final Value value)
      throws QueryException {
    bind(rxp.name, args, value == null || value.isEmpty() ? rxp.value : value);
  }

  /**
   * Binds the specified value to a variable.
   * @param name variable name
   * @param args argument array
   * @param value value to be bound
   * @throws QueryException query exception
   */
  private void bind(final QNm name, final Expr[] args, final Value value) throws QueryException {
    // skip nulled values
    if(value == null) return;

    for(int f = 0; f < function.args.length; f++) {
      final Var var = function.args[f];
      if(var.name.eq(name)) {
        // casts and binds the value
        final SeqType decl = var.declaredType();
        final Value val = value.seqType().instanceOf(decl) ? value :
          decl.cast(value, qc, function.sc, null);
        args[f] = var.checkType(val, qc, null, false);
        break;
      }
    }
  }

  /**
   * Returns the specified value as a string.
   * @param value value
   * @param name name
   * @return string
   * @throws QueryException HTTP exception
   */
  private String toString(final Value value, final QNm name) throws QueryException {
    if(value instanceof Str) return ((Str) value).toJava();
    throw error(function.info, ANN_STRING, "%", name.string(), value);
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
  private RestXqParam param(final Value value, final QNm name, final boolean... declared)
      throws QueryException {

    final long vs = value.size();
    if(vs < 2) throw error(function.info, ANN_ATLEAST, "%", name.string(), 2);
    // name of parameter
    final String key = toString(value.itemAt(0), name);
    // variable template
    final QNm qnm = checkVariable(toString(value.itemAt(1), name), declared);
    // default value
    final ValueBuilder vb = new ValueBuilder();
    for(int v = 2; v < vs; v++) vb.add(value.itemAt(v));
    return new RestXqParam(qnm, key, vb.value());
  }

  /**
   * Returns an error.
   * @param value value
   * @param name name
   * @throws QueryException HTTP exception
   */
  private void error(final Value value, final QNm name) throws QueryException {
    if(value.isEmpty()) throw error(function.info, ANN_ATLEAST, "%", name.string(), 1);

    if(error == null) error = new RestXqError();

    // name of parameter
    final int s = (int) value.size();
    NameTest last = error.get(0);
    for(int i = 0; i < s; i++) {
      final String err = toString(value.itemAt(i), name);
      final Kind kind;
      QNm qnm = null;
      if(err.equals("*")) {
        kind = Kind.WILDCARD;
      } else if(err.startsWith("*:")) {
        final byte[] local = token(err.substring(2));
        if(!XMLToken.isNCName(local)) throw error(INV_CODE, err);
        qnm = new QNm(local);
        kind = Kind.NAME;
      } else if(err.endsWith(":*")) {
        final byte[] prefix = token(err.substring(0, err.length() - 2));
        if(!XMLToken.isNCName(prefix)) throw error(INV_CODE, err);
        qnm = new QNm(concat(prefix, COLON), function.sc);
        kind = Kind.URI;
      } else {
        final Matcher m = EQNAME.matcher(err);
        if(m.matches()) {
          final byte[] uri = token(m.group(1));
          final byte[] local = token(m.group(2));
          if(local.length == 1 && local[0] == '*') {
            qnm = new QNm(COLON, uri);
            kind = Kind.URI;
          } else {
            if(!XMLToken.isNCName(local) || !Uri.uri(uri).isValid()) throw error(INV_CODE, err);
            qnm = new QNm(local, uri);
            kind = Kind.URI_NAME;
          }
        } else {
          final byte[] nm = token(err);
          if(!XMLToken.isQName(nm)) throw error(INV_CODE, err);
          qnm = new QNm(nm, function.sc);
          kind = Kind.URI_NAME;
        }
      }
      // message
      if(qnm != null && qnm.hasPrefix() && !qnm.hasURI()) throw error(INV_NONS, qnm);
      final NameTest test = new NameTest(qnm, kind, false, null);
      if(last != null && last.kind != kind) throw error(INV_PRIORITY, last, test);
      if(!error.add(test)) throw error(INV_ERR_SAME, last);
      last = test;
    }
  }
}
