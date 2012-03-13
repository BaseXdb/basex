package org.basex.http;

import static javax.servlet.http.HttpServletResponse.*;
import static org.basex.data.DataText.*;
import static org.basex.http.HTTPText.*;
import static org.basex.io.MimeTypes.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import javax.servlet.http.*;

import org.basex.core.*;
import org.basex.data.*;
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
  /** Global database context. */
  private static Context context;

  /** Servlet request. */
  public final HttpServletRequest req;
  /** Servlet response. */
  public final HttpServletResponse res;
  /** Request method. */
  public final HTTPMethod method;
  /** Output stream. */
  public final OutputStream out;
  /** Input stream. */
  public final InputStream in;

  /** Serialization parameters. */
  public String serialization = "";
  /** Result wrapping. */
  public boolean wrapping;

  /** Segments. */
  private final String[] segments;
  /** Full path. */
  private final String path;
  /** Current user session. */
  private Session session;
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
    method = HTTPMethod.get(rq.getMethod());

    in = req.getInputStream();
    out = res.getOutputStream();

    // set UTF8 as default encoding (can be overwritten)
    res.setCharacterEncoding(UTF8);
    segments = toSegments(req.getPathInfo());
    path = join(0);

    user = System.getProperty(DBUSER);
    pass = System.getProperty(DBPASS);

    // set session-specific credentials
    final String auth = req.getHeader(DataText.AUTHORIZATION);
    if(auth != null) {
      final String[] values = auth.split(" ");
      if(values[0].equals(DataText.BASIC)) {
        final String[] cred = Base64.decode(values[1]).split(":", 2);
        if(cred.length != 2) throw new LoginException(NOPASSWD);
        user = cred[0];
        pass = cred[1];
      } else {
        throw new LoginException(WHICHAUTH, values[0]);
      }
    }

    // initialize database context
    init();
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
   * Initializes the output. Sets the expected encoding and content type.
   * @param sprop serialization properties
   */
  public void initResponse(final SerializerProp sprop) {
    // set encoding
    res.setCharacterEncoding(sprop.get(SerializerProp.S_ENCODING));

    // set content type
    String type = sprop.get(SerializerProp.S_MEDIA_TYPE);
    if(type.isEmpty()) {
      // determine content type dependent on output method
      final String mt = sprop.get(SerializerProp.S_METHOD);
      if(mt.equals(M_RAW)) {
        type = APP_OCTET;
      } else if(mt.equals(M_XML)) {
        type = APP_XML;
      } else if(Token.eq(mt, M_JSON, M_JSONML)) {
        type = APP_JSON;
      } else if(Token.eq(mt, M_XHTML, M_HTML)) {
        type = TEXT_HTML;
      } else {
        type = TEXT_PLAIN;
      }
    }
    res.setContentType(type);
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
    if(session != null) session.close();
    res.setStatus(code);
    if(code == SC_UNAUTHORIZED) res.setHeader(DataText.WWW_AUTHENTICATE, DataText.BASIC);
    if(message != null) out.write(token(message));
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
   * Creates a new session instance. By default, a {@link LocalSession}
   * instance will be generated. A {@link ClientSession} will be created
   * if "client" has been chosen an operation mode.
   * @return database session
   * @throws IOException I/O exception
   */
  public Session session() throws IOException {
    if(user == null || user.isEmpty() || pass == null || pass.isEmpty())
      throw new LoginException(NOPASSWD);

    if(session == null) session = CLIENT.equals(System.getProperty(DBMODE)) ?
        new ClientSession(context(), user, pass) :
        new LocalSession(context(), user, pass);
    return session;
  }

  /**
   * Closes an open database session.
   * @throws IOException I/O exception
   */
  public void close() throws IOException {
    if(session != null) session.close();
  }

  /**
   * Returns the database context.
   * @return context;
   */
  public Context context() {
    return context;
  }

  // STATIC METHODS =====================================================================

  /**
   * Returns the static database context instance.
   * Initializes the context if not done yet.
   * @return context;
   */
  public static Context init() {
    synchronized(Context.class) {
      if(context == null) context = new Context();
    }
    return context;
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
}
