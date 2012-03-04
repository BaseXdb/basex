package org.basex.api.rest;

import static javax.servlet.http.HttpServletResponse.*;
import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.basex.api.*;
import org.basex.server.*;
import org.basex.util.*;

/**
 * <p>This servlet receives and processes REST requests.</p>
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class RESTServlet extends HttpServlet {
  @Override
  protected void service(final HttpServletRequest req, final HttpServletResponse res)
      throws IOException, ServletException {

    final HTTPContext http = new HTTPContext(req, res);
    final RESTCode code;
    switch(http.method) {
      case DELETE: code = new RESTDelete(); break;
      case GET:    code = new RESTGet();    break;
      case POST:   code = new RESTPost();   break;
      case PUT:    code = new RESTPut();    break;
      default:     super.service(req, res); return;
    }

    try {
      http.session = new HTTPSession(req).login();
    } catch(final LoginException ex) {
      http.status(SC_UNAUTHORIZED, ex.getMessage());
      return;
    }

    try {
      code.run(http);
      http.status(SC_OK, null);
    } catch(final HTTPException ex) {
      http.status(ex.getStatus(), ex.getMessage());
    } catch(final IOException ex) {
      http.status(SC_BAD_REQUEST, Util.message(ex));
    } catch(final Exception ex) {
      Util.errln(Util.bug(ex));
      http.status(SC_INTERNAL_SERVER_ERROR, ex.toString());
    }
  }
}
