package org.basex.http;

import static jakarta.servlet.http.HttpServletResponse.*;

import java.io.*;
import java.util.*;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import org.basex.core.*;
import org.basex.core.StaticOptions.*;
import org.basex.core.jobs.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.server.*;
import org.basex.util.*;
import org.basex.util.http.*;

/**
 * Base class for various servlets.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public abstract class BaseXServlet extends HttpServlet {
  /** Servlet-specific user. */
  private String username;
  /** Servlet-specific authentication method. */
  private AuthMethod auth;

  @Override
  public void init(final ServletConfig config) throws ServletException {
    super.init(config);

    final HTTPContext hc = HTTPContext.get();
    try {
      hc.init(config.getServletContext());
    } catch(final IOException ex) {
      throw new ServletException(ex);
    }

    // parse servlet-specific user and authentication method
    final Enumeration<String> en = config.getInitParameterNames();
    while(en.hasMoreElements()) {
      String name = en.nextElement();
      final String value = config.getInitParameter(name);
      if(name.startsWith(Prop.DBPREFIX)) {
        name = name.substring(Prop.DBPREFIX.length());
        if(name.equalsIgnoreCase(StaticOptions.USER.name())) {
          username = value;
        } else if(name.equalsIgnoreCase(StaticOptions.AUTHMETHOD.name())) {
          auth = AuthMethod.valueOf(value);
        }
      }
    }
    final Context ctx = hc.context();
    if(ctx.soptions.get(StaticOptions.LOGTRACE)) ctx.setExternal(ctx.log);
  }

  @Override
  public final void service(final HttpServletRequest request, final HttpServletResponse response)
      throws IOException {

    final HTTPConnection conn = new HTTPConnection(request, response, auth);
    try {
      conn.authenticate(username);
      run(conn);
    } catch(final HTTPException ex) {
      conn.error(ex.getStatus(), Util.message(ex));
    } catch(final LoginException ex) {
      conn.error(SC_UNAUTHORIZED, Util.message(ex));
    } catch(final QueryException ex) {
      int code = SC_INTERNAL_SERVER_ERROR;
      boolean full = conn.context.soptions.get(StaticOptions.RESTXQERRORS);
      final QNm qname = ex.qname();
      if(Token.eq(qname.uri(), QueryText.REST_URI)) {
        final Value value = ex.value();
        if(value instanceof ANum) code = (int) ((ANum) value).itr();
        full = false;
      }
      conn.error(code, full ? Util.message(ex) : ex.getLocalizedMessage());
    } catch(final IOException ex) {
      final boolean full = conn.context.soptions.get(StaticOptions.RESTXQERRORS);
      conn.error(SC_INTERNAL_SERVER_ERROR, full ? Util.message(ex) : ex.getLocalizedMessage());
    } catch(final JobException ex) {
      conn.stop(ex);
    } catch(final Exception ex) {
      final String message = Util.bug(ex);
      Util.errln(message);
      conn.error(SC_INTERNAL_SERVER_ERROR, Util.info(HTTPText.UNEXPECTED_X, message));
    } finally {
      if(Prop.debug) {
        Util.errln("Request: " + request.getMethod() + ' ' + request.getRequestURL());
        for(final Enumeration<String> en = request.getHeaderNames(); en.hasMoreElements();) {
          final String name = en.nextElement();
          Util.errln("* " + name + ": " + request.getHeader(name));
        }
        Util.errln("Response: " + response.getStatus());
        for(final String name : response.getHeaderNames()) {
          Util.errln("* " + name + ": " + response.getHeader(name));
        }
      }
    }
  }

  /**
   * Runs the code.
   * @param conn HTTP connection
   * @throws Exception any exception
   */
  protected abstract void run(HTTPConnection conn) throws Exception;
}
