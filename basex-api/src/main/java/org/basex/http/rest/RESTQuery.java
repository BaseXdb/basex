package org.basex.http.rest;

import static org.basex.core.Text.*;
import static org.basex.http.rest.RESTText.*;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.cmd.Set;
import org.basex.http.*;
import org.basex.io.in.*;
import org.basex.io.serial.*;
import org.basex.server.*;
import org.basex.util.*;

/**
 * REST-based evaluation of XQuery expressions.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
class RESTQuery extends RESTCode {
  /** External variables. */
  protected final Map<String, String[]> variables;
  /** Query input. */
  protected final String input;
  /** Optional context item. */
  protected final byte[] item;

  /**
   * Constructor.
   * @param in query to be executed
   * @param vars external variables
   * @param it context item
   */
  RESTQuery(final String in, final Map<String, String[]> vars, final byte[] it) {
    input = in;
    variables = vars;
    item = it;
  }

  @Override
  void run(final HTTPContext http) throws IOException {
    query(input, http, http.context().globalopts.get(GlobalOptions.WEBPATH));
  }

  /**
   * Evaluates the specified query.
   * @param in query input
   * @param http HTTP context
   * @param path set query path
   * @throws HTTPException REST exception
   * @throws IOException I/O exception
   */
  protected void query(final String in, final HTTPContext http, final String path)
      throws IOException {

    final LocalSession session = http.session();
    if(item != null) {
      // create main memory instance of the document specified as context node
      final boolean mm = session.execute(
          new Get(MainOptions.MAINMEM)).split(COLS)[1].equals(TRUE);
      session.execute(new Set(MainOptions.MAINMEM, true));
      session.create(Util.className(RESTQuery.class), new ArrayInput(item));
      if(!mm) session.execute(new Set(MainOptions.MAINMEM, false));
    } else {
      // open addressed database
      open(http);
    }

    // send serialization options to the server
    session.execute(new Set(MainOptions.SERIALIZER, serial(http)));
    session.setOutputStream(http.res.getOutputStream());
    // set base path to correctly resolve local references
    session.execute(new Set(MainOptions.QUERYPATH, path));

    // create query instance and bind http context
    final Query qu = session.query(in);
    qu.context(http);

    // bind external variables
    for(final Entry<String, String[]> e : variables.entrySet()) {
      final String[] val = e.getValue();
      if(val.length == 2) qu.bind(e.getKey(), val[0], val[1]);
      if(val.length == 1) qu.bind(e.getKey(), val[0]);
    }
    // initializes the response with query serialization options
    http.initResponse(new SerializerOptions(qu.options()));
    // run query
    qu.execute();
  }

  /**
   * Returns a string representation of the used serialization parameters.
   * @param http HTTP context
   * @return serialization parameters
   */
  static String serial(final HTTPContext http) {
    final TokenBuilder ser = new TokenBuilder(http.serialization);
    if(http.wrapping) {
      if(!ser.isEmpty()) ser.add(',');
      ser.addExt(SerializerOptions.S_WRAP_PREFIX.name()).add('=').add(REST).add(',');
      ser.addExt(SerializerOptions.S_WRAP_URI.name()).add('=').add(RESTURI);
    }
    return ser.toString();
  }
}
