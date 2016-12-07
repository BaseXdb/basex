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
import org.basex.query.*;
import org.basex.server.*;
import org.basex.util.*;

/**
 * <p>Base class for all servlets.</p>
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public abstract class BaseXServlet extends HttpServlet {
  /** Servlet-specific user. */
  String username = "";
  /** Servlet-specific password. */
  String password = "";
  /** Servlet-specific authentication method. */
  AuthMethod auth;

  @Override
  public void init(final ServletConfig config) throws ServletException {
    super.init(config);
    try {
      HTTPContext.init(config.getServletContext());
      final Enumeration<String> en = config.getInitParameterNames();
      while(en.hasMoreElements()) {
        String key = en.nextElement().toLowerCase(Locale.ENGLISH);
        final String val = config.getInitParameter(key);
        if(key.startsWith(Prop.DBPREFIX)) key = key.substring(Prop.DBPREFIX.length());
        if(key.equalsIgnoreCase(StaticOptions.USER.name())) {
          username = val;
        } else if(key.equalsIgnoreCase(StaticOptions.PASSWORD.name())) {
          password = val;
        } else if(key.equalsIgnoreCase(StaticOptions.AUTHMETHOD.name())) {
          auth = AuthMethod.valueOf(val);
        }
      }
    } catch(final IOException ex) {
      throw new ServletException(ex);
    }
  }

  @Override
  public final void service(final HttpServletRequest req, final HttpServletResponse res)
      throws IOException {

    final HTTPContext http = new HTTPContext(req, res, this);
    try {
      http.authorize();
      run(http);
      http.log(SC_OK, "");
    } catch(final HTTPException ex) {
      http.error(ex.getStatus(), Util.message(ex));
    } catch(final LoginException ex) {
      http.error(SC_UNAUTHORIZED, Util.message(ex));
    } catch(final IOException | QueryException ex) {
      http.error(SC_BAD_REQUEST, Util.message(ex));
    } catch(final JobException ex) {
      http.error(SC_GONE, Text.INTERRUPTED);
    } catch(final Exception ex) {
      final String msg = Util.bug(ex);
      Util.errln(msg);
      http.error(SC_INTERNAL_SERVER_ERROR, Util.info(UNEXPECTED, msg));
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
   * @param http HTTP context
   * @throws Exception any exception
   */
  protected abstract void run(HTTPContext http) throws Exception;
}
