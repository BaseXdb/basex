package org.basex.api.rest;

import static org.basex.util.Token.*;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.basex.server.Session;
import org.basex.util.TokenBuilder;
import org.basex.util.list.StringList;

/**
 * This class contains context-based information on a REST operation.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
final class RESTContext {
  /** Path. */
  private final String[] path;

  /** Servlet request. */
  protected HttpServletRequest req;
  /** Servlet response. */
  protected HttpServletResponse res;
  /** Database session. */
  protected Session session;
  /** Output stream. */
  protected OutputStream out;

  /** Serialization parameters. */
  protected String serialization = "";
  /** Result wrapping. */
  protected boolean wrapping;

  /**
   * Constructor.
   * @param rq request
   * @param rs response
   * @throws IOException I/O exception
   */
  RESTContext(final HttpServletRequest rq, final HttpServletResponse rs)
      throws IOException {

    req = rq;
    res = rs;
    out = res.getOutputStream();
    res.setCharacterEncoding(UTF8);

    // convert path to string array
    final StringList sl = new StringList();
    final String p = req.getPathInfo();
    if(p != null) {
      final TokenBuilder tb = new TokenBuilder();
      for(int s = 0; s < p.length(); s++) {
        final char ch = p.charAt(s);
        if(ch == '/') {
          if(tb.size() == 0) continue;
          sl.add(tb.toString());
          tb.reset();
        } else {
          tb.add(ch);
        }
      }
      if(tb.size() != 0) sl.add(tb.toString());
    }
    path = sl.toArray();
  }

  /**
   * Returns the path depth.
   * @return path depth
   */
  int depth() {
    return path.length;
  }

  /**
   * Returns the database path.
   * @return path depth
   */
  String dbpath() {
    return join(1);
  }

  /**
   * Returns the complete path.
   * @return path depth
   */
  String all() {
    return join(0);
  }

  /**
   * Returns the addressed database, or {@code null}.
   * @return database
   */
  String db() {
    return depth() == 0 ? null : path[0];
  }

  /**
   * Joins the path.
   * @param s step to start with
   * @return joined path
   */
  private String join(final int s) {
    final TokenBuilder tb = new TokenBuilder();
    for(int p = s; p < path.length; p++) {
      if(tb.size() != 0) tb.add('/');
      tb.add(path[p]);
    }
    return tb.toString();
  }

  /**
   * Sets a status and sends an info message.
   * @param code status code
   * @param message info message
   * @throws IOException I/O exception
   */
  void status(final int code, final String message) throws IOException {
    session.close();
    res.setStatus(code);
    if(message != null) out.write(token(message));
  }
}
