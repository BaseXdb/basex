package org.basex.http.rest;

import static javax.servlet.http.HttpServletResponse.*;

import org.basex.http.*;

/**
 * <p>This servlet receives and processes REST requests.</p>
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class RESTServlet extends BaseXServlet {
  @Override
  protected void run() throws Exception {
    final RESTCode code;
    switch(http.method) {
      case DELETE: code = new RESTDelete(); break;
      case GET:    code = new RESTGet();    break;
      case POST:   code = new RESTPost();   break;
      case PUT:    code = new RESTPut();    break;
      default:     throw new HTTPException(SC_NOT_IMPLEMENTED, http.method.toString());
    }
    code.run(http);
  }
}
