package org.basex.http.restxq;

import org.basex.http.*;

/**
 * <p>This servlet receives and processes REST requests.
 * The evaluated code is defined in XQuery modules, which are located in the web server's
 * root directory (specified by the {@code HTTPPATH} option), and decorated with RESTXQ
 * annotations.</p>
 *
 * <p>The implementation is based on Adam Retter's paper presented at XMLPrague 2012,
 * titled "RESTful XQuery - Standardised XQuery 3.0 Annotations for REST".</p>
 *
 * @author BaseX Team 2005-12, BSD License
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
    final RestXqFunction func = rxm.find(http);
    if(func == null) HTTPErr.NO_XQUERY.thrw();
    // process function
    func.process(http);
  }
}
