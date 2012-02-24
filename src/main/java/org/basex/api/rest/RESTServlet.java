package org.basex.api.rest;

import static javax.servlet.http.HttpServletResponse.*;
import java.io.*;

import javax.servlet.http.*;

import org.basex.api.*;
import org.basex.server.*;
import org.basex.util.*;

/**
 * REST Servlet.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class RESTServlet extends HttpServlet {
  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse res)
      throws IOException {
    run(new RESTGet(), req, res);
  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse res)
      throws IOException {
    run(new RESTPost(), req, res);
  }

  @Override
  protected void doPut(final HttpServletRequest req, final HttpServletResponse res)
      throws IOException {
    run(new RESTPut(), req, res);
  }

  @Override
  protected void doDelete(final HttpServletRequest req,
      final HttpServletResponse res) throws IOException {
    run(new RESTDelete(), req, res);
  }

  /**
   * Performs the REST request.
   * @param code code to evaluated
   * @param req servlet request
   * @param res servlet response
   * @throws IOException I/O exception
   */
  private static void run(final RESTCode code, final HttpServletRequest req,
      final HttpServletResponse res) throws IOException {

    final HTTPContext ctx = new HTTPContext(req, res);
    try {
      ctx.session = new HTTPSession(req).login();
    } catch(final LoginException ex) {
      ctx.status(SC_UNAUTHORIZED, ex.getMessage());
      return;
    }

    try {
      code.run(ctx);
      ctx.status(SC_OK, null);
    } catch(final HTTPException ex) {
      ctx.status(ex.getStatus(), ex.getMessage());
    } catch(final IOException ex) {
      ctx.status(SC_BAD_REQUEST, Util.message(ex));
    } catch(final Exception ex) {
      Util.errln(Util.bug(ex));
      ctx.status(SC_INTERNAL_SERVER_ERROR, ex.toString());
    }
  }
}
