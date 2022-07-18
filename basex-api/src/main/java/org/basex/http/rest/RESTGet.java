package org.basex.http.rest;

import static org.basex.http.rest.RESTText.*;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import org.basex.http.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * This class processes GET requests sent to the REST server.
 *
 * @author BaseX Team 2005-22, BSD License
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
    final Map<String, String[]> bindings = new HashMap<>();

    // parse query string
    String op = null, input = null, value = null;
    final HTTPConnection conn = session.conn;
    final Options sopts = conn.sopts(), mopts = conn.context.options;
    for(final Entry<String, String[]> param : conn.requestCtx.queryStrings().entrySet()) {
      final String key = param.getKey();
      final String[] values = param.getValue();

      if(Strings.eqic(key, COMMAND, QUERY, RUN)) {
        if(op != null || values.length > 1)
          throw HTTPStatus.MULTIPLE_OPS_X.get(String.join(", ", values));
        op = key;
        input = values[0];
      } else if(key.equalsIgnoreCase(CONTEXT)) {
        // context parameter
        value = values[0];
      } else if(!RESTCmd.assign(sopts, param, false) && !RESTCmd.assign(mopts, param, false)) {
        // assign database option, serialization parameter or external variable
        bindings.put(key, new String[] { values[0], null });
      }
    }
    if(value != null) {
      bindings.put(null, new String[] { value, NodeType.DOCUMENT_NODE.toString() });
    }

    if(op == null) return RESTRetrieve.get(session);
    if(op.equals(QUERY)) return RESTQuery.get(session, input, bindings);
    if(op.equals(RUN)) return RESTRun.get(session, input, bindings);
    return RESTCommands.get(session, input, true);
  }
}
