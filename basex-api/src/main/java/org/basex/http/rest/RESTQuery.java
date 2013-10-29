package org.basex.http.rest;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.http.*;
import org.basex.query.value.type.*;

/**
 * Evaluate queries via REST.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
class RESTQuery extends RESTCmd {
  /** External variables. */
  private final Map<String, String[]> variables;
  /** Optional context value. */
  private final String value;

  /**
   * Constructor.
   * @param rs REST Session
   * @param vars external variables
   * @param val context value
   */
  RESTQuery(final RESTSession rs, final Map<String, String[]> vars, final String val) {
    super(rs);
    variables = vars;
    value = val;
  }

  @Override
  protected void run0() throws IOException {
    query(session.context.globalopts.get(GlobalOptions.WEBPATH));
  }

  /**
   * Evaluates the specified query.
   * @param path set query path
   * @throws HTTPException REST exception
   * @throws IOException I/O exception
   */
  protected void query(final String path) throws IOException {
    final XQuery xq;
    int c = 0;
    while(!(cmds.get(c) instanceof XQuery)) run(cmds.get(c++));
    xq = (XQuery) cmds.get(c);

    // create query instance
    if(value != null) xq.bind(null, value, NodeType.DOC.toString());

    // set base path and serialization parameters
    final HTTPContext http = session.http;
    context.options.set(MainOptions.QUERYPATH, path);
    context.options.set(MainOptions.SERIALIZER, serial(http));

    // bind HTTP context and external variables
    xq.http(http);
    for(final Entry<String, String[]> e : variables.entrySet()) {
      final String key = e.getKey();
      final String[] val = e.getValue();
      if(val.length == 2) xq.bind(key, val[0], val[1]);
      if(val.length == 1) xq.bind(key, val[0]);
    }

    // initializes the response with query serialization options
    http.serialization.parse(xq.parameters(context).toString());
    http.initResponse();
    // run query
    run(xq, http.res.getOutputStream());
  }

  /**
   * Creates a new instance of this command.
   * @param rs REST session
   * @param query query
   * @param vars external variables
   * @param val context value
   * @return command
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  static RESTQuery get(final RESTSession rs, final String query, final Map<String, String[]> vars,
      final String val) throws IOException {

    open(rs);
    rs.add(new XQuery(query));
    return new RESTQuery(rs, vars, val);
  }
}
