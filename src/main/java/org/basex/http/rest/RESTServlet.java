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
  protected void run() throws Exception {
    code(http.method).run(http);
  }

  /**
   * Returns the correct code for the specified HTTP method, or an exception.
   * @param mth HTTP method
   * @return code
   * @throws HTTPException HTTP exception
   */
  private RESTCode code(final HTTPMethod mth) throws HTTPException {
    if(mth == HTTPMethod.GET)    return new RESTGet();
    if(mth == HTTPMethod.POST)   return new RESTPost();
    if(mth == HTTPMethod.PUT)    return new RESTPut();
    if(mth == HTTPMethod.DELETE) return new RESTDelete();
    throw HTTPErr.NOT_IMPLEMENTED_X.thrw(http.req.getMethod());
  }
}
