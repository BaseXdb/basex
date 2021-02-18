package org.basex.http.restxq;

import static org.basex.http.web.WebText.*;
import static org.basex.query.QueryError.*;
import static org.basex.query.ann.Annotation.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.*;

import javax.servlet.http.*;

import org.basex.build.csv.*;
import org.basex.build.html.*;
import org.basex.build.json.*;
import org.basex.build.text.*;
import org.basex.core.*;
import org.basex.http.*;
import org.basex.http.web.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.util.hash.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.http.*;
import org.basex.util.list.*;
import org.basex.util.options.*;

/**
 * This class represents a single RESTXQ function.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class RestXqFunction extends WebFunction {
  /** EQName pattern. */
  private static final Pattern EQNAME = Pattern.compile("^Q\\{(.*?)}(.*)$");

  /** Query parameters. */
  final ArrayList<WebParam> queryParams = new ArrayList<>();
  /** Form parameters. */
  final ArrayList<WebParam> formParams = new ArrayList<>();
  /** Returned media types. */
  public final ArrayList<MediaType> produces = new ArrayList<>();

  /** Supported methods. */
  final Set<String> methods = new HashSet<>();
  /** Permissions (can be empty). */
  final TokenList allows = new TokenList();

  /** Query parameters. */
  private final ArrayList<WebParam> errorParams = new ArrayList<>();

  /** Cookie parameters. */
  private final ArrayList<WebParam> cookieParams = new ArrayList<>();
  /** Consumed media types. */
  private final ArrayList<MediaType> consumes = new ArrayList<>();

  /** Path (can be {@code null}). */
  public RestXqPath path;
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
   * @param module web module
   */
  public RestXqFunction(final StaticFunc function, final QueryContext qc, final WebModule module) {
    super(function, module, qc);
  }

  @Override
  public boolean parse(final Context ctx) throws QueryException, IOException {
    // parse all annotations
    final boolean[] declared = new boolean[function.params.length];
    boolean found = false;
    final MainOptions options = ctx.options;

    final AnnList starts = new AnnList();
    for(final Ann ann : function.anns) {
      final Annotation sig = ann.sig;
      if(sig == null) continue;

      found |= eq(sig.uri, QueryText.REST_URI, QueryText.PERM_URI);
      final Item[] args = ann.args();
      if(sig == _REST_PATH) {
        try {
          path = new RestXqPath(toString(args[0]), ann.info);
          starts.add(ann);
        } catch(final IllegalArgumentException ex) {
          throw error(ann.info, ex.getMessage());
        }
        for(final QNm name : path.varNames()) {
          checkVariable(name, declared);
        }
      } else if(sig == _REST_ERROR) {
        error(ann);
        // function can have multiple error annotations
        if(!starts.contains(sig)) starts.add(ann);
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
        starts.add(ann);
      }
    }

    // check validity of quality factors
    for(final MediaType produce : produces) {
      final String qf = produce.parameters().get("qs");
      if(qf != null) {
        final double d = toDouble(token(qf));
        // NaN will be included if negated condition is used...
        if(!(d >= 0 && d <= 1)) throw error(ERROR_QS_X, qf);
      }
    }
    return checkParsed(found, starts, declared);
  }

  /**
   * Binds the annotated variables.
   * @param args arguments
   * @param ext extended processing information (can be {@code null})
   * @param conn HTTP connection
   * @param qc query context
   * @throws QueryException exception
   * @throws IOException I/O exception
   */
  public void bind(final Expr[] args, final Object ext, final HTTPConnection conn,
      final QueryContext qc) throws QueryException, IOException {

    // bind variables from segments
    if(path != null) {
      final QNmMap<String> qnames = path.values(conn);
      for(final QNm qname : qnames) {
        final QNm qnm = new QNm(qname.string(), function.sc);
        if(function.sc.elemNS != null && eq(qnm.uri(), function.sc.elemNS)) qnm.uri(EMPTY);
        bind(qnm, args, new Atm(qnames.get(qname)), qc, "Path segment");
      }
    }

    // bind request body in the correct format
    final MainOptions mopts = conn.context.options;
    if(requestBody != null) {
      final MediaType mt = conn.mediaType();
      final IOContent payload = conn.requestCtx.payload();
      final Value value;
      try {
        value = HttpPayload.value(payload, mopts, mt);
      } catch(final IOException ex) {
        throw error(BODY_TYPE_X_X, mt, ex);
      }
      bind(requestBody, args, value, qc, "Request body");
    }

    // bind query and form parameters
    for(final WebParam rxp : queryParams) {
      bind(rxp, args, conn.requestCtx.queryValues().get(rxp.name), qc);
    }
    for(final WebParam rxp : formParams) {
      bind(rxp, args, conn.requestCtx.formValues(mopts).get(rxp.name), qc);
    }

    // bind header parameters
    for(final WebParam rxp : headerParams) {
      final TokenList tl = new TokenList();
      final Enumeration<?> en = conn.request.getHeaders(rxp.name);
      while(en.hasMoreElements()) {
        for(final String s : en.nextElement().toString().split(", *")) tl.add(s);
      }
      bind(rxp, args, StrSeq.get(tl), qc);
    }

    // bind cookie parameters
    final Cookie[] ck = conn.request.getCookies();
    for(final WebParam rxp : cookieParams) {
      Value value = Empty.VALUE;
      if(ck != null) {
        for(final Cookie c : ck) {
          if(rxp.name.equals(c.getName())) value = Str.get(c.getValue());
        }
      }
      bind(rxp, args, value, qc);
    }

    // bind errors
    final Map<String, Value> errs = new HashMap<>();
    if(ext instanceof QueryException) {
      final Value[] values = Catch.values((QueryException) ext);
      final QNm[] names = Catch.NAMES;
      final int nl = names.length;
      for(int n = 0; n < nl; n++) errs.put(string(names[n].local()), values[n]);
    }
    for(final WebParam rxp : errorParams) bind(rxp, args, errs.get(rxp.name), qc);

    // bind permission information
    if(ext instanceof RestXqFunction && permission.var != null) {
      bind(permission.var, args, RestXqPerm.map((RestXqFunction) ext, conn), qc, "Error info");
    }
  }

  /**
   * Checks if an HTTP request matches this function and its constraints.
   * @param conn HTTP connection
   * @param err error code (assigned if error function is to be called)
   * @param perm permission flag
   * @return result of check
   */
  public boolean matches(final HTTPConnection conn, final QNm err, final boolean perm) {
    // check method, consumed and produced media type, and path or error
    if(!((methods.isEmpty() || methods.contains(conn.method)) && consumes(conn) &&
        produces(conn))) return false;

    if(perm) return permission != null && permission.matches(conn);
    if(err != null) return error != null && error.matches(err);
    return path != null && path.matches(conn);
  }

  /**
   * Returns the most specific consume type for the specified type.
   * @param type media type
   * @return most specific type
   */
  public MediaType consumedType(final MediaType type) {
    MediaType mt = null;
    for(final MediaType consume : consumes) {
      if(type.matches(consume) && (mt == null || mt.compareTo(consume) > 0)) mt = consume;
    }
    return mt == null ? MediaType.ALL_ALL : mt;
  }

  @Override
  public QueryException error(final String msg, final Object... ext) {
    return error(function.info, msg, ext);
  }

  /**
   * Creates an exception with the specified message.
   * @param ii input info
   * @param msg error message
   * @param ext error extension
   * @return QueryException query exception
   */
  static QueryException error(final InputInfo ii, final String msg, final Object... ext) {
    return BASEX_RESTXQ_X.get(ii, Util.info(msg, ext));
  }

  @Override
  public int compareTo(final WebFunction func) {
    if(!(func instanceof RestXqFunction)) return -1;

    final RestXqFunction rxf = (RestXqFunction) func;
    if(path != null) return path.compareTo(rxf.path);
    if(error != null) return error.compareTo(rxf.error);
    return permission.compareTo(rxf.permission);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder().append(super.toString());
    if(!produces.isEmpty()) sb.append(' ').append(produces);
    return sb.toString();
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Assigns annotation values as options.
   * @param <O> option type
   * @param opts options instance
   * @param ann annotation
   * @return options instance
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private static <O extends Options> O parse(final O opts, final Ann ann)
      throws QueryException, IOException {
    for(final Item arg : ann.args()) opts.assign(string(arg.string(ann.info)));
    return opts;
  }

  /**
   * Adds an HTTP method to the list of supported methods by this RESTXQ function.
   * @param method HTTP method as a string
   * @param body variable to which the HTTP request body to be bound (optional)
   * @param declared variable declaration flags
   * @param ii input info
   * @throws QueryException query exception
   */
  private void addMethod(final String method, final Item body, final boolean[] declared,
      final InputInfo ii) throws QueryException {

    if(body != null) {
      final HttpMethod m = HttpMethod.get(method);
      if(m != null && !m.body) throw error(ii, METHOD_VALUE_X, m);
      if(requestBody != null) throw error(ii, ANN_BODYVAR);
      requestBody = checkVariable(toString(body), declared);
    }
    if(methods.contains(method)) throw error(ii, ANN_TWICE_X_X, "%", method);
    methods.add(method);
  }

  /**
   * Checks if the consumed content type matches.
   * @param conn HTTP connection
   * @return result of check
   */
  private boolean consumes(final HTTPConnection conn) {
    // check if any combination matches
    final MediaType mt = conn.mediaType();
    for(final MediaType consume : consumes) {
      if(mt.matches(consume)) return true;
    }
    // return true if no type is given
    return consumes.isEmpty();
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
   * @param param parameter
   * @param args argument array
   * @param value values to be bound; the parameter's default value is assigned
   *        if the argument is {@code null} or empty
   * @param qc query context
   * @throws QueryException query exception
   */
  private void bind(final WebParam param, final Expr[] args, final Value value,
      final QueryContext qc) throws QueryException {
    bind(param.var, args, value == null || value.isEmpty() ? param.value : value, qc,
      "Value of \"" + param.name + '"');
  }

  /**
   * Adds items to the specified list.
   * @param ann annotation
   * @param list list to add values to
   */
  private static void strings(final Ann ann, final ArrayList<MediaType> list) {
    for(final Item arg : ann.args()) list.add(new MediaType(toString(arg)));
  }

  /**
   * Returns a parameter.
   * @param ann annotation
   * @param declared variable declaration flags
   * @return parameter
   * @throws QueryException query exception
   */
  private WebParam param(final Ann ann, final boolean... declared) throws QueryException {
    // name of parameter
    final Item[] args = ann.args();
    final String name = toString(args[0]);
    // variable template
    final QNm var = checkVariable(toString(args[1]), declared);
    // default value
    final int al = args.length;
    final ItemList items = new ItemList(al - 2);
    for(int a = 2; a < al; a++) items.add(args[a]);
    return new WebParam(var, name, items.value());
  }

  /**
   * Creates an error function.
   * @param ann annotation
   * @throws QueryException query exception
   */
  private void error(final Ann ann) throws QueryException {
    if(error == null) error = new RestXqError();

    // name of parameter
    for(final Item arg : ann.args()) {
      final String err = toString(arg);
      final QNm name;
      final NamePart part;
      if(err.equals("*")) {
        name = null;
        part = null;
      } else if(err.startsWith("*:")) {
        final byte[] local = token(err.substring(2));
        if(!XMLToken.isNCName(local)) throw error(INV_CODE_X, err);
        name = new QNm(local);
        part = NamePart.LOCAL;
      } else if(err.endsWith(":*")) {
        final byte[] prefix = token(err.substring(0, err.length() - 2));
        if(!XMLToken.isNCName(prefix)) throw error(INV_CODE_X, err);
        name = new QNm(concat(prefix, COLON), function.sc);
        part = NamePart.URI;
      } else {
        final Matcher m = EQNAME.matcher(err);
        if(m.matches()) {
          final byte[] uri = token(m.group(1)), local = token(m.group(2));
          if(local.length == 1 && local[0] == '*') {
            name = new QNm(COLON, uri);
            part = NamePart.URI;
          } else {
            if(!XMLToken.isNCName(local) || !Uri.uri(uri).isValid()) throw error(INV_CODE_X, err);
            name = new QNm(local, uri);
            part = NamePart.FULL;
          }
        } else {
          final byte[] nm = token(err);
          if(!XMLToken.isQName(nm)) throw error(INV_CODE_X, err);
          name = new QNm(nm, function.sc);
          part = NamePart.FULL;
        }
      }

      // message
      if(name != null && name.hasPrefix() && !name.hasURI()) throw error(INV_NONS_X, name);
      final NameTest test = part != null ?
        new NameTest(name, part, NodeType.ELEMENT, null) : null;

      final Function<NameTest, String> toString = t -> t != null ? t.toString() : "*";
      if(!error.isEmpty()) {
        final NameTest first = error.get(0);
        if(first != null ? first.part() != part : part != null) {
          throw error(INV_PRECEDENCE_X_X, toString.apply(first), toString.apply(test));
        }
      }
      if(!error.add(test)) throw error(INV_ERR_TWICE_X, toString.apply(test));
    }
  }
}
