package org.basex.http.restxq;

import static org.basex.http.restxq.RestXqText.*;

import org.basex.http.*;
import org.basex.query.*;

/**
 * <p>This servlet receives and processes REST requests.
 * The evaluated code is defined in XQuery modules, which are located in the web server's
 * root directory (specified by the {@code HTTPPATH} option), and decorated with RESTXQ
 * annotations.</p>
 *
 * <p>The implementation is based on Adam Retter's paper presented at XMLPrague 2012,
 * titled "RESTful XQuery - Standardised XQuery 3.0 Annotations for REST".</p>
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class RestXqServlet extends BaseXServlet {
  @Override
  protected void run(final HTTPContext http) throws Exception {
    // no trailing slash: send redirect
    if(http.req.getPathInfo() == null) {
      http.redirect("/");
      return;
    }

    // analyze input path
    final RestXqModules rxm = RestXqModules.get();

    // initialize RESTXQ
    if(http.path().equals("/" + INIT)) {
      rxm.init();
      return;
    }

    // select function that closest matches the request
    RestXqFunction func = rxm.find(http, null);
    if(func == null) throw HTTPCode.NO_XQUERY.get();

    try {
      // process function
      func.process(http, null);
    } catch(final QueryException ex) {
      // run optional error function
      func = rxm.find(http, ex.qname());
      if(func == null) throw ex;
      func.process(http, ex);
    }
  }
}
