package org.basex.http.restxq;

import static org.basex.http.restxq.RestXqText.*;
import static org.basex.query.QueryError.*;
import static org.basex.query.ann.Annotation.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.Map.*;
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
 * @author BaseX Team 2005-17, BSD License
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

  /** Associated module. */
  private final RestXqModule module;
  /** Query parameters. */
  private final ArrayList<RestXqParam> errorParams = new ArrayList<>();

  /** Cookie parameters. */
  private final ArrayList<RestXqParam> cookieParams = new ArrayList<>();
  /** Consumed media types. */
  private final ArrayList<MediaType> consumes = new ArrayList<>();

  /** Path. */
  RestXqPath path;
  /** Singleton id (can be {@code null}). */
  String singleton;
  /** Error. */
  private RestXqError error;
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
    this.module = module;
    output = qc.serParams();
  }

  /**
   * Processes the HTTP request.
   * Parses new modules and discards obsolete ones.
   * @param conn HTTP connection
   * @param qe query exception (optional)
   * @throws Exception exception
   */
  void process(final HTTPConnection conn, final QueryException qe) throws Exception {
    try {
      module.process(conn, this, qe);
    } catch(final QueryException ex) {
      if(ex.file() == null) ex.info(function.info);
      throw ex;
    }
  }

  /**
   * Checks a function for RESTFful annotations.
   * @param ctx database context
   * @return {@code true} if module contains relevant annotations
   * @throws Exception exception
   */
  boolean parse(final Context ctx) throws Exception {
    // parse all annotations
    final boolean[] declared = new boolean[function.args.length];
    boolean found = false;
    final MainOptions options = ctx.options;
    for(final Ann ann : function.anns) {
      final Annotation sig = ann.sig;
      if(sig == null) continue;

      found |= eq(sig.uri, QueryText.REST_URI);
      final Item[] args = ann.args();
      if(sig == _REST_PATH) {
        try {
          path = new RestXqPath(toString(args[0]), ann.info);
        } catch(final IllegalArgumentException ex) {
          throw error(ann.info, ex.getMessage());
        }
        for(final QNm v : path.vars()) checkVariable(v, AtomType.AAT, declared);
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
          throw error(ann.info, UNKNOWN_SER, sig.local());
        }
      }
    }

    if(found) {
      if(path == null && error == null)
        throw error(function.info, ANN_MISSING, '%', PATH, '%', ERROR);

      final int dl = declared.length;
      for(int d = 0; d < dl; d++) {
        if(declared[d]) continue;
        throw error(function.info, VAR_UNDEFINED, function.args[d].name.string());
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
   * @param ii input info
   * @throws QueryException query exception
   */
  private void addMethod(final String method, final Item body, final boolean[] declared,
      final InputInfo ii) throws QueryException {

    if(body != null && !body.isEmpty()) {
      final HttpMethod m = HttpMethod.get(method);
      if(m != null && !m.body) throw error(ii, METHOD_VALUE, m);
      if(requestBody != null) throw error(ii, ANN_BODYVAR);
      requestBody = checkVariable(toString(body), declared);
    }
    if(methods.contains(method)) throw error(ii, ANN_TWICE, "%", method);
    methods.add(method);
  }

  /**
   * Checks if an HTTP request matches this function and its constraints.
   * @param conn HTTP connection
   * @param err error code
   * @return result of check
   */
  boolean matches(final HTTPConnection conn, final QNm err) {
    // check method, consumed and produced media type, and path or error
    return (methods.isEmpty() || methods.contains(conn.method)) && consumes(conn) &&
        produces(conn) && (err == null ? path != null && path.matches(conn) :
          error != null && error.matches(err));
  }

  /**
   * Binds the annotated variables.
   * @param conn HTTP connection
   * @param arg argument array
   * @param err optional query error
   * @param qc query context
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  void bind(final HTTPConnection conn, final Expr[] arg, final QueryException err,
      final QueryContext qc) throws QueryException, IOException {

    // bind variables from segments
    if(path != null) {
      for(final Entry<QNm, String> entry : path.values(conn).entrySet()) {
        final QNm qnm = new QNm(entry.getKey().string(), function.sc);
        if(function.sc.elemNS != null && eq(qnm.uri(), function.sc.elemNS)) qnm.uri(EMPTY);
        bind(qnm, arg, new Atm(entry.getValue()), qc);
      }
    }

    // bind request body in the correct format
    final MainOptions mo = conn.context.options;
    if(requestBody != null) {
      try {
        bind(requestBody, arg, HttpPayload.value(conn.params.body(), mo, conn.contentType()), qc);
      } catch(final IOException ex) {
        throw error(INPUT_CONV, ex);
      }
    }

    // bind query and form parameters
    for(final RestXqParam rxp : queryParams) bind(rxp, arg, conn.params.query().get(rxp.name), qc);
    for(final RestXqParam rxp : formParams) bind(rxp, arg, conn.params.form(mo).get(rxp.name), qc);

    // bind header parameters
    for(final RestXqParam rxp : headerParams) {
      final TokenList tl = new TokenList();
      final Enumeration<?> en =  conn.req.getHeaders(rxp.name);
      while(en.hasMoreElements()) {
        for(final String s : en.nextElement().toString().split(", *")) tl.add(s);
      }
      bind(rxp, arg, StrSeq.get(tl), qc);
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
      bind(rxp, arg, val, qc);
    }

    // bind errors
    final Map<String, Value> errs = new HashMap<>();
    if(err != null) {
      final Value[] values = Catch.values(err);
      final QNm[] names = Catch.NAMES;
      final int nl = names.length;
      for(int n = 0; n < nl; n++) errs.put(string(names[n].local()), values[n]);
    }
    for(final RestXqParam rxp : errorParams) bind(rxp, arg, errs.get(rxp.name), qc);
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
   * @param ii input info
   * @param msg message
   * @param ext error extension
   * @return exception
   */
  private static QueryException error(final InputInfo ii, final String msg, final Object... ext) {
    return BASX_RESTXQ_X.get(ii, Util.info(msg, ext));
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

    final Matcher m = TEMPLATE.matcher(tmp);
    if(!m.find()) throw error(INV_TEMPLATE, tmp);
    final byte[] vn = token(m.group(1));
    if(!XMLToken.isQName(vn)) throw error(INV_VARNAME, vn);
    final QNm name = new QNm(vn);
    return checkVariable(name, type, declared);
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
    int a = -1;
    final Var[] args = function.args;
    final int al = args.length;
    while(++a < al && !args[a].name.eq(name));

    if(a == args.length) throw error(UNKNOWN_VAR, name.string());
    if(declared[a]) throw error(VAR_ASSIGNED, name.string());

    final SeqType st = args[a].declaredType();
    if(args[a].checksType() && !st.type.instanceOf(type))
      throw error(INV_VARTYPE, name.string(), type);

    declared[a] = true;
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
   * @param args argument array
   * @param value value to be bound
   * @param qc query context
   * @throws QueryException query exception
   */
  private void bind(final QNm name, final Expr[] args, final Value value, final QueryContext qc)
      throws QueryException {

    // skip nulled values
    if(value == null) return;

    final Var[] fargs = function.args;
    final int fl = fargs.length;
    for(int f = 0; f < fl; f++) {
      final Var var = fargs[f];
      if(var.name.eq(name)) {
        // casts and binds the value
        final SeqType decl = var.declaredType();
        final Value val = value.seqType().instanceOf(decl) ? value :
          decl.cast(value, qc, function.sc, null);
        args[f] = var.checkType(val, qc, false);
        break;
      }
    }
  }

  /**
   * Returns the specified item as a string.
   * @param item item
   * @return string
   */
  private static String toString(final Item item) {
    return ((Str) item).toJava();
  }

  /**
   * Adds items to the specified list.
   * @param ann annotation
   * @param list list to add values to
   */
  private static void strings(final Ann ann, final ArrayList<MediaType> list) {
    for(final Item it : ann.args()) list.add(new MediaType(toString(it)));
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
    final ValueBuilder vb = new ValueBuilder();
    for(int a = 2; a < al; a++) vb.add(args[a]);
    return new RestXqParam(var, name, vb.value());
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
