package org.basex.api.restxq;

import static javax.servlet.http.HttpServletResponse.*;
import static org.basex.api.restxq.RestXqText.*;

import java.io.*;

import javax.servlet.http.*;

import org.basex.api.*;
import org.basex.query.*;
import org.basex.server.*;
import org.basex.util.*;

/**
 * <p>This servlet receives and processes REST requests.
 * The evaluated code is defined in XQuery modules, which are located in the web server's
 * root directory (specified by the {@code HTTPPATH} option), and decorated with RESTful
 * annotations.</p>
 *
 * <p>The implementation is based on Adam Retter's paper presented at XMLPrague 2012,
 * titled "RESTful XQuery - Standardised XQuery 3.0 Annotations for REST".</p>
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class RestXqServlet extends HttpServlet {
  @Override
  protected void service(final HttpServletRequest req, final HttpServletResponse res)
      throws IOException {

    // creates a new context
    final HTTPContext http = new HTTPContext(req, res);
    try {
      // creates a new database session
      http.session = new HTTPSession(req).login();

      // selects an XQuery module for the specified annotation
      final RestXqModule module = RestXqModules.get().find(http);

      // no module found: return 404
      if(module == null) throw new HTTPException(SC_NOT_FOUND, NOT_FOUND);

      // process module
      module.process(http);

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
      http.status(SC_INTERNAL_SERVER_ERROR, Util.info(UNEXPECTED_ERROR, ex.getMessage()));
    }
  }
}

/* [CG] RestXQ: OPEN ISSUES
 * - resolve conflicting paths: what is "more specific"?
 * - check methods: HEAD must only return rest:reponse element
 * - %rest:query-param: query string parameters
 * - %rest:form-param: "application/x-www-form-urlencoded"
 * - %rest:header-param: request headers
 * - %rest:cookie-param: cookies
 * - check compatibility of annotation and function return type
 */
