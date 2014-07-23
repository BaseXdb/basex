package org.basex.http.rest;

import java.io.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.http.*;

/**
 * <p>This servlet receives and processes REST requests.</p>
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class RESTServlet extends BaseXServlet {
  @Override
  protected void run(final HTTPContext http) throws IOException {

   //System.out.println(http.context().globalopts);
    //System.out.println(http.nonce);


    final RESTSession rs = new RESTSession(http, http.authenticate());
    final RESTCmd cmd = code(rs);
    try {
      cmd.execute(rs.context);
    } catch(final BaseXException ex) {
      // catch "database not found" message
      final String msg = Open.dbnf(http.db());
      if(ex.getMessage().equals(msg)) throw HTTPCode.NOT_FOUND_X.get(msg);
      throw ex;
    }

    final HTTPCode code = cmd.code;
    if(code != null) throw code.get(cmd.info());
  }

  /**
   * Returns the correct code for the specified HTTP method, or an exception.
   * @param rs session
   * @return code
   * @throws IOException I/O exception
   */
  private static RESTCmd code(final RESTSession rs) throws IOException {
    final String mth = rs.http.method;
    if(mth.equals(HTTPMethod.GET.name()))    return RESTGet.get(rs);
    if(mth.equals(HTTPMethod.POST.name()))   return RESTPost.get(rs);
    if(mth.equals(HTTPMethod.PUT.name()))    return RESTPut.get(rs);
    if(mth.equals(HTTPMethod.DELETE.name())) return RESTDelete.get(rs);
    throw HTTPCode.NOT_IMPLEMENTED_X.get(rs.http.req.getMethod());
  }
}
