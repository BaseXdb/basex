package org.basex.http.rest;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.http.*;

/**
 * Evaluate queries via REST.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
class RESTQuery extends RESTCmd {
  /**
   * Constructor.
   * @param session REST Session
   * @param bindings external bindings
   */
  RESTQuery(final RESTSession session, final Map<String, String[]> bindings) {
    super(session);
    for(final Command cmd : session) {
      if(cmd instanceof XQuery) {
        final XQuery xq = (XQuery) cmd;
        bindings.forEach((key, value) -> xq.bind(key, value[0], value[1]));
      }
    }
  }

  @Override
  protected void run0() throws IOException {
    query();
  }

  /**
   * Evaluates the specified query.
   * @throws HTTPException REST exception
   * @throws IOException I/O exception
   */
  private void query() throws IOException {
    final HTTPConnection conn = session.conn;
    context.options.set(MainOptions.SERIALIZER, conn.sopts());
    context.setExternal(conn.requestCtx);
    conn.initResponse();

    for(final Command cmd : session) {
      if(cmd instanceof XQuery) {
        conn.sopts().assign(((XQuery) cmd).parameters(context));
        conn.initResponse();
      }
      run(cmd, conn.response.getOutputStream());
    }
  }

  /**
   * Creates a new instance of this command.
   * @param session REST session
   * @param query query
   * @param bindings external bindings
   * @return command
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  static RESTQuery get(final RESTSession session, final String query,
      final Map<String, String[]> bindings) throws IOException {

    final String uri = session.conn.context.soptions.get(StaticOptions.WEBPATH);
    session.add(new XQuery(query).baseURI(uri));
    return new RESTQuery(session, bindings);
  }
}
