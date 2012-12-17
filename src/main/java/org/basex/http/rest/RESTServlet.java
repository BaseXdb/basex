package org.basex.http.rest;

import org.basex.http.*;

/**
 * <p>This servlet receives and processes REST requests.</p>
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class RESTServlet extends BaseXServlet {
  @Override
  protected void run(final HTTPContext http) throws Exception {
    code(http).run(http);
  }

  /**
   * Returns the correct code for the specified HTTP method, or an exception.
   * @param http HTTP method
   * @return code
   * @throws HTTPException HTTP exception
   */
  private static RESTCode code(final HTTPContext http) throws HTTPException {
    final HTTPMethod mth = http.method;
    if(mth == HTTPMethod.GET)    return new RESTGet();
    if(mth == HTTPMethod.POST)   return new RESTPost();
    if(mth == HTTPMethod.PUT)    return new RESTPut();
    if(mth == HTTPMethod.DELETE) return new RESTDelete();
    throw HTTPErr.NOT_IMPLEMENTED_X.thrw(http.req.getMethod());
  }
}
