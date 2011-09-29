package org.basex.api.rest;

import static javax.servlet.http.HttpServletResponse.*;
import static org.basex.api.rest.RESTText.*;

import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.basex.api.HTTPSession;
import org.basex.server.LoginException;
import org.basex.util.Util;

/**
 * REST Servlet.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class RESTServlet extends HttpServlet {
  @Override
  protected void doGet(final HttpServletRequest req,
      final HttpServletResponse res) throws IOException {
    run(new RESTGet(), req, res);
  }

  @Override
  protected void doPost(final HttpServletRequest req,
      final HttpServletResponse res) throws IOException {
    run(new RESTPost(), req, res);
  }

  @Override
  protected void doPut(final HttpServletRequest req,
      final HttpServletResponse res) throws IOException {
    run(new RESTPut(), req, res);
  }

  @Override
  protected void doDelete(final HttpServletRequest req,
      final HttpServletResponse res) throws IOException {
    run(new RESTDelete(), req, res);
  }

  /**
   * Runs the REST code.
   * @param code code to evaluated
   * @param req servlet request
   * @param res servlet response
   * @throws IOException I/O exception
   */
  private void run(final RESTCode code, final HttpServletRequest req,
      final HttpServletResponse res) throws IOException {

    final RESTContext ctx = new RESTContext(req, res);
    if(ctx.redirect()) return;

    try {
      ctx.session = new HTTPSession(req).login();
    } catch(final LoginException ex) {
      ctx.status(SC_UNAUTHORIZED, ex.getMessage());
      return;
    }

    try {
      code.run(ctx);
      ctx.status(SC_OK, null);
    } catch(final RESTException ex) {
      ctx.status(ex.getStatus(), ex.getMessage());
    } catch(final IOException ex) {
      ctx.status(SC_BAD_REQUEST, Util.message(ex));
    } catch(final Exception ex) {
      Util.errln(Util.bug(ex));
      ctx.status(SC_INTERNAL_SERVER_ERROR, ERR_UNEXPECTED + ex.getMessage());
    }
  }
}
