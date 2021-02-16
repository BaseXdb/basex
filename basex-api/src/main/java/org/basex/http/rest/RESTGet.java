package org.basex.http.rest;

import static org.basex.http.rest.RESTText.*;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.basex.http.*;
import org.basex.io.serial.*;
import org.basex.util.*;

/**
 * This class processes GET requests sent to the REST server.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class RESTGet {
  /** Private constructor. */
  private RESTGet() { }

  /**
   * Creates and returns a REST command.
   * @param session REST session
   * @return code
   * @throws IOException I/O exception
   */
  public static RESTCmd get(final RESTSession session) throws IOException {
    final Map<String, String[]> variables = new HashMap<>();

    // parse query string
    String op = null, input = null, value = null;
    final HTTPConnection conn = session.conn;
    final SerializerOptions sopts = conn.sopts();
    for(final Entry<String, String[]> param : conn.requestCtx.queryStrings().entrySet()) {
      final String key = param.getKey();
      final String[] values = param.getValue();

      if(Strings.eqic(key, COMMAND, QUERY, RUN)) {
        if(op != null || values.length > 1)
          throw HTTPCode.MULTIPLE_OPS_X.get(String.join(", ", values));
        op = key;
        input = values[0];
      } else if(key.equalsIgnoreCase(CONTEXT)) {
        // context parameter
        value = values[0];
      } else if(sopts.option(key) != null) {
        // serialization parameters
        for(final String val : values) sopts.assign(key, val);
      } else if(!RESTCmd.parseOption(session, param, false)) {
        // options or (if not found) external variables
        variables.put(key, new String[] { values[0] });
      }
    }

    if(op == null) return RESTRetrieve.get(session);
    if(op.equals(QUERY)) return RESTQuery.get(session, input, variables, value);
    if(op.equals(RUN)) return RESTRun.get(session, input, variables, value);
    return RESTCommand.get(session, input);
  }
}
