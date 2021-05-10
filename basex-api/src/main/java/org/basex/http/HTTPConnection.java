package org.basex.http;

import static javax.servlet.http.HttpServletResponse.*;
import static org.basex.util.Token.*;
import static org.basex.util.http.HttpText.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.basex.core.*;
import org.basex.core.StaticOptions.*;
import org.basex.core.jobs.*;
import org.basex.core.users.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.server.*;
import org.basex.server.Log.*;
import org.basex.util.*;
import org.basex.util.Base64;
import org.basex.util.http.*;
import org.basex.util.list.*;

/**
 * Single HTTP connection.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class HTTPConnection implements ClientInfo {
  /** Forwarding headers. */
  private static final String[] FORWARDING_HEADERS = { "X-Forwarded-For", "Proxy-Client-IP",
      "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR" };

  /** HTTP servlet request. */
  public final HttpServletRequest request;
  /** HTTP servlet response. */
  public final HttpServletResponse response;

  /** Current database context. */
  public final Context context;
  /** Request parameters. */
  public final RequestContext requestCtx;

  /** Performance. */
  private final Performance perf = new Performance();
  /** Authentication method. */
  private final AuthMethod auth;
  /** Path, starting with a slash. */
  private final String path;

  /** Request method. */
  public String method;
  /** Serialization parameters. */
  private SerializerOptions serializer;

  /**
   * Constructor.
   * @param request request
   * @param response response
   * @param auth authentication method (can be {@code null})
   */
  HTTPConnection(final HttpServletRequest request, final HttpServletResponse response,
      final AuthMethod auth) {

    this.request = request;
    this.response = response;

    context = new Context(HTTPContext.get().context(), this);
    method = request.getMethod();
    requestCtx = new RequestContext(request);

    // set UTF8 as default encoding (can be overwritten)
    response.setCharacterEncoding(Strings.UTF8);
    path = normalize(request.getPathInfo());

    // authentication method (servlet-specific or global)
    this.auth = auth != null ? auth : context.soptions.get(StaticOptions.AUTHMETHOD);
  }

  /**
   * Authorizes a request. Initializes the user if it is called for the first time.
   * @param username name of default servlet user (can be {@code null})
   * @throws IOException I/O exception
   */
  public void authenticate(final String username) throws IOException {
    // choose admin user for OPTIONS requests, servlet-specific user, or global user (can be empty)
    String name = method.equals(HttpMethod.OPTIONS.name()) ? UserText.ADMIN : username;
    if(name == null) name = context.soptions.get(StaticOptions.USER);

    // look for existing user. if it does not exist, try to authenticate
    User user = context.users.get(name);
    if(user == null) user = login();

    // successful authentication: assign user
    context.user(user);

    // generate log entry
    final StringBuilder uri = new StringBuilder(request.getRequestURI());
    final String qs = request.getQueryString();
    if(qs != null) uri.append('?').append(qs);
    context.log.write(LogType.REQUEST, '[' + method + "] " + uri, null, context);
  }

  /**
   * Returns the content type of a request as media type.
   * @return content type
   */
  public MediaType mediaType() {
    return mediaType(request);
  }

  /**
   * Initializes the output and assigns the character encoding and content type.
   */
  public void initResponse() {
    final SerializerOptions sopts = sopts();
    final MediaType mt = mediaType(sopts);
    final HashMap<String, String> params = mt.parameters();
    params.putIfAbsent(CHARSET, sopts.get(SerializerOptions.ENCODING));
    response.setCharacterEncoding(params.get(CHARSET));
    response.setContentType(mt.toString());
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
    final int i = path.indexOf('/', 1);
    return i == -1 ? "" : path.substring(i + 1);
  }

  /**
   * Returns the addressed database (i.e., the first path entry).
   * @return database, or {@code null} if the root directory was specified
   */
  public String db() {
    final int i = path.indexOf('/', 1);
    return path.substring(1, i == -1 ? path.length() : i);
  }

  /**
   * Returns all accepted media types.
   * @return accepted media types
   */
  public ArrayList<MediaType> accepts() {
    final String accepts = request.getHeader(ACCEPT);
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
    return list;
  }

  /**
   * Handles an error with an info message.
   * @param code status code
   * @param info info, will additionally be logged
   * @throws IOException I/O exception
   */
  public void error(final int code, final String info) throws IOException {
    log(code, info);
    status(code, null, info);
  }

  /**
   * Assigns serialization parameters.
   * @param opts serialization parameters
   */
  public void sopts(final SerializerOptions opts) {
    serializer = opts;
  }

  /**
   * Returns the serialization parameters.
   * @return serialization parameters
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
  public void log(final int status, final String info) {
    context.log.write(status, info, perf, context);
  }

  /**
   * Normalizes a redirection location. Prefixes absolute locations with the request URI.
   * @param location location
   * @return normalized representation
   */
  public String resolve(final String location) {
    String loc = location;
    if(Strings.startsWith(location, '/')) {
      final String uri = request.getRequestURI(), info = request.getPathInfo();
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
    response.sendRedirect(resolve(location));
  }

  /**
   * Sends a forward.
   * @param location location
   * @throws IOException I/O exception
   * @throws ServletException servlet exception
   */
  public void forward(final String location) throws IOException, ServletException {
    request.getRequestDispatcher(resolve(location)).forward(request, response);
  }

  @Override
  public String clientAddress() {
    return getRemoteAddr() + ':' + request.getRemotePort();
  }

  @Override
  public String clientName() {
    // check for request id
    Object value = request.getAttribute(HTTPText.CLIENT_ID);

    // check for session id (DBA, global)
    if(value == null) {
      final HttpSession session = request.getSession(false);
      if(session != null) {
        final boolean dba = (path() + '/').contains('/' + HTTPText.DBA_CLIENT_ID + '/');
        value = session.getAttribute(dba ? HTTPText.DBA_CLIENT_ID : HTTPText.CLIENT_ID);
      }
    }
    return clientName(value, context);
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
      response.resetBuffer();
      response.setStatus(code);
      response.setContentType(MediaType.TEXT_PLAIN + "; " + CHARSET + '=' + Strings.UTF8);
      // client directive: do not cache result (HTTP 1.1, old clients)
      response.setHeader(CACHE_CONTROL, "no-cache, no-store, must-revalidate");
      response.setHeader(PRAGMA, "no-cache");
      response.setHeader(EXPIRES, "0");
      response.getOutputStream().write(token(info));
    } catch(final IllegalStateException e) {
      // too late (response has already been committed)
      logError(code, null, info, e);
    }
  }

  /**
   * Sets a status and sends an info message.
   * @param code status code
   * @param message status message (can be {@code null})
   * @param body message for response body (can be {@code null})
   * @throws IOException I/O exception
   */
  @SuppressWarnings("deprecation")
  public void status(final int code, final String message, final String body) throws IOException {
    try {
      response.resetBuffer();
      if(code == SC_UNAUTHORIZED && !response.containsHeader(WWW_AUTHENTICATE)) {
        final TokenBuilder header = new TokenBuilder();
        header.add(auth).add(' ').add(Request.REALM).add("=\"").add(Prop.NAME).add('"');
        if(auth == AuthMethod.DIGEST) {
          final String nonce = Strings.md5(Long.toString(System.nanoTime()));
          header.add(",").add(Request.QOP).add("=\"").add(AUTH).add(',').add(AUTH_INT);
          header.add('"').add(',').add(Request.NONCE).add("=\"").add(nonce).add('"');
        }
        response.setHeader(WWW_AUTHENTICATE, header.toString());
      }

      final int c = code < 0 || code > 999 ? 500 : code;
      if(message == null) {
        response.setStatus(c);
      } else {
        // do not allow Jetty to create a custom error html page
        // control characters and non-ASCII codes will be removed (GH-1632)
        response.setStatus(c, message.replaceAll("[^\\x20-\\x7F]", "?"));
      }

      if(body != null) {
        response.setContentType(MediaType.TEXT_PLAIN + "; " + CHARSET + '=' + Strings.UTF8);
        response.getOutputStream().write(new TokenBuilder(token(body)).normalize().finish());
      }
    } catch(final IllegalStateException | IllegalArgumentException ex) {
      logError(code, message, body, ex);
    }
  }

  /**
   * Sets profiling information.
   * @param qi query info
   */
  public void timing(final QueryInfo qi) {
    final StringList list = new StringList(4);
    final BiConsumer<String, Long> add = (name, nano) ->
      list.add(name + ";dur=" + Performance.getTime(nano, 1));
    add.accept("parse", qi.parsing);
    add.accept("compile", qi.compiling);
    add.accept("evaluate", qi.evaluating);
    add.accept("serialize", qi.serializing);
    response.setHeader(SERVER_TIMING, String.join(",", list.finish()));
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

  /**
   * Returns the content type of a request as media type.
   * @param request servlet request
   * @return content type
   */
  public static MediaType mediaType(final HttpServletRequest request) {
    final String ct = request.getContentType();
    return ct == null ? MediaType.ALL_ALL : new MediaType(ct);
  }

  /**
   * Returns the content type of a request, or an empty string.
   * @param request servlet request
   * @return content type
   */
  public static String remoteAddress(final HttpServletRequest request) {
    for(final String header : FORWARDING_HEADERS) {
      final String addr = request.getHeader(header);
      if (addr != null && !addr.isEmpty() && !"unknown".equalsIgnoreCase(addr)) return addr;
    }
    return request.getRemoteAddr();
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Normalizes the specified path.
   * @param path path (can be {@code null})
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
    try {
      final User user;
      if(auth == AuthMethod.CUSTOM) {
        // custom authentication
        user = user(UserText.ADMIN);
      } else {
        // request authorization header, check authentication method
        final String header = request.getHeader(AUTHORIZATION);
        final String[] am = header != null ? Strings.split(header, ' ', 2) : new String[] { "" };
        final AuthMethod meth = StaticOptions.AUTHMETHOD.get(am[0]);
        if(auth != meth) throw new LoginException(HTTPText.WRONGAUTH_X, auth);

        if(auth == AuthMethod.BASIC) {
          final String details = am.length > 1 ? am[1] : "";
          final String[] creds = Strings.split(Base64.decode(details), ':', 2);
          user = user(creds[0]);
          if(creds.length < 2 || !user.matches(creds[1])) throw new LoginException(user.name());

        } else {
          final EnumMap<Request, String> map = HttpClient.authHeaders(header);
          user = user(map.get(Request.USERNAME));

          final String nonce = map.get(Request.NONCE), cnonce = map.get(Request.CNONCE);
          String ha1 = user.code(Algorithm.DIGEST, Code.HASH);
          if(Strings.eq(map.get(Request.ALGORITHM), MD5_SESS))
            ha1 = Strings.md5(ha1 + ':' + nonce + ':' + cnonce);

          String h2 = method + ':' + map.get(Request.URI);
          final String qop = map.get(Request.QOP);
          if(Strings.eq(qop, AUTH_INT)) h2 += ':' + Strings.md5(requestCtx.payload().toString());
          final String ha2 = Strings.md5(h2);

          final StringBuilder sb = new StringBuilder(ha1).append(':').append(nonce);
          if(Strings.eq(qop, AUTH, AUTH_INT)) {
            sb.append(':').append(map.get(Request.NC));
            sb.append(':').append(cnonce).append(':').append(qop);
          }
          sb.append(':').append(ha2);

          if(!Strings.md5(sb.toString()).equals(map.get(Request.RESPONSE)))
            throw new LoginException(user.name());
        }
      }

      // accept and return user
      context.blocker.remove(token(getRemoteAddr()));
      return user;

    } catch(final LoginException ex) {
      // delay users with wrong passwords
      context.blocker.delay(token(getRemoteAddr()));
      throw ex;
    }
  }

  /**
   * Returns a user for the specified string, or an error.
   * @param name user name (can be {@code null})
   * @return user reference
   * @throws LoginException login exception
   */
  private User user(final String name) throws LoginException {
    final User user = context.users.get(name);
    if(user == null) throw new LoginException(name);
    return user;
  }

  /**
   * Returns the remote address. Resolves proxy forwardings.
   * @return client address
   */
  private String getRemoteAddr() {
    return remoteAddress(request);
  }

  /**
   * Sets a status and sends an info message.
   * @param code status code
   * @param message status message (can be {@code null})
   * @param info detailed information (can be {@code null})
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
