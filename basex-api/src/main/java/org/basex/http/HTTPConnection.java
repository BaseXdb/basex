package org.basex.http;

import static javax.servlet.http.HttpServletResponse.*;
import static org.basex.http.HTTPText.*;
import static org.basex.util.Token.*;
import static org.basex.util.http.HttpText.*;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.basex.core.*;
import org.basex.core.StaticOptions.*;
import org.basex.core.jobs.*;
import org.basex.core.users.*;
import org.basex.io.serial.*;
import org.basex.server.*;
import org.basex.server.Log.*;
import org.basex.util.*;
import org.basex.util.http.*;

/**
 * Single HTTP connection.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class HTTPConnection implements ClientInfo {
  /** Servlet request. */
  public final HttpServletRequest req;
  /** Servlet response. */
  public final HttpServletResponse res;
  /** Servlet instance. */
  public final BaseXServlet servlet;

  /** Current database context. */
  public final Context context;
  /** Request method. */
  public final String method;
  /** Request parameters. */
  public final HTTPParams params;

  /** Performance. */
  private final Performance perf = new Performance();
  /** Authentication method. */
  private final AuthMethod auth;
  /** Path, starting with a slash. */
  private final String path;

  /** Serialization parameters. */
  private SerializerOptions serializer;
  /** User name. */
  private String username;

  /**
   * Constructor.
   * @param req request
   * @param res response
   * @param servlet calling servlet instance
   */
  HTTPConnection(final HttpServletRequest req, final HttpServletResponse res,
      final BaseXServlet servlet) {

    this.req = req;
    this.res = res;
    this.servlet = servlet;

    context = new Context(HTTPContext.context(), this);
    method = req.getMethod();
    params = new HTTPParams(this);

    // set UTF8 as default encoding (can be overwritten)
    res.setCharacterEncoding(Strings.UTF8);
    path = decode(normalize(req.getPathInfo()));

    // authentication method
    auth = servlet.auth != null ? servlet.auth : context.soptions.get(StaticOptions.AUTHMETHOD);
  }

  /**
   * Authorizes a request. Initializes the user if it is called for the first time.
   * @throws IOException I/O exception
   */
  public void authenticate() throws IOException {
    // choose admin user for OPTIONS requests, servlet-specific user, or global user (can be empty)
    String name = method.equals(HttpMethod.OPTIONS.name()) ? UserText.ADMIN : servlet.user;
    if(name == null) name = context.soptions.get(StaticOptions.USER);

    // look for existing user. if it does not exist, try to authenticate
    User user = context.users.get(name);
    if(user == null) user = login();

    // successful authentication: assign user
    context.user(user);
    username = servlet.username(this);

    // generate log entry
    final StringBuilder uri = new StringBuilder(req.getRequestURL());
    final String qs = req.getQueryString();
    if(qs != null) uri.append('?').append(qs);
    context.log.write(LogType.REQUEST, '[' + method + "] " + uri, null, context);
  }

  /**
   * Returns the content type of a request, or an empty string.
   * @return content type
   */
  public MediaType contentType() {
    final String ct = req.getContentType();
    return new MediaType(ct == null ? "" : ct);
  }

  /**
   * Initializes the output. Sets the expected encoding and content type.
   */
  public void initResponse() {
    // set content type and encoding
    final SerializerOptions opts = sopts();
    final String enc = opts.get(SerializerOptions.ENCODING);
    res.setCharacterEncoding(enc);
    res.setContentType(new MediaType(mediaType(opts) + "; " + CHARSET + '=' + enc).toString());
  }

  /**
   * Returns the URL path. The path always starts with a slash.
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
  public MediaType[] accepts() {
    final String accepts = req.getHeader(ACCEPT);
    final ArrayList<MediaType> list = new ArrayList<>();
    if(accepts == null) {
      list.add(MediaType.ALL_ALL);
    } else {
      for(final String accept : accepts.split("\\s*,\\s*")) {
        // check if quality factor was specified
        final MediaType type = new MediaType(accept);
        final String qf = type.parameters().get("q");
        final double d = qf != null ? toDouble(token(qf)) : 1;
        // only accept media types with valid double values
        if(d > 0 && d <= 1) {
          final StringBuilder sb = new StringBuilder();
          final String main = type.main(), sub = type.sub();
          sb.append(main.isEmpty() ? "*" : main).append('/');
          sb.append(sub.isEmpty() ? "*" : sub).append("; q=").append(d);
          list.add(new MediaType(sb.toString()));
        }
      }
    }
    return list.toArray(new MediaType[list.size()]);
  }

  /**
   * Sends an error with an info message.
   * @param code status code
   * @param info info, sent as body
   * @throws IOException I/O exception
   */
  public void error(final int code, final String info) throws IOException {
    status(code, null, info);
  }

  /**
   * Sets the HTTP status code and message.
   * @param code status code
   * @param message status message
   * @throws IOException I/O exception
   */
  public void status(final int code, final String message) throws IOException {
    status(code, message, null);
  }

  /**
   * Assigns serialization parameters.
   * @param opts serialization parameters.
   */
  public void sopts(final SerializerOptions opts) {
    serializer = opts;
  }

  /**
   * Returns the serialization parameters.
   * @return serialization parameters.
   */
  public SerializerOptions sopts() {
    if(serializer == null) serializer = new SerializerOptions();
    return serializer;
  }

  /**
   * Writes a log message.
   * @param status HTTP status code
   * @param info info string (can be {@code null})
   */
  void log(final int status, final String info) {
    context.log.write(Integer.toString(status), info, perf, context);
  }

  /**
   * Normalizes a redirection location. Prefixes absolute locations with the request URI.
   * @param location location
   * @return normalized representation
   */
  public String resolve(final String location) {
    String loc = location;
    if(location.startsWith("/")) {
      final String uri = req.getRequestURI(), info = req.getPathInfo();
      loc = (info == null ? uri : uri.substring(0, uri.length() - info.length())) + location;
    }
    return loc;
  }

  /**
   * Sends a redirect.
   * @param location location
   * @throws IOException I/O exception
   */
  public void redirect(final String location) throws IOException {
    res.sendRedirect(resolve(location));
  }

  /**
   * Sends a forward.
   * @param location location
   * @throws IOException I/O exception
   * @throws ServletException servlet exception
   */
  public void forward(final String location) throws IOException, ServletException {
    req.getRequestDispatcher(resolve(location)).forward(req, res);
  }

  @Override
  public String clientAddress() {
    return req.getRemoteAddr() + ':' + req.getRemotePort();
  }

  @Override
  public String clientName() {
    return username;
  }

  /**
   * Decodes the specified path.
   * @param path strings to be decoded
   * @return argument
   */
  public static String decode(final String path) {
    try {
      return URLDecoder.decode(path, Prop.ENCODING);
    } catch(final UnsupportedEncodingException | IllegalArgumentException ex) {
      Util.debug(ex);
      return path;
    }
  }

  /**
   * Returns the media type defined in the specified serialization parameters.
   * @param sopts serialization parameters
   * @return media type
   */
  public static MediaType mediaType(final SerializerOptions sopts) {
    // set content type
    final String type = sopts.get(SerializerOptions.MEDIA_TYPE);
    if(!type.isEmpty()) return new MediaType(type);

    // determine content type dependent on output method
    final SerialMethod sm = sopts.get(SerializerOptions.METHOD);
    if(sm == SerialMethod.BASEX || sm == SerialMethod.ADAPTIVE || sm == SerialMethod.XML)
      return MediaType.APPLICATION_XML;
    if(sm == SerialMethod.XHTML || sm == SerialMethod.HTML) return MediaType.TEXT_HTML;
    if(sm == SerialMethod.JSON) return MediaType.APPLICATION_JSON;
    return MediaType.TEXT_PLAIN;
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Normalizes the specified path.
   * @param path path
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
   * Authenticates the user and returns a {@link User} instance or an exception.
   * @return user
   * @throws IOException I/O exception
   */
  private User login() throws IOException {
    final byte[] address = token(req.getRemoteAddr());
    try {
      final User user;
      if(auth == AuthMethod.CUSTOM) {
        // custom authentication
        user = user(UserText.ADMIN);
      } else {
        // request authorization header, check authentication method
        final String header = req.getHeader(AUTHORIZATION);
        final String[] am = header != null ? Strings.split(header, ' ', 2) : new String[] { "" };
        final AuthMethod meth = StaticOptions.AUTHMETHOD.get(am[0]);
        if(auth != meth) throw new LoginException(WRONGAUTH_X, auth);

        if(auth == AuthMethod.BASIC) {
          final String details = am.length > 1 ? am[1] : "";
          final String[] creds = Strings.split(org.basex.util.Base64.decode(details), ':', 2);
          user = user(creds[0]);
          if(creds.length < 2 || !user.matches(creds[1])) throw new LoginException();

        } else {
          final EnumMap<Request, String> map = HttpClient.digestHeaders(header);
          user = user(map.get(Request.USERNAME));

          final String nonce = map.get(Request.NONCE), cnonce = map.get(Request.CNONCE);
          String ha1 = user.code(Algorithm.DIGEST, Code.HASH);
          if(Strings.eq(map.get(Request.ALGORITHM), MD5_SESS))
            ha1 = Strings.md5(ha1 + ':' + nonce + ':' + cnonce);

          String h2 = method + ':' + map.get(Request.URI);
          final String qop = map.get(Request.QOP);
          if(Strings.eq(qop, AUTH_INT)) h2 += ':' + Strings.md5(params.body().toString());
          final String ha2 = Strings.md5(h2);

          final StringBuilder response = new StringBuilder(ha1).append(':').append(nonce);
          if(Strings.eq(qop, AUTH, AUTH_INT)) {
            response.append(':').append(map.get(Request.NC));
            response.append(':').append(cnonce).append(':').append(qop);
          }
          response.append(':').append(ha2);

          if(!Strings.md5(response.toString()).equals(map.get(Request.RESPONSE)))
            throw new LoginException();
        }
      }

      // accept and return user
      context.blocker.remove(address);
      return user;

    } catch(final LoginException ex) {
      // delay users with wrong passwords
      context.blocker.delay(address);
      throw ex;
    }
  }

  /**
   * Returns a user for the specified string, or an error.
   * @param user user name (can be {@code null})
   * @return user reference
   * @throws LoginException login exception
   */
  private User user(final String user) throws LoginException {
    final User u = context.users.get(user);
    if(u == null) throw new LoginException();
    return u;
  }

  /**
   * Sets 460 a proprietary status code and sends the exception message as info.
   * @param ex job exception
   * @throws IOException I/O exception
   */
  public void stop(final JobException ex) throws IOException {
    final int code = 460;
    final String info = ex.getMessage();
    log(code, info);
    try {
      res.resetBuffer();
      res.setStatus(code);
      res.setContentType(MediaType.TEXT_PLAIN.toString());
      // client directive: do not cache result (HTTP 1.1, old clients)
      res.setHeader(CACHE_CONTROL, "no-cache, no-store, must-revalidate");
      res.setHeader(PRAGMA, "no-cache");
      res.setHeader(EXPIRES, "0");
      res.getOutputStream().write(token(info));
    } catch(final IllegalStateException e) {
      // too late (response has already been committed)
      logError(code, null, info, e);
    }
  }

  /**
   * Sets a status and sends an info message.
   * @param code status code
   * @param message status message (can be {@code null})
   * @param info info, sent as body (can be {@code null})
   * @throws IOException I/O exception
   */
  @SuppressWarnings("deprecation")
  private void status(final int code, final String message, final String info) throws IOException {
    log(code, message != null ? message : info != null ? info : "");
    try {
      res.resetBuffer();
      if(code == SC_UNAUTHORIZED) {
        final TokenBuilder header = new TokenBuilder(auth.toString());
        header.add(' ').addExt(Request.REALM).add("=\"").add(Prop.NAME).add('"');
        if(auth == AuthMethod.DIGEST) {
          final String nonce = Strings.md5(Long.toString(System.nanoTime()));
          header.add(",").addExt(Request.QOP).add("=\"").add(AUTH).add(',').add(AUTH_INT).add('"');
          header.add(',').addExt(Request.NONCE).add("=\"").add(nonce).add('"');
        }
        res.setHeader(WWW_AUTHENTICATE, header.toString());
      }

      final int c = code < 0 || code > 999 ? 500 : code;
      if(message == null) {
        res.setStatus(c);
      } else {
        // do not allow Jetty to create a custom error html page
        res.setStatus(c, message);
      }
      if(info != null) {
        res.setContentType(MediaType.TEXT_PLAIN.toString());
        res.getOutputStream().write(new TokenBuilder(token(info)).normalize().finish());
      }
    } catch(final IllegalStateException | IllegalArgumentException ex) {
      logError(code, message, info, ex);
    }
  }

  /**
   * Sets a status and sends an info message.
   * @param code status code
   * @param message status message (can be {@code null})
   * @param info info, sent as body (can be {@code null})
   * @param ex exception
   */
  private void logError(final int code, final String message, final String info,
      final Exception ex) {

    final StringBuilder sb = new StringBuilder();
    sb.append("Code: ").append(code);
    if(info != null) sb.append(", Info: ").append(info);
    if(message != null) sb.append(", Message: ").append(message);
    sb.append(", Error: ").append(Util.message(ex));
    log(SC_INTERNAL_SERVER_ERROR, sb.toString());
  }
}
