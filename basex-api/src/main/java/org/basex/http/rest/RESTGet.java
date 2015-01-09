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
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class RESTGet {
  /** Private constructor. */
  private RESTGet() { }

  /**
   * Creates REST code.
   * @param session REST session
   * @return code
   * @throws IOException I/O exception
   */
  public static RESTCmd get(final RESTSession session) throws IOException {
    final Map<String, String[]> vars = new HashMap<>();

    // parse query string
    String op = null, input = null, value = null;
    final HTTPContext http = session.http;
    final SerializerOptions sopts = http.sopts();
    for(final Entry<String, String[]> param : http.params.map().entrySet()) {
      final String key = param.getKey();
      final String[] vals = param.getValue();
      final String val = vals[0];

      if(Strings.eqic(key, COMMAND, QUERY, RUN)) {
        if(op != null || vals.length > 1) throw HTTPCode.ONEOP.get();
        op = key;
        input = val;
      } else if(key.equalsIgnoreCase(WRAP)) {
        // wrapping flag
        http.wrapping = Strings.yes(val);
      } else if(key.equalsIgnoreCase(CONTEXT)) {
        // context parameter
        value = val;
      } else if(sopts.option(key) != null) {
        // serialization parameters
        for(final String v : vals) sopts.assign(key, v);
      } else if(!RESTCmd.parseOption(session, param, false)) {
        // options or (if not found) external variables
        vars.put(key, new String[] { val });
      }
    }

    if(op == null) return RESTRetrieve.get(session);
    if(op.equals(QUERY)) return RESTQuery.get(session, input, vars, value);
    if(op.equals(RUN)) return RESTRun.get(session, input, vars, value);
    return RESTCommand.get(session, input);
  }
}
