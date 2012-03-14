package org.basex.http.restxq;

import static javax.servlet.http.HttpServletResponse.*;
import static org.basex.http.restxq.RestXqText.*;

import org.basex.http.*;

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
public final class RestXqServlet extends BaseXServlet {
  @Override
  protected void run() throws Exception {
    // selects an XQuery module for the specified annotation
    final RestXqModule module = RestXqModules.get().find(http);
    // no module found: return 404
    if(module == null) throw new HTTPException(SC_NOT_FOUND, NOT_FOUND);
    // process module
    module.process(http);
  }
}

/* [CG] RESTXQ: Open Issues
 * - resolve conflicting paths: what is "more specific"?
 * - check methods: HEAD must only return rest:reponse element
 * - %rest:form-param (?)
 * - %rest:cookie-param (?)
 * - check compatibility of annotation and function return type
 */
