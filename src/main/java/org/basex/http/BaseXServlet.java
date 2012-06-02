package org.basex.http;

import static javax.servlet.http.HttpServletResponse.*;
import static org.basex.http.HTTPText.*;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.server.*;
import org.basex.util.*;

/**
 * <p>Base class for all servlets.</p>
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Dimitar Popov
 */
public abstract class BaseXServlet extends HttpServlet {
  @Override
  public final void init(final ServletConfig config) throws ServletException {
    try {
      HTTPContext.init(config.getServletContext());
    } catch(final IOException ex) {
      throw new ServletException(ex);
    }
  }

  @Override
  public final void service(final HttpServletRequest req, final HttpServletResponse res)
      throws IOException {

    final HTTPContext http = new HTTPContext(req, res);
    try {
      run(http);
    } catch(final HTTPException ex) {
      Util.debug(ex);
      http.status(ex.getStatus(), ex.getMessage());
    } catch(final LoginException ex) {
      Util.debug(ex);
      http.status(SC_UNAUTHORIZED, ex.getMessage());
    } catch(final IOException ex) {
      Util.debug(ex);
      http.status(SC_BAD_REQUEST, Util.message(ex));
    } catch(final QueryException ex) {
      Util.debug(ex);
      http.status(SC_BAD_REQUEST, ex.getMessage());
    } catch(final Exception ex) {
      Util.errln(Util.bug(ex));
      http.status(SC_INTERNAL_SERVER_ERROR, Util.info(UNEXPECTED, ex));
    } finally {
      if(Boolean.parseBoolean(System.getProperty(HTTPText.DBVERBOSE))) {
        Util.out("_ REQUEST ___________________________________" + Prop.NL + req);
        Util.out("_ RESPONSE __________________________________" + Prop.NL + res);
      }
      http.close();
    }
  }

  /**
   * Runs the code.
   * @param http HTTP context
   * @throws Exception exception
   */
  protected abstract void run(final HTTPContext http) throws Exception;
}
