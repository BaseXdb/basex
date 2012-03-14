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
  /** HTTP context. */
  protected HTTPContext http;

  @Override
  public final void init(final ServletConfig config) throws ServletException {
    // get context
    final ServletContext sc = config.getServletContext();
    // get context parameter from web.xml
    String home = sc.getInitParameter(Util.PATH);
    // get root path of servlet context
    if(home == null || home.isEmpty()) home = sc.getRealPath("/");
    // set property (will later be evaluated by the Context constructor)
    setProperty(Util.PATH, home);

    // set remaining options
    setProperty(HTTPText.DBMODE, sc.getInitParameter(HTTPText.DBMODE));
    setProperty(HTTPText.DBUSER, sc.getInitParameter(HTTPText.DBUSER));
    setProperty(HTTPText.DBPASS, sc.getInitParameter(HTTPText.DBPASS));
  }

  /**
   * Sets the specified system property if its value is not {@code null} or empty.
   * @param key key to be set
   * @param value value to be set
   */
  private void setProperty(final String key, final String value) {
    if(value != null && !value.isEmpty()) System.setProperty(key, value);
  }

  @Override
  public final void service(final HttpServletRequest req, final HttpServletResponse res)
      throws IOException {

    http = new HTTPContext(req, res);
    try {
      run();
    } catch(final HTTPException ex) {
      http.status(ex.getStatus(), ex.getMessage());
    } catch(final LoginException ex) {
      http.status(SC_UNAUTHORIZED, ex.getMessage());
    } catch(final IOException ex) {
      http.status(SC_BAD_REQUEST, Util.message(ex));
    } catch(final QueryException ex) {
      http.status(SC_BAD_REQUEST, ex.getMessage());
    } catch(final Exception ex) {
      Util.errln(Util.bug(ex));
      http.status(SC_INTERNAL_SERVER_ERROR, Util.info(UNEXPECTED, Util.message(ex)));
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
   * @throws Exception exception
   */
  protected abstract void run() throws Exception;
}
