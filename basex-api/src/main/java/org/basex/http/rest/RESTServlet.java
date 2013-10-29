package org.basex.http.rest;

import java.io.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.http.*;

/**
 * <p>This servlet receives and processes REST requests.</p>
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class RESTServlet extends BaseXServlet {
  @Override
  protected void run(final HTTPContext http) throws IOException {
    final RESTSession rs = new RESTSession(http, http.authenticate());
    final RESTCmd cmd = code(rs);
    try {
      cmd.execute(rs.context);
    } catch(final BaseXException ex) {
      // catch "database not found" message
      final String msg = Open.dbnf(http.db());
      if(ex.getMessage().equals(msg)) HTTPCode.NOT_FOUND_X.thrw(msg);
      throw ex;
    } finally {
      new Close().run(rs.context);
    }

    final HTTPCode code = cmd.code;
    if(code != null) code.thrw(cmd.info());
  }

  /**
   * Returns the correct code for the specified HTTP method, or an exception.
   * @param rs session
   * @return code
   * @throws IOException I/O exception
   */
  private static RESTCmd code(final RESTSession rs) throws IOException {
    final HTTPMethod mth = rs.http.method;
    if(mth == HTTPMethod.GET)    return RESTGet.get(rs);
    if(mth == HTTPMethod.POST)   return RESTPost.get(rs);
    if(mth == HTTPMethod.PUT)    return RESTPut.get(rs);
    if(mth == HTTPMethod.DELETE) return RESTDelete.get(rs);
    throw HTTPCode.NOT_IMPLEMENTED_X.thrw(rs.http.req.getMethod());
  }
}
