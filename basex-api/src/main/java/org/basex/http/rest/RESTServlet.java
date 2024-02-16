package org.basex.http.rest;

import static jakarta.servlet.http.HttpServletResponse.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.http.*;
import org.basex.util.http.*;

/**
 * <p>This servlet receives and processes REST requests.</p>
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class RESTServlet extends BaseXServlet {
  @Override
  protected void run(final HTTPConnection conn) throws IOException {
    // open database if name was specified
    final RESTSession session = new RESTSession(conn);
    final String db = conn.db(), path = conn.dbpath();
    if(!db.isEmpty()) session.add(new Open(db, path));

    // generate and run commands
    final RESTCmd cmd = command(session);
    try {
      cmd.execute(conn.context);
      conn.log(SC_OK, "");
    } catch(final BaseXException ex) {
      // ignore error if code was assigned (same error message)
      if(cmd.status == null) throw ex;
    }

    final HTTPStatus status = cmd.status;
    if(status != null) throw status.get(cmd.info());
  }

  /**
   * Creates and returns a REST command.
   * @param session session
   * @return code
   * @throws IOException I/O exception
   */
  private static RESTCmd command(final RESTSession session) throws IOException {
    final String method = session.conn.method;
    if(method.equals(Method.GET.name()))    return RESTGet.get(session);
    if(method.equals(Method.POST.name()))   return RESTPost.get(session);
    if(method.equals(Method.PUT.name()))    return RESTPut.get(session);
    if(method.equals(Method.DELETE.name())) return RESTDelete.get(session);
    throw HTTPStatus.METHOD_NOT_SUPPORTED_X.get(session.conn.request.getMethod());
  }
}
