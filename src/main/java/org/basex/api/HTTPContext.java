package org.basex.api;

import static javax.servlet.http.HttpServletResponse.*;
import static org.basex.api.HTTPText.*;
import static org.basex.data.DataText.*;
import static org.basex.io.MimeTypes.*;
import static org.basex.util.Token.*;

import java.io.*;

import javax.servlet.http.*;

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
  /** Database session. */
  public Session session;

  /** Serialization parameters. */
  public String serialization = "";
  /** Result wrapping. */
  public boolean wrapping;

  /** Segments. */
  private final String[] segments;
  /** Full path. */
  private final String path;

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
   * @param s offset
   * @return specified segment
   */
  public String segment(final int s) {
    return segments[s];
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
    if(code == SC_UNAUTHORIZED) res.setHeader(WWW_AUTHENTICATE, BASIC);
    if(message != null) out.write(token(message));
  }

  // STATIC METHODS =====================================================================

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
