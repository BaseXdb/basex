package org.basex.http;

import static javax.servlet.http.HttpServletResponse.*;
import static org.basex.http.HTTPText.*;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.basex.core.*;
import org.basex.core.StaticOptions.*;
import org.basex.core.jobs.*;
import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.server.*;
import org.basex.util.*;

/**
 * Base class for various servlets.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public abstract class BaseXServlet extends HttpServlet {
  /** Servlet-specific user. */
  String user;
  /** Servlet-specific authentication method. */
  AuthMethod auth;

  @Override
  public void init(final ServletConfig config) throws ServletException {
    super.init(config);
    try {
      HTTPContext.init(config.getServletContext());
    } catch(final IOException ex) {
      throw new ServletException(ex);
    }

    // set user and authentication method
    final Enumeration<String> en = config.getInitParameterNames();
    while(en.hasMoreElements()) {
      String key = en.nextElement().toLowerCase(Locale.ENGLISH);
      final String val = config.getInitParameter(key);
      if(key.startsWith(Prop.DBPREFIX)) key = key.substring(Prop.DBPREFIX.length());
      if(key.equalsIgnoreCase(StaticOptions.USER.name())) {
        user = val;
      } else if(key.equalsIgnoreCase(StaticOptions.AUTHMETHOD.name())) {
        auth = AuthMethod.valueOf(val);
      }
    }
  }

  @Override
  public final void service(final HttpServletRequest req, final HttpServletResponse res)
      throws IOException {

    final HTTPConnection conn = new HTTPConnection(req, res, this);
    try {
      conn.authenticate();
      run(conn);
      conn.log(SC_OK, "");
    } catch(final HTTPException ex) {
      conn.error(ex.getStatus(), Util.message(ex));
    } catch(final LoginException ex) {
      conn.error(SC_UNAUTHORIZED, Util.message(ex));
    } catch(final QueryException ex) {
      final Value v = ex.value();
      final int code = v instanceof Int ? (int) ((Int) v).itr() : SC_BAD_REQUEST;
      conn.error(code, ex.getMessage(), Util.message(ex));
    } catch(final IOException ex) {
      conn.error(SC_BAD_REQUEST, Util.message(ex));
    } catch(final JobException ex) {
      conn.stop(ex);
    } catch(final Exception ex) {
      final String msg = Util.bug(ex);
      Util.errln(msg);
      conn.error(SC_INTERNAL_SERVER_ERROR, Util.info(UNEXPECTED_X, msg));
    } finally {
      if(Prop.debug) {
        Util.outln("_ REQUEST _________________________________" + Prop.NL + req);
        final Enumeration<String> en = req.getHeaderNames();
        while(en.hasMoreElements()) {
          final String key = en.nextElement();
          Util.outln(Text.LI + key + Text.COLS + req.getHeader(key));
        }
        Util.out("_ RESPONSE ________________________________" + Prop.NL + res);
      }
    }
  }

  /**
   * Runs the code.
   * @param conn HTTP connection
   * @throws Exception any exception
   */
  protected abstract void run(HTTPConnection conn) throws Exception;

  /**
   * Returns a servlet-specific user name. By default, it returns the name of the database user.
   * @param http HTTP connection
   * @return user name or {@code null}
   */
  public String username(final HTTPConnection http) {
    final User u = http.context.user();
    return u != null ? u.name() : null;
  }
}
