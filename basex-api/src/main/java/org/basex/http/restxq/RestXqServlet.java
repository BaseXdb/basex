package org.basex.http.restxq;

import static jakarta.servlet.http.HttpServletResponse.*;

import org.basex.http.*;
import org.basex.http.web.*;
import org.basex.http.web.WebResponse.Response;
import org.basex.query.*;
import org.basex.util.http.*;

/**
 * <p>This servlet receives and processes REST requests.
 * The evaluated code is defined in XQuery modules, which are located in the web server's
 * root directory (specified by the {@code HTTPPATH} option), and decorated with RESTXQ
 * annotations.</p>
 *
 * <p>The implementation is based on Adam Retter's paper presented at XMLPrague 2012,
 * titled "RESTful XQuery - Standardised XQuery 3.0 Annotations for REST".</p>
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class RestXqServlet extends BaseXServlet {
  @Override
  protected void run(final HTTPConnection conn) throws Exception {
    // no trailing slash: send redirect
    if(conn.request.getPathInfo() == null && conn.request.getServletPath().isEmpty()) {
      conn.redirect("/");
      return;
    }

    // analyze input path
    final WebModules modules = WebModules.get(conn.context);

    // initialize RESTXQ
    if(conn.path().equals('/' + WebText.INIT)) {
      modules.init(false);
      return;
    }

    // choose function to process
    RestXqFunction func = modules.restxq(conn, null);
    boolean body = true;

    // HEAD: evaluate GET and discard body, unless a function is declared for HEAD
    if(conn.method.equals(Method.HEAD.name()) &&
        (func == null || !func.methods.contains(Method.HEAD.name()))) {
      conn.method = Method.GET.name();
      final RestXqFunction get = modules.restxq(conn, null);
      if(get != null) {
        func = get;
        body = false;
      } else {
        // restore the original method to report it in the error message
        conn.method = Method.HEAD.name();
      }
    }

    // no function found: answer OPTIONS requests for known paths, reject other requests
    if(func == null) {
      final String allowed = conn.method.equals(Method.OPTIONS.name()) ?
        modules.allowed(conn) : null;
      if(allowed == null) throw modules.noMatch(conn);
      conn.response.setHeader(HTTPText.ALLOW, allowed);
      return;
    }

    try {
      // run checks; stop further processing if a function produces a response
      for(final RestXqFunction check : modules.checks(conn)) {
        if(new RestXqResponse(conn).create(check, func, body) != Response.NONE) return;
      }
      // run addressed function
      if(new RestXqResponse(conn).create(func, null, body) != Response.CUSTOM) {
        conn.log(SC_OK, "");
      }
    } catch(final QueryException ex) {
      // run optional error function
      func = modules.restxq(conn, ex.qname());
      if(func == null) throw ex;

      new RestXqResponse(conn).create(func, ex, body);
    }
  }
}
