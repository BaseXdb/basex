package org.basex.http.restxq;

import static org.basex.http.web.WebText.*;

import javax.servlet.http.*;

import org.basex.http.*;
import org.basex.http.web.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * <p>This servlet receives and processes REST requests.
 * The evaluated code is defined in XQuery modules, which are located in the web server's
 * root directory (specified by the {@code HTTPPATH} option), and decorated with RESTXQ
 * annotations.</p>
 *
 * <p>The implementation is based on Adam Retter's paper presented at XMLPrague 2012,
 * titled "RESTful XQuery - Standardised XQuery 3.0 Annotations for REST".</p>
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class RestXqServlet extends BaseXServlet {
  @Override
  protected void run(final HTTPConnection conn) throws Exception {
    // no trailing slash: send redirect
    if(conn.req.getPathInfo() == null) {
      conn.redirect("/");
      return;
    }

    // analyze input path
    final WebModules modules = WebModules.get(conn.context);

    // initialize RESTXQ
    if(conn.path().equals('/' + INIT)) {
      modules.init();
      return;
    }

    // select the closest match for this request
    RestXqFunction func = modules.restxq(conn, null);
    if(func == null) throw HTTPCode.NO_XQUERY.get();

    final RestXqResponse response = new RestXqResponse(conn);
    try {
      for(final RestXqFunction check : modules.checks(conn)) {
        // skip further checks if function results a result
        if(response.create(check, func)) return;
      }

      // process function
      response.create(func, null);

    } catch(final QueryException ex) {
      // run optional error function
      func = modules.restxq(conn, ex.qname());
      if(func == null) throw ex;
      response.create(func, ex);
    }
  }

  @Override
  public String username(final HTTPConnection http) {
    // try to retrieve session id (DBA, global one)
    final HttpSession session = http.req.getSession(false);
    if(session != null) {
      final String id = (http.path() + '/').contains('/' + DBA + '/') ? DBA : ID;
      final byte[] value = HTTPContext.token(session.getAttribute(id));
      if(value != null) return Token.string(value);
    }
    return super.username(http);
  }
}
