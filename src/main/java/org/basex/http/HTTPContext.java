package org.basex.http;

import static javax.servlet.http.HttpServletResponse.*;
import static org.basex.data.DataText.*;
import static org.basex.http.HTTPText.*;
import static org.basex.io.MimeTypes.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import javax.servlet.*;
import javax.servlet.http.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.serial.*;
import org.basex.server.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class bundles context-based information on a single HTTP operation.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class HTTPContext {
  /** Singleton database context. */
  private static Context context;
  /** Singleton HTTP properties. */
  private static HTTPProp hprop;
  /** Initialization flag. */
  private static boolean init;

  /** Servlet request. */
  public final HttpServletRequest req;
  /** Servlet response. */
  public final HttpServletResponse res;
  /** Request method. */
  public final HTTPMethod method;
  /** Performance. */
  private final Performance perf = new Performance();

  /** Serialization parameters. */
  public String serialization = "";
  /** Result wrapping. */
  public boolean wrapping;

  /** Segments. */
  private final String[] segments;
  /** Full path. */
  private final String path;
  /** Current user session. */
  private LocalSession session;
  /** User name. */
  private String user;
  /** Password. */
  private String pass;

  /**
   * Constructor.
   * @param rq request
   * @param rs response
   * @throws IOException I/O exception
   */
  public HTTPContext(final HttpServletRequest rq, final HttpServletResponse rs)
      throws IOException {

    req = rq;
    res = rs;
    final String mth = rq.getMethod();
    method = HTTPMethod.get(mth);

    final StringBuilder uri = new StringBuilder(req.getRequestURL());
    final String qs = req.getQueryString();
    if(qs != null) uri.append('?').append(qs);
    log("[" + mth + "] " + uri, null);

    // set UTF8 as default encoding (can be overwritten)
    res.setCharacterEncoding(UTF8);

    segments = toSegments(req.getPathInfo());
    path = join(0);

    final HTTPProp hp = hprop(context);
    user = hp.get(HTTPProp.USER);
    pass = hp.get(HTTPProp.PASSWORD);

    // set session-specific credentials
    final String auth = req.getHeader(AUTHORIZATION);
    if(auth != null) {
      final String[] values = auth.split(" ");
      if(values[0].equals(BASIC)) {
        final String[] cred = Base64.decode(values[1]).split(":", 2);
        if(cred.length != 2) throw new LoginException(NOPASSWD);
        user = cred[0];
        pass = cred[1];
      } else {
        throw new LoginException(WHICHAUTH, values[0]);
      }
    }
  }

  /**
   * Returns all query parameters.
   * @return parameters
   */
  public Map<String, String[]> params() {
    final Map<String, String[]> params = new HashMap<String, String[]>();
    final Map<?, ?> map = req.getParameterMap();
    for(final Entry<?, ?> s : map.entrySet()) {
      final String key = s.getKey().toString();
      final String[] vals = s.getValue() instanceof String[] ?
          (String[]) s.getValue() : new String[] { s.getValue().toString() };
      params.put(key, vals);
    }
    return params;
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
   * Initializes the output. Sets the expected encoding and content type.
   * @param sprop serialization properties
   */
  public void initResponse(final SerializerProp sprop) {
    // set encoding
    final String encoding = sprop.get(SerializerProp.S_ENCODING);
    res.setCharacterEncoding(encoding);

    // set content type
    String type = sprop.get(SerializerProp.S_MEDIA_TYPE);
    if(type.isEmpty()) {
      // determine content type dependent on output method
      final String mt = sprop.get(SerializerProp.S_METHOD);
      if(mt.equals(M_RAW)) {
        type = APP_OCTET;
      } else if(mt.equals(M_XML)) {
        type = APP_XML;
      } else if(eq(mt, M_JSON, M_JSONML)) {
        type = APP_JSON;
      } else if(eq(mt, M_XHTML, M_HTML5, M_HTML)) {
        type = TEXT_HTML;
      } else {
        type = TEXT_PLAIN;
      }
    }
    res.setContentType(type + MimeTypes.CHARSET + encoding);
  }

  /**
   * Returns the path depth.
   * @return path depth
   */
  public int depth() {
    return segments.length;
  }

  /**
   * Returns the complete path.
   * @return path depth
   */
  public String path() {
    return path;
  }

  /**
   * Returns a single path segment.
   * @param i index
   * @return segment
   */
  public String segment(final int i) {
    return segments[i];
  }

  /**
   * Returns the database path (i.e., all path entries except for the first).
   * @return path depth
   */
  public String dbpath() {
    return join(1);
  }

  /**
   * Returns the addressed database (i.e., the first path entry), or {@code null}
   * if the root directory was specified.
   * @return database
   */
  public String db() {
    return depth() == 0 ? null : segments[0];
  }

  /**
   * Returns an array with all accepted content types.
   * if the root directory was specified.
   * @return database
   */
  public String[] produces() {
    final String[] acc = req.getHeader("Accept").split("\\s*,\\s*");
    for(int a = 0; a < acc.length; a++) {
      if(acc[a].indexOf(';') != -1) acc[a] = acc[a].replaceAll("\\w*;.*", "");
    }
    return acc;
  }

  /**
   * Sets a status and sends an info message.
   * @param code status code
   * @param message info message
   * @throws IOException I/O exception
   */
  public void status(final int code, final String message) throws IOException {
    log(message, code);
    res.resetBuffer();
    res.setStatus(code);
    if(code == SC_UNAUTHORIZED) res.setHeader(WWW_AUTHENTICATE, BASIC);
    if(message != null) res.getOutputStream().write(token(message));
  }

  /**
   * Updates the credentials.
   * @param u user
   * @param p password
   */
  public void credentials(final String u, final String p) {
    user = u;
    pass = p;
  }

  /**
   * Creates a new {@link LocalSession} instance.
   * @return database session
   * @throws IOException I/O exception
   */
  public LocalSession session() throws IOException {
    if(session == null) {
      final byte[] address = token(req.getRemoteAddr());
      try {
        if(user == null || user.isEmpty() || pass == null || pass.isEmpty())
          throw new LoginException(NOPASSWD);
        session = new LocalSession(context(), user, pass);
        context.blocker.remove(address);
      } catch(final LoginException ex) {
        // delay users with wrong passwords
        for(int d = context.blocker.delay(address); d > 0; d--) Performance.sleep(1000);
        throw ex;
      }
    }
    return session;
  }

  /**
   * Closes an open database session.
   */
  public void close() {
    if(session != null) session.close();
  }

  /**
   * Returns the database context.
   * @return context;
   */
  public Context context() {
    return context;
  }

  /**
   * Returns the database context.
   * @return context;
   */
  public HTTPProp hprop() {
    return hprop;
  }

  /**
   * Writes a log message.
   * @param info message info
   * @param type message type (true/false/null: OK, ERROR, REQUEST, Error Code)
   */
  public void log(final String info, final Object type) {
    // add evaluation time if any type is specified
    context.log.write(type != null ?
      new Object[] { address(), context.user.name, type, info, perf } :
      new Object[] { address(), context.user.name, null, info });
  }

  // STATIC METHODS =====================================================================

  /**
   * Returns the HTTP properties.
   * @param ctx database context
   * @return context;
   */
  public static synchronized HTTPProp hprop(final Context ctx) {
    if(hprop == null) {
      hprop = new HTTPProp();
      // if not modified yet, set restxq to webapp path
      if(hprop.get(HTTPProp.RESTXQPATH).isEmpty())
         hprop.set(HTTPProp.RESTXQPATH, ctx.mprop.get(MainProp.WEBPATH));
    }
    return hprop;
  }

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
    Prop.setSystem(Prop.PATH, webapp);
    Prop.setSystem(MainProp.WEBPATH, webapp);

    // bind all parameters that start with "org.basex." to system properties
    final Enumeration<String> en = sc.getInitParameterNames();
    while(en.hasMoreElements()) {
      String key = en.nextElement();
      if(!key.startsWith(Prop.DBPREFIX)) continue;

      // legacy: rewrite obsolete properties
      String val = sc.getInitParameter(key);
      String k = key;
      String v = val;
      if(key.equals(Prop.DBPREFIX + "httppath")) {
        k = Prop.DBPREFIX + HTTPProp.RESTXQPATH[0];
      } else if(key.equals(Prop.DBPREFIX + "mode")) {
        k = Prop.DBPREFIX + HTTPProp.SERVER[0];
        v = Boolean.toString(!v.equals("local"));
      }
      k = k.toLowerCase(Locale.ENGLISH);
      if(!k.equals(key) || !v.equals(val)) {
        Util.errln("Warning! Outdated property: " +
          key + "=" + val + " => " + k + "=" + v);
      }
      // prefix relative paths with absolute servlet path
      if(k.endsWith("path") && !new File(v).isAbsolute()) {
        Util.debug(k.toUpperCase(Locale.ENGLISH) + ": " + v);
        v = new IOFile(webapp, v).path();
      }
      Prop.setSystem(k, v);
    }

    // create context, update property instances
    if(context == null) context = new Context(false);
    hprop(context).setSystem();
    context.mprop.setSystem();
    context.prop.setSystem();

    // start server instance
    if(hprop(context).is(HTTPProp.SERVER)) new BaseXServer(context);
  }

  /**
   * Converts the path to a string array, containing the single segments.
   * @param path path, or {@code null}
   * @return path depth
   */
  public static String[] toSegments(final String path) {
    final StringList sl = new StringList();
    if(path != null) {
      final TokenBuilder tb = new TokenBuilder();
      for(int s = 0; s < path.length(); s++) {
        final char ch = path.charAt(s);
        if(ch == '/') {
          if(tb.isEmpty()) continue;
          sl.add(tb.toString());
          tb.reset();
        } else {
          tb.add(ch);
        }
      }
      if(!tb.isEmpty()) sl.add(tb.toString());
    }
    return sl.toArray();
  }

  // PRIVATE METHODS ====================================================================

  /**
   * Joins the path.
   * @param s segment to start with
   * @return joined path
   */
  private String join(final int s) {
    final TokenBuilder tb = new TokenBuilder();
    for(int p = s; p < segments.length; p++) {
      if(!tb.isEmpty()) tb.add('/');
      tb.add(segments[p]);
    }
    return tb.toString();
  }

  /**
   * Returns a string with the remote user address.
   * @return user address
   */
  private String address() {
    return new StringBuilder(req.getRemoteAddr()).append(':').
        append(req.getRemotePort()).toString();
  }
}
