package org.basex.http;

import static javax.servlet.http.HttpServletResponse.*;
import static org.basex.data.DataText.*;
import static org.basex.query.func.http.HttpText.*;
import static org.basex.http.HTTPText.*;
import static org.basex.io.MimeTypes.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.basex.*;
import org.basex.build.json.*;
import org.basex.build.json.JsonOptions.*;
import org.basex.core.*;
import org.basex.core.StaticOptions.AuthMethod;
import org.basex.core.users.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.func.http.*;
import org.basex.server.Log.LogType;
import org.basex.server.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Bundles context-based information on a single HTTP operation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class HTTPContext {
  /** Quality factor pattern. */
  private static final Pattern QF = Pattern.compile("^(.*);\\s*q\\s*=(.*)$");

  /** Global static database context. */
  private static Context context;
  /** Initialization flag. */
  private static boolean init;
  /** Initialized failed. */
  private static IOException exception;

  /** Servlet request. */
  public final HttpServletRequest req;
  /** Servlet response. */
  public final HttpServletResponse res;
  /** Request method. */
  public final String method;
  /** Request method. */
  public final HTTPParams params;

  /** Authentication method. */
  public AuthMethod auth;
  /** User name. */
  public String username;
  /** Password (plain text). */
  public String password;

  /** Performance. */
  private final Performance perf = new Performance();
  /** Path, starting with a slash. */
  private final String path;

  /** Serialization parameters. */
  private SerializerOptions sopts;

  /**
   * Constructor.
   * @param req request
   * @param res response
   * @param servlet calling servlet instance
   */
  HTTPContext(final HttpServletRequest req, final HttpServletResponse res,
      final BaseXServlet servlet) {

    this.req = req;
    this.res = res;
    params = new HTTPParams(this);
    method = req.getMethod();

    final StringBuilder uri = new StringBuilder(req.getRequestURL());
    final String qs = req.getQueryString();
    if(qs != null) uri.append('?').append(qs);
    context.log.write(address(), context.user(), LogType.REQUEST, '[' + method + "] " + uri, null);

    // set UTF8 as default encoding (can be overwritten)
    res.setCharacterEncoding(Strings.UTF8);
    path = decode(normalize(req.getPathInfo()));

    final StaticOptions mprop = context().soptions;
    if(servlet.username.isEmpty()) {
      // adopt existing servlet-specific credentials
      username = mprop.get(StaticOptions.USER);
      password = mprop.get(StaticOptions.PASSWORD);
    } else {
      // otherwise, adopt global credentials
      username = servlet.username;
      password = servlet.password;
    }

    // prefer safest authorization method
    final String value = req.getHeader(AUTHORIZATION);
    final String am = value == null ? AuthMethod.BASIC.toString() : Strings.split(value, ' ', 2)[0];
    auth = StaticOptions.AUTHMETHOD.get(am) == AuthMethod.DIGEST ? AuthMethod.DIGEST :
      servlet.auth != null ? servlet.auth : mprop.get(StaticOptions.AUTHMETHOD);
  }

  /**
   * Authorizes a request.
   * @throws BaseXException database exception
   */
  void authorize() throws BaseXException {
    final String value = req.getHeader(AUTHORIZATION);
    if(value == null) return;

    final String[] ams = Strings.split(value, ' ', 2);
    final AuthMethod am = StaticOptions.AUTHMETHOD.get(ams[0]);
    if(am == null) throw new BaseXException(WHICHAUTH, value);

    // overwrite credentials with client data (basic or digest)
    if(am == AuthMethod.BASIC) {
      final String details = ams.length > 1 ? ams[1] : "";
      final String[] cred = Strings.split(org.basex.util.Base64.decode(details), ':', 2);
      if(cred.length != 2) throw new BaseXException(NOUSERNAME);
      username = cred[0];
      password = cred[1];
    } else { // (will always be) digest
      final EnumMap<Request, String> map = HttpClient.digestHeaders(value);
      username = map.get(Request.USERNAME);
      password = map.get(Request.RESPONSE);
    }
  }

  /**
   * Returns the content type of a request (without an optional encoding).
   * @return content type
   */
  public String contentType() {
    final String ct = req.getContentType();
    return ct != null ? ct.replaceFirst(";.*", "") : null;
  }

  /**
   * Returns the content type extension of a request (without an optional encoding).
   * @return content type
   */
  String contentTypeExt() {
    final String ct = req.getContentType();
    return ct != null ? ct.replaceFirst("^.*?;\\s*", "") : null;
  }

  /**
   * Initializes the output. Sets the expected encoding and content type.
   */
  public void initResponse() {
    // set content type and encoding
    final SerializerOptions opts = sopts();
    final String enc = opts.get(SerializerOptions.ENCODING);
    res.setCharacterEncoding(enc);
    final String ct = mediaType(opts);
    res.setContentType(new TokenBuilder(ct).add(CHARSET).add(enc).toString());
  }

  /**
   * Returns the media type defined in the specified serialization parameters.
   * @param sopts serialization parameters
   * @return media type
   */
  public static String mediaType(final SerializerOptions sopts) {
    // set content type
    final String type = sopts.get(SerializerOptions.MEDIA_TYPE);
    if(!type.isEmpty()) return type;

    // determine content type dependent on output method
    final SerialMethod sm = sopts.get(SerializerOptions.METHOD);
    if(sm == SerialMethod.RAW) return APP_OCTET;
    if(sm == SerialMethod.ADAPTIVE || sm == SerialMethod.XML) return APP_XML;
    if(sm == SerialMethod.XHTML || sm == SerialMethod.HTML) return TEXT_HTML;
    if(sm == SerialMethod.JSON) {
      final JsonSerialOptions jprop = sopts.get(SerializerOptions.JSON);
      return jprop.get(JsonOptions.FORMAT) == JsonFormat.JSONML ? APP_JSONML : APP_JSON;
    }
    return TEXT_PLAIN;
  }

  /**
   * Returns the URL path.
   * @return path path
   */
  public String path() {
    return path;
  }

  /**
   * Returns the database path (i.e., all path entries except for the first).
   * @return database path
   */
  public String dbpath() {
    final int s = path.indexOf('/', 1);
    return s == -1 ? "" : path.substring(s + 1);
  }

  /**
   * Returns the addressed database (i.e., the first path entry).
   * @return database, or {@code null} if the root directory was specified.
   */
  public String db() {
    final int s = path.indexOf('/', 1);
    return path.substring(1, s == -1 ? path.length() : s);
  }

  /**
   * Returns all accepted media types.
   * @return accepted media types
   */
  public HTTPAccept[] accepts() {
    final String accept = req.getHeader(ACCEPT);
    if(accept == null) return new HTTPAccept[0];

    final ArrayList<HTTPAccept> list = new ArrayList<>();
    for(final String produce : accept.split("\\s*,\\s*")) {
      final HTTPAccept acc = new HTTPAccept();
      // check if quality factor was specified
      final Matcher m = QF.matcher(produce);
      if(m.find()) {
        acc.type = m.group(1);
        acc.qf = toDouble(token(m.group(2)));
      } else {
        acc.type = produce;
      }
      // only accept valid double values
      if(acc.qf > 0 && acc.qf <= 1) list.add(acc);
    }
    return list.toArray(new HTTPAccept[list.size()]);
  }

  /**
   * Sets a status and sends an info message.
   * @param code status code
   * @param info info message (can be {@code null})
   * @param error treat as error (use web server standard output)
   * @throws IOException I/O exception
   */
  public void status(final int code, final String info, final boolean error) throws IOException {
    try {
      log(code, info);
      res.resetBuffer();
      if(code == SC_UNAUTHORIZED) {
        final StringBuilder header = new StringBuilder(auth.toString());
        if(auth == AuthMethod.DIGEST) {
          header.append(" " + Request.REALM + "=\"").append(Prop.NAME).append("\",");
          header.append(Request.QOP).append("=\"").append(AUTH).append(',');
          header.append(AUTH_INT).append("\",").append(Request.NONCE + "=\"");
          header.append(Strings.md5(Long.toString(System.nanoTime()))).append('"');
        }
        res.setHeader(WWW_AUTHENTICATE, header.toString());
      }

      if(error && code >= SC_BAD_REQUEST) {
        res.sendError(code, info);
      } else {
        res.setStatus(code);
        if(info != null) {
          res.setContentType(TEXT_PLAIN);
          final ArrayOutput ao = new ArrayOutput();
          ao.write(token(info));
          res.getOutputStream().write(ao.normalize().finish());
        }
      }
    } catch(final IllegalStateException ex) {
      log(SC_INTERNAL_SERVER_ERROR, Util.message(ex));
    }
  }

  /**
   * Updates the credentials.
   * @param user user
   * @param pass password
   */
  public void credentials(final String user, final String pass) {
    username = user;
    password = pass;
  }

  /**
   * Authenticates the user and returns a new client {@link Context} instance.
   * @return client context
   * @throws IOException I/O exception
   */
  public Context authenticate() throws IOException {
    final byte[] address = token(req.getRemoteAddr());
    try {
      if(username == null || username.isEmpty()) throw new LoginException(NOUSERNAME);

      final Context ctx = new Context(context(), null);
      final User us = ctx.users.get(username);
      if(us == null) throw new LoginException();
      ctx.user(us);

      if(auth == AuthMethod.BASIC) {
        if(password == null || !us.matches(password)) throw new LoginException();
      } else {
        // digest authentication
        final EnumMap<Request, String> map = HttpClient.digestHeaders(req.getHeader(AUTHORIZATION));
        final String am = map.get(Request.AUTH_METHOD);
        if(!AuthMethod.DIGEST.toString().equals(am)) throw new LoginException(DIGESTAUTH);

        String ha1 = us.code(Algorithm.DIGEST, Code.HASH);
        if(Strings.eq(map.get(Request.ALGORITHM), MD5_SESS))
          ha1 = Strings.md5(ha1 + ':' + map.get(Request.NONCE) + ':' + map.get(Request.CNONCE));

        String h2 = method + ':' + map.get(Request.URI);
        final String qop = map.get(Request.QOP);
        if(Strings.eq(qop, AUTH_INT)) h2 += ':' + Strings.md5(params.body().toString());
        final String ha2 = Strings.md5(h2);

        final StringBuilder rsp = new StringBuilder(ha1).append(':').append(map.get(Request.NONCE));
        if(Strings.eq(qop, AUTH, AUTH_INT)) {
          rsp.append(':').append(map.get(Request.NC));
          rsp.append(':').append(map.get(Request.CNONCE));
          rsp.append(':').append(qop);
        }
        rsp.append(':').append(ha2);

        if(!Strings.md5(rsp.toString()).equals(password)) throw new LoginException();
      }

      context.blocker.remove(address);
      return ctx;
    } catch(final LoginException ex) {
      // delay users with wrong passwords
      context.blocker.delay(address);
      throw ex;
    }
  }

  /**
   * Returns the database context.
   * @return context
   */
  public static Context context() {
    return context;
  }

  /**
   * Returns an exception that may have been caught by the initialization of the database server.
   * @return exception
   */
  public static IOException exception() {
    return exception;
  }

  /**
   * Assigns serialization parameters.
   * @param opts serialization parameters.
   */
  public void sopts(final SerializerOptions opts) {
    sopts = opts;
  }

  /**
   * Returns the serialization parameters.
   * @return serialization parameters.
   */
  public SerializerOptions sopts() {
    if(sopts == null) sopts = new SerializerOptions();
    return sopts;
  }

  /**
   * Writes a log message.
   * @param type log type
   * @param info info string (can be {@code null})
   */
  void log(final int type, final String info) {
    context.log.write(address(), context.user(), type, info, perf);
  }

  // STATIC METHODS =====================================================================

  /**
   * Initializes the HTTP context.
   * @return context;
   */
  public static synchronized Context init() {
    if(context == null) context = new Context();
    return context;
  }

  /**
   * Initializes the database context, based on the initial servlet context.
   * Parses all context parameters and passes them on to the database context.
   * @param sc servlet context
   * @throws IOException I/O exception
   */
  public static synchronized void init(final ServletContext sc) throws IOException {
    // check if HTTP context has already been initialized
    if(init) return;
    init = true;

    // set web application path as home directory and HTTPPATH
    final String webapp = sc.getRealPath("/");
    Options.setSystem(Prop.PATH, webapp);
    Options.setSystem(StaticOptions.WEBPATH, webapp);

    // bind all parameters that start with "org.basex." to system properties
    final Enumeration<String> en = sc.getInitParameterNames();
    while(en.hasMoreElements()) {
      final String key = en.nextElement();
      if(!key.startsWith(Prop.DBPREFIX)) continue;

      String val = sc.getInitParameter(key);
      if(key.endsWith("path") && !new File(val).isAbsolute()) {
        // prefix relative path with absolute servlet path
        Util.debug(key.toUpperCase(Locale.ENGLISH) + ": " + val);
        val = new IOFile(webapp, val).path();
      }
      Options.setSystem(key, val);
    }

    // create context, update options
    if(context == null) {
      context = new Context(false);
    } else {
      context.soptions.setSystem();
      context.options.setSystem();
    }

    // start server instance
    if(!context.soptions.get(StaticOptions.HTTPLOCAL)) {
      try {
        new BaseXServer(context);
      } catch(final IOException ex) {
        exception = ex;
        throw ex;
      }
    }
  }

  /**
   * Decodes the specified path segments.
   * @param segments strings to be decoded
   * @return argument
   * @throws IllegalArgumentException invalid path segments
   */
  public static String decode(final String segments) {
    try {
      return URLDecoder.decode(segments, Prop.ENCODING);
    } catch(final UnsupportedEncodingException ex) {
      throw new IllegalArgumentException(ex);
    }
  }

  // PRIVATE METHODS ====================================================================

  /**
   * Normalizes the path information.
   * @param path path, or {@code null}
   * @return normalized path
   */
  private static String normalize(final String path) {
    final TokenBuilder tmp = new TokenBuilder();
    if(path != null) {
      final TokenBuilder tb = new TokenBuilder();
      final int pl = path.length();
      for(int p = 0; p < pl; p++) {
        final char ch = path.charAt(p);
        if(ch == '/') {
          if(tb.isEmpty()) continue;
          tmp.add('/').add(tb.toArray());
          tb.reset();
        } else {
          tb.add(ch);
        }
      }
      if(!tb.isEmpty()) tmp.add('/').add(tb.finish());
    }
    if(tmp.isEmpty()) tmp.add('/');
    return tmp.toString();
  }

  /**
   * Returns a string with the remote user address.
   * @return user address
   */
  private String address() {
    return req.getRemoteAddr() + ':' + req.getRemotePort();
  }
}
