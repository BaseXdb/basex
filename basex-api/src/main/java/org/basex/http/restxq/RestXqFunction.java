package org.basex.http.restxq;

import static org.basex.http.restxq.RestXqText.*;
import static org.basex.query.QueryError.*;
import static org.basex.query.ann.Annotation.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.Set;
import java.util.regex.*;

import javax.servlet.http.*;

import org.basex.build.csv.*;
import org.basex.build.html.*;
import org.basex.build.json.*;
import org.basex.build.text.*;
import org.basex.core.*;
import org.basex.http.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.query.expr.path.Test.*;
import org.basex.query.func.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.http.*;
import org.basex.util.list.*;
import org.basex.util.options.*;

/**
 * This class represents a single RESTXQ function.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
final class RestXqFunction implements Comparable<RestXqFunction> {
  /** Single template pattern. */
  private static final Pattern TEMPLATE = Pattern.compile("\\s*\\{\\s*\\$(.+?)\\s*}\\s*");
  /** EQName pattern. */
  private static final Pattern EQNAME = Pattern.compile("^Q\\{(.*?)}(.*)$");

  /** Query parameters. */
  final ArrayList<RestXqParam> queryParams = new ArrayList<>();
  /** Form parameters. */
  final ArrayList<RestXqParam> formParams = new ArrayList<>();
  /** Header parameters. */
  final ArrayList<RestXqParam> headerParams = new ArrayList<>();
  /** Returned media types. */
  final ArrayList<MediaType> produces = new ArrayList<>();

  /** Supported methods. */
  final Set<String> methods = new HashSet<>();
  /** Serialization parameters. */
  final SerializerOptions output;
  /** Associated function. */
  final StaticFunc function;
  /** Permissions (can be empty). */
  final TokenList allows = new TokenList();

  /** Associated module. */
  private final RestXqModule module;
  /** Query parameters. */
  private final ArrayList<RestXqParam> errorParams = new ArrayList<>();

  /** Cookie parameters. */
  private final ArrayList<RestXqParam> cookieParams = new ArrayList<>();
  /** Consumed media types. */
  private final ArrayList<MediaType> consumes = new ArrayList<>();

  /** Path (can be {@code null}). */
  RestXqPath path;
  /** Singleton id (can be {@code null}). */
  String singleton;

  /** Post/Put variable (can be {@code null}). */
  private QNm requestBody;

  /** Error (can be {@code null}). */
  private RestXqError error;
  /** Error (can be {@code null}). */
  private RestXqPerm permission;

  /**
   * Constructor.
   * @param function associated user function
   * @param qc query context
   * @param module associated module
   */
  RestXqFunction(final StaticFunc function, final QueryContext qc, final RestXqModule module) {
    this.function = function;
    this.module = module;
    output = qc.serParams();
  }

  /**
   * Processes the HTTP request.
   * Parses new modules and discards obsolete ones.
   * @param conn HTTP connection
   * @param ext extended processing information (can be {@code null})
   * @return {@code true} if function creates no result
   * @throws Exception exception
   */
  boolean process(final HTTPConnection conn, final Object ext) throws Exception {
    try {
      return module.process(conn, this, ext);
    } catch(final QueryException ex) {
      if(ex.file() == null) ex.info(function.info);
      throw ex;
    }
  }

  /**
   * Checks a function for REST and permission annotations.
   * @param ctx database context
   * @return {@code true} if function contains relevant annotations
   * @throws Exception exception
   */
  boolean parse(final Context ctx) throws Exception {
    // parse all annotations
    final boolean[] declared = new boolean[function.params.length];
    boolean found = false;
    final MainOptions options = ctx.options;

    for(final Ann ann : function.anns) {
      final Annotation sig = ann.sig;
      if(sig == null) continue;

      found |= eq(sig.uri, QueryText.REST_URI, QueryText.PERM_URI);
      final Item[] args = ann.args();
      if(sig == _REST_PATH) {
        try {
          path = new RestXqPath(toString(args[0]), ann.info);
        } catch(final IllegalArgumentException ex) {
          throw error(ann.info, ex.getMessage());
        }
        for(final QNm name : path.varNames()) checkVariable(name, AtomType.AAT, declared);
      } else if(sig == _REST_ERROR) {
        error(ann);
      } else if(sig == _REST_CONSUMES) {
        strings(ann, consumes);
      } else if(sig == _REST_PRODUCES) {
        strings(ann, produces);
      } else if(sig == _REST_QUERY_PARAM) {
        queryParams.add(param(ann, declared));
      } else if(sig == _REST_FORM_PARAM) {
        formParams.add(param(ann, declared));
      } else if(sig == _REST_HEADER_PARAM) {
        headerParams.add(param(ann, declared));
      } else if(sig == _REST_COOKIE_PARAM) {
        cookieParams.add(param(ann, declared));
      } else if(sig == _REST_ERROR_PARAM) {
        errorParams.add(param(ann, declared));
      } else if(sig == _REST_METHOD) {
        final String mth = toString(args[0]).toUpperCase(Locale.ENGLISH);
        final Item body = args.length > 1 ? args[1] : null;
        addMethod(mth, body, declared, ann.info);
      } else if(sig == _REST_SINGLE) {
        singleton = '\u0001' + (args.length > 0 ? toString(args[0]) :
          (function.info.path() + ':' + function.info.line()));
      } else if(eq(sig.uri, QueryText.REST_URI)) {
        final Item body = args.length == 0 ? null : args[0];
        addMethod(string(sig.local()), body, declared, ann.info);
      } else if(sig == _INPUT_CSV) {
        final CsvParserOptions opts = new CsvParserOptions(options.get(MainOptions.CSVPARSER));
        options.set(MainOptions.CSVPARSER, parse(opts, ann));
      } else if(sig == _INPUT_JSON) {
        final JsonParserOptions opts = new JsonParserOptions(options.get(MainOptions.JSONPARSER));
        options.set(MainOptions.JSONPARSER, parse(opts, ann));
      } else if(sig == _INPUT_HTML) {
        final HtmlOptions opts = new HtmlOptions(options.get(MainOptions.HTMLPARSER));
        options.set(MainOptions.HTMLPARSER, parse(opts, ann));
      } else if(sig == _INPUT_TEXT) {
        final TextOptions opts = new TextOptions(options.get(MainOptions.TEXTPARSER));
        options.set(MainOptions.TEXTPARSER, parse(opts, ann));
      } else if(eq(sig.uri, QueryText.OUTPUT_URI)) {
        // serialization parameters
        try {
          output.assign(string(sig.local()), toString(args[0]));
        } catch(final BaseXException ex) {
          Util.debug(ex);
          throw error(ann.info, UNKNOWN_SER_X, sig.local());
        }
      } else if(sig == _PERM_ALLOW) {
        for(final Item arg : args) allows.add(toString(arg));
      } else if(sig == _PERM_CHECK) {
        final String p = args.length > 0 ? toString(args[0]) : "";
        final QNm v = args.length > 1 ? checkVariable(toString(args[1]), declared) : null;
        permission = new RestXqPerm(p, v);
      }
    }

    if(found) {
      final int paths = (path != null ? 1 : 0) + (error != null ? 1 : 0) +
          (permission != null ? 1 : 0);
      if(paths != 1) throw error(function.info, paths == 0 ? ANN_MISSING : ANN_CONFLICT);

      final int dl = declared.length;
      for(int d = 0; d < dl; d++) {
        if(declared[d]) continue;
        throw error(function.info, VAR_UNDEFINED_X, function.params[d].name.string());
      }
    }
    return found;
  }

  /**
   * Assigns annotation values as options.
   * @param <O> option type
   * @param opts options instance
   * @param ann annotation
   * @return options instance
   * @throws Exception any exception
   */
  private static <O extends Options> O parse(final O opts, final Ann ann) throws Exception {
    for(final Item arg : ann.args()) opts.assign(string(arg.string(ann.info)));
    return opts;
  }

  /**
   * Add a HTTP method to the list of supported methods by this RESTXQ function.
   * @param method HTTP method as a string
   * @param body variable to which the HTTP request body to be bound (optional)
   * @param declared variable declaration flags
   * @param info input info
   * @throws QueryException query exception
   */
  private void addMethod(final String method, final Item body, final boolean[] declared,
      final InputInfo info) throws QueryException {

    if(body != null && !body.isEmpty()) {
      final HttpMethod m = HttpMethod.get(method);
      if(m != null && !m.body) throw error(info, METHOD_VALUE_X, m);
      if(requestBody != null) throw error(info, ANN_BODYVAR);
      requestBody = checkVariable(toString(body), declared);
    }
    if(methods.contains(method)) throw error(info, ANN_TWICE_X_X, "%", method);
    methods.add(method);
  }

  /**
   * Checks if an HTTP request matches this function and its constraints.
   * @param conn HTTP connection
   * @param err error code (assigned if error function is to be called)
   * @param perm permission flag
   * @return result of check
   */
  boolean matches(final HTTPConnection conn, final QNm err, final boolean perm) {
    // check method, consumed and produced media type, and path or error
    if(!((methods.isEmpty() || methods.contains(conn.method)) && consumes(conn) &&
        produces(conn))) return false;

    if(perm) return permission != null && permission.matches(conn);
    if(err != null) return error != null && error.matches(err);
    return path != null && path.matches(conn);
  }

  /**
   * Binds the annotated variables.
   * @param conn HTTP connection
   * @param args arguments
   * @param ext extended processing information (can be {@code null})
   * @param qc query context
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  void bind(final HTTPConnection conn, final Expr[] args, final Object ext, final QueryContext qc)
      throws QueryException, IOException {

    // bind variables from segments
    if(path != null) {
      for(final Map.Entry<QNm, String> entry : path.values(conn).entrySet()) {
        final QNm qnm = new QNm(entry.getKey().string(), function.sc);
        if(function.sc.elemNS != null && eq(qnm.uri(), function.sc.elemNS)) qnm.uri(EMPTY);
        bind(qnm, args, new Atm(entry.getValue()), qc);
      }
    }

    // bind request body in the correct format
    final MainOptions mo = conn.context.options;
    if(requestBody != null) {
      try {
        bind(requestBody, args, HttpPayload.value(conn.params.body(), mo, conn.contentType()), qc);
      } catch(final IOException ex) {
        throw error(INPUT_CONV_X, ex);
      }
    }

    // bind query and form parameters
    for(final RestXqParam rxp : queryParams) {
      bind(rxp, args, conn.params.map().get(rxp.name), qc);
    }
    for(final RestXqParam rxp : formParams) {
      bind(rxp, args, conn.params.form(mo).get(rxp.name), qc);
    }

    // bind header parameters
    for(final RestXqParam rxp : headerParams) {
      final TokenList tl = new TokenList();
      final Enumeration<?> en =  conn.req.getHeaders(rxp.name);
      while(en.hasMoreElements()) {
        for(final String s : en.nextElement().toString().split(", *")) tl.add(s);
      }
      bind(rxp, args, StrSeq.get(tl), qc);
    }

    // bind cookie parameters
    final Cookie[] ck = conn.req.getCookies();
    for(final RestXqParam rxp : cookieParams) {
      Value val = Empty.SEQ;
      if(ck != null) {
        for(final Cookie c : ck) {
          if(rxp.name.equals(c.getName())) val = Str.get(c.getValue());
        }
      }
      bind(rxp, args, val, qc);
    }

    // bind errors
    final Map<String, Value> errs = new HashMap<>();
    if(ext instanceof QueryException) {
      final Value[] values = Catch.values((QueryException) ext);
      final QNm[] names = Catch.NAMES;
      final int nl = names.length;
      for(int n = 0; n < nl; n++) errs.put(string(names[n].local()), values[n]);
    }
    for(final RestXqParam rxp : errorParams) bind(rxp, args, errs.get(rxp.name), qc);

    // bind permission information
    if(ext instanceof RestXqFunction && permission.var != null) {
      bind(permission.var, args, permission.map((RestXqFunction) ext), qc);
    }
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
    return BASEX_RESTXQ_X.get(info, Util.info(msg, ext));
  }

  @Override
  public int compareTo(final RestXqFunction rxf) {
    if(path != null) return path.compareTo(rxf.path);
    if(error != null) return error.compareTo(rxf.error);
    return permission.compareTo(rxf.permission);
  }

  // PRIVATE METHODS ====================================================================

  /**
   * Checks the specified template and adds a variable.
   * @param tmp template string
   * @param declared variable declaration flags
   * @return resulting variable
   * @throws QueryException query exception
   */
  QNm checkVariable(final String tmp, final boolean... declared) throws QueryException {
    final Matcher m = TEMPLATE.matcher(tmp);
    if(!m.find()) throw error(INV_TEMPLATE_X, tmp);
    final byte[] vn = token(m.group(1));
    if(!XMLToken.isQName(vn)) throw error(INV_VARNAME_X, vn);
    final QNm name = new QNm(vn);
    return checkVariable(name, AtomType.ITEM, declared);
  }

  /**
   * Checks if the specified variable exists in the current function.
   * @param name variable
   * @param type allowed type
   * @param declared variable declaration flags
   * @return resulting variable
   * @throws QueryException query exception
   */
  private QNm checkVariable(final QNm name, final Type type, final boolean[] declared)
      throws QueryException {

    if(name.hasPrefix()) name.uri(function.sc.ns.uri(name.prefix()));
    int p = -1;
    final Var[] params = function.params;
    final int pl = params.length;
    while(++p < pl && !params[p].name.eq(name));

    if(p == params.length) throw error(UNKNOWN_VAR_X, name.string());
    if(declared[p]) throw error(VAR_ASSIGNED_X, name.string());

    final SeqType st = params[p].declaredType();
    if(params[p].checksType() && !st.type.instanceOf(type))
      throw error(INV_VARTYPE_X_X, name.string(), type);

    declared[p] = true;
    return name;
  }

  /**
   * Checks if the consumed content type matches.
   * @param conn HTTP connection
   * @return result of check
   */
  private boolean consumes(final HTTPConnection conn) {
    // return true if no type is given
    if(consumes.isEmpty()) return true;
    // return true if no content type is specified by the user
    final MediaType type = conn.contentType();
    if(type.type().isEmpty()) return true;

    // check if any combination matches
    for(final MediaType consume : consumes) {
      if(consume.matches(type)) return true;
    }
    return false;
  }

  /**
   * Checks if the produced media type matches.
   * @param conn HTTP connection
   * @return result of check
   */
  private boolean produces(final HTTPConnection conn) {
    // return true if no type is given
    if(produces.isEmpty()) return true;
    // check if any combination matches
    for(final MediaType accept : conn.accepts()) {
      for(final MediaType produce : produces) {
        if(produce.matches(accept)) return true;
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
   * @param qc query context
   * @throws QueryException query exception
   */
  private void bind(final RestXqParam rxp, final Expr[] args, final Value value,
      final QueryContext qc) throws QueryException {
    bind(rxp.var, args, value == null || value.isEmpty() ? rxp.value : value, qc);
  }

  /**
   * Binds the specified value to a variable.
   * @param name variable name
   * @param args arguments
   * @param value value to be bound
   * @param qc query context
   * @throws QueryException query exception
   */
  private void bind(final QNm name, final Expr[] args, final Value value, final QueryContext qc)
      throws QueryException {

    // skip nulled values
    if(value == null) return;

    final Var[] params = function.params;
    final int pl = params.length;
    for(int p = 0; p < pl; p++) {
      final Var var = params[p];
      if(var.name.eq(name)) {
        // casts and binds the value
        final SeqType decl = var.declaredType();
        final Value val = value.seqType().instanceOf(decl) ? value :
          decl.cast(value, qc, function.sc, null);
        args[p] = var.checkType(val, qc, false);
        break;
      }
    }
  }

  /**
   * Returns the specified item as a string.
   * @param item item
   * @return string
   */
  static String toString(final Item item) {
    return ((Str) item).toJava();
  }

  /**
   * Adds items to the specified list.
   * @param ann annotation
   * @param list list to add values to
   */
  private static void strings(final Ann ann, final ArrayList<MediaType> list) {
    for(final Item item : ann.args()) list.add(new MediaType(toString(item)));
  }

  /**
   * Returns a parameter.
   * @param ann annotation
   * @param declared variable declaration flags
   * @return parameter
   * @throws QueryException HTTP exception
   */
  private RestXqParam param(final Ann ann, final boolean... declared) throws QueryException {
    // name of parameter
    final Item[] args = ann.args();
    final String name = toString(args[0]);
    // variable template
    final QNm var = checkVariable(toString(args[1]), declared);
    // default value
    final int al = args.length;
    final ItemList items = new ItemList(al - 2);
    for(int a = 2; a < al; a++) items.add(args[a]);
    return new RestXqParam(var, name, items.value());
  }

  /**
   * Creates an error function.
   * @param ann annotation
   * @throws QueryException HTTP exception
   */
  private void error(final Ann ann) throws QueryException {
    if(error == null) error = new RestXqError();

    // name of parameter
    NameTest last = error.get(0);
    for(final Item arg : ann.args()) {
      final String err = toString(arg);
      final Kind kind;
      QNm qnm = null;
      if(err.equals("*")) {
        kind = Kind.WILDCARD;
      } else if(err.startsWith("*:")) {
        final byte[] local = token(err.substring(2));
        if(!XMLToken.isNCName(local)) throw error(INV_CODE_X, err);
        qnm = new QNm(local);
        kind = Kind.NAME;
      } else if(err.endsWith(":*")) {
        final byte[] prefix = token(err.substring(0, err.length() - 2));
        if(!XMLToken.isNCName(prefix)) throw error(INV_CODE_X, err);
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
            if(!XMLToken.isNCName(local) || !Uri.uri(uri).isValid()) throw error(INV_CODE_X, err);
            qnm = new QNm(local, uri);
            kind = Kind.URI_NAME;
          }
        } else {
          final byte[] nm = token(err);
          if(!XMLToken.isQName(nm)) throw error(INV_CODE_X, err);
          qnm = new QNm(nm, function.sc);
          kind = Kind.URI_NAME;
        }
      }
      // message
      if(qnm != null && qnm.hasPrefix() && !qnm.hasURI()) throw error(INV_NONS_X, qnm);
      final NameTest test = new NameTest(qnm, kind, false, null);
      if(last != null && last.kind != kind) throw error(INV_PRIORITY_X_X, last, test);
      if(!error.add(test)) throw error(INV_ERR_SAME_X, last);
      last = test;
    }
  }
}
