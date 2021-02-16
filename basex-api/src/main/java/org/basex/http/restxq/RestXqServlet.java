package org.basex.http.restxq;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.stream.*;

import org.basex.http.*;
import org.basex.http.web.*;
import org.basex.http.web.WebResponse.*;
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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class RestXqServlet extends BaseXServlet {
  @Override
  protected void run(final HTTPConnection conn) throws Exception {
    // no trailing slash: send redirect
    if(conn.request.getPathInfo() == null) {
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

    // no function found? check alternatives
    if(func == null) {
      // OPTIONS: no custom response required
      if(conn.method.equals(HttpMethod.OPTIONS.name())) {
        conn.response.setHeader(HttpText.ALLOW, Stream.of(HttpMethod.values()).map(Enum::name).
            collect(Collectors.joining(",")));
        return;
      }
      // HEAD: evaluate GET, discard body
      if(conn.method.equals(HttpMethod.HEAD.name())) {
        conn.method = HttpMethod.GET.name();
        func = modules.restxq(conn, null);
        body = false;
      }
      if(func == null) throw HTTPCode.SERVICE_NOT_FOUND.get();
    }

    // create response
    final RestXqResponse response = new RestXqResponse(conn);
    try {
      // run checks; stop further processing if a function produces a response
      for(final RestXqFunction check : modules.checks(conn)) {
        if(response.create(check, func, body) != Response.NONE) return;
      }

      // run addressed function
      if(response.create(func, null, body) != Response.CUSTOM) conn.log(SC_OK, "");

    } catch(final QueryException ex) {
      // run optional error function
      func = modules.restxq(conn, ex.qname());
      if(func == null) throw ex;

      response.create(func, ex, body);
    }
  }
}
