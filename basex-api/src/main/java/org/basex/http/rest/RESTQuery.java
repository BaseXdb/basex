package org.basex.http.rest;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.http.*;
import org.basex.query.value.type.*;

/**
 * Evaluate queries via REST.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
class RESTQuery extends RESTCmd {
  /** External variables. */
  private final Map<String, String[]> vars;
  /** Optional context value. */
  private final String value;

  /**
   * Constructor.
   * @param session REST Session
   * @param vars external variables
   * @param value context value
   */
  RESTQuery(final RESTSession session, final Map<String, String[]> vars, final String value) {
    super(session);
    this.vars = vars;
    this.value = value;
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
    // set base path and serialization parameters
    final HTTPConnection conn = session.conn;
    context.options.set(MainOptions.SERIALIZER, conn.sopts());
    context.setExternal(conn.requestCtx);
    conn.initResponse();

    for(final Command cmd : session) {
      if(cmd instanceof XQuery) {
        final XQuery xq = (XQuery) cmd;
        // create query instance
        if(value != null) xq.bind(null, value, NodeType.DOCUMENT_NODE.toString());

        // bind HTTP context and external variables
        vars.forEach((key, val) -> {
          if(val.length == 2) xq.bind(key, val[0], val[1]);
          if(val.length == 1) xq.bind(key, val[0]);
        });

        // initializes the response with query serialization options
        conn.sopts().assign(xq.parameters(context));
        conn.initResponse();
      }
      // run command
      run(cmd, conn.response.getOutputStream());
    }
  }

  /**
   * Creates a new instance of this command.
   * @param session REST session
   * @param query query
   * @param variables external variables
   * @param value context value
   * @return command
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  static RESTQuery get(final RESTSession session, final String query,
      final Map<String, String[]> variables, final String value) throws IOException {

    final String uri = session.conn.context.soptions.get(StaticOptions.WEBPATH);
    session.add(new XQuery(query).baseURI(uri));
    return new RESTQuery(session, variables, value);
  }
}
