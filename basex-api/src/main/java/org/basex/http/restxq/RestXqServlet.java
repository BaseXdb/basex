package org.basex.http.restxq;

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
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class RestXqServlet extends BaseXServlet {
  @Override
  protected void run(final HTTPContext http) throws Exception {
    // authenticate user
    http.session();

    // analyze input path
    final RestXqModules rxm = RestXqModules.get();
    // select XQuery function
    RestXqFunction func = rxm.find(http, null);
    if(func == null) HTTPErr.NO_XQUERY.thrw();
    try {
      // process function that matches the current request
      func.process(http, null);
    } catch(final QueryException ex) {
      // process optional error function
      func = rxm.find(http, ex.qname());
      if(func == null) throw ex;
      func.process(http, ex);
    }
  }
}
