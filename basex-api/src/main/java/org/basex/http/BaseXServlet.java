package org.basex.http;

import static jakarta.servlet.http.HttpServletResponse.*;

import java.io.*;
import java.util.*;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import org.basex.core.*;
import org.basex.core.StaticOptions.*;
import org.basex.core.jobs.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.server.*;
import org.basex.util.*;
import org.basex.util.http.*;

/**
 * Base class for various servlets.
 *
 * @author BaseX Team, BSD License
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
    username = initParam(config, StaticOptions.USER.name());
    final String method = initParam(config, StaticOptions.AUTHMETHOD.name());
    if(method != null) auth = AuthMethod.valueOf(method);

    final Context ctx = hc.context();
    if(ctx.soptions.get(StaticOptions.LOGTRACE)) ctx.setExternal(ctx.log);
  }

  @Override
  public final void service(final HttpServletRequest request, final HttpServletResponse response)
      throws IOException {

    final HTTPConnection conn = new HTTPConnection(request, response, auth, null);
    try {
      conn.authenticate(username);
      run(conn);
    } catch(final Exception ex) {
      error(conn, ex);
    } finally {
      if(Prop.debug) {
        Util.errln("Request: " + request.getMethod() + ' ' + request.getRequestURL());
        for(final String name : Collections.list(request.getHeaderNames())) {
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

  /**
   * Returns the value of a servlet-specific initialization parameter.
   * @param config servlet configuration
   * @param name name of parameter (without database prefix)
   * @return value, or {@code null} if the parameter is not specified
   */
  public static String initParam(final ServletConfig config, final String name) {
    for(final String param : Collections.list(config.getInitParameterNames())) {
      if(param.startsWith(Prop.DBPREFIX) &&
          param.substring(Prop.DBPREFIX.length()).equalsIgnoreCase(name)) {
        return config.getInitParameter(param);
      }
    }
    return null;
  }

  /**
   * Handles a servlet exception and sends an error response.
   * @param conn HTTP connection
   * @param ex exception
   * @throws IOException I/O exception
   */
  public static void error(final HTTPConnection conn, final Exception ex) throws IOException {
    if(ex instanceof final HTTPException hex) {
      conn.error(hex.getStatus(), Util.message(hex));
    } else if(ex instanceof LoginException) {
      conn.error(SC_UNAUTHORIZED, Util.message(ex));
    } else if(ex instanceof final QueryException qex) {
      int code = SC_INTERNAL_SERVER_ERROR;
      boolean full = conn.context.soptions.get(StaticOptions.RESTXQERRORS);
      final QNm qname = qex.qname();
      if(Token.eq(qname.uri(), QueryText.REST_URI)) {
        // status code is encoded in the local name (e.g. 'status404')
        code = Token.toInt(Token.substring(qname.local(), QueryText.STATUS.length));
        full = false;
      }
      final SerializerOptions sopts = qex.output();
      if(sopts != null) {
        // render the error value as the response body
        String body;
        try {
          body = qex.value().serialize(sopts).toString();
        } catch(final QueryIOException e) {
          Util.debug(e);
          body = qex.getLocalizedMessage();
        }
        conn.error(code, qex.getLocalizedMessage(), body, sopts.mediaType());
      } else {
        conn.error(code, full ? Util.message(qex) : qex.getLocalizedMessage());
      }
    } else if(ex instanceof IOException) {
      final boolean full = conn.context.soptions.get(StaticOptions.RESTXQERRORS);
      conn.error(SC_INTERNAL_SERVER_ERROR, full ? Util.message(ex) : ex.getLocalizedMessage());
    } else if(ex instanceof final JobException jex) {
      conn.stop(jex);
    } else {
      final String message = Util.bug(ex);
      Util.errln(message);
      conn.error(SC_INTERNAL_SERVER_ERROR, Util.info(HTTPText.UNEXPECTED_X, message));
    }
  }
}
