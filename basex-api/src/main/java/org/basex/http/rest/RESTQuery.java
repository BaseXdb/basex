package org.basex.http.rest;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.http.*;
import org.basex.io.serial.*;

/**
 * Evaluate queries via REST.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
class RESTQuery extends RESTCmd {
  /**
   * Constructor.
   * @param session REST Session
   * @param bindings external bindings
   */
  RESTQuery(final RESTSession session, final Map<String, Entry<Object, String>> bindings) {
    super(session);
    for(final Command cmd : session) {
      if(cmd instanceof XQuery) {
        final XQuery xq = (XQuery) cmd;
        bindings.forEach(xq::bind);
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
    final SerializerOptions sopts = conn.sopts();
    context.options.set(MainOptions.SERIALIZER, sopts);
    context.setExternal(conn.requestCtx);
    conn.initResponse();

    final OutputStream os = conn.response.getOutputStream();
    for(final Command cmd : session) {
      if(cmd instanceof XQuery) {
        sopts.assign(((XQuery) cmd).parameters(context));
        conn.initResponse();
      }
      run(cmd, os);
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
      final Map<String, Map.Entry<Object, String>> bindings) throws IOException {

    final String uri = session.conn.context.soptions.get(StaticOptions.WEBPATH);
    return new RESTQuery(session.add(new XQuery(query).baseURI(uri)), bindings);
  }
}
