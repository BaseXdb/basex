package org.basex.http.rest;

import static javax.servlet.http.HttpServletResponse.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.http.*;
import org.basex.util.http.*;

/**
 * <p>This servlet receives and processes REST requests.</p>
 *
 * @author BaseX Team 2005-21, BSD License
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
    final Context ctx = conn.context;
    if(ctx.soptions.get(StaticOptions.LOGTRACE)) cmd.jc().tracer = ctx.log;

    try {
      cmd.execute(ctx);
      conn.log(SC_OK, "");
    } catch(final BaseXException ex) {
      // ignore error if code was assigned (same error message)
      if(cmd.code == null) throw ex;
    }

    final HTTPCode code = cmd.code;
    if(code != null) throw code.get(cmd.info());
  }

  /**
   * Creates and returns a REST command.
   * @param session session
   * @return code
   * @throws IOException I/O exception
   */
  private static RESTCmd command(final RESTSession session) throws IOException {
    final String mth = session.conn.method;
    if(mth.equals(HttpMethod.GET.name()))    return RESTGet.get(session);
    if(mth.equals(HttpMethod.POST.name()))   return RESTPost.get(session);
    if(mth.equals(HttpMethod.PUT.name()))    return RESTPut.get(session);
    if(mth.equals(HttpMethod.DELETE.name())) return RESTDelete.get(session);
    throw HTTPCode.METHOD_NOT_SUPPORTED_X.get(session.conn.request.getMethod());
  }
}
