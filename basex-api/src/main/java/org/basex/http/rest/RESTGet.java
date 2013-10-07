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
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class RESTGet extends RESTCode {
  @Override
  void run(final HTTPContext http) throws IOException {
    final Map<String, String[]> vars = new HashMap<String, String[]>();

    // handle query parameters
    String operation = null;
    String input = null;
    byte[] item = null;

    // parse database options
    final TokenBuilder ser = new TokenBuilder();
    final SerializerOptions sp = new SerializerOptions();
    for(final Entry<String, String[]> param : http.params().entrySet()) {
      final String key = param.getKey();
      final String[] vals = param.getValue();
      final String val = vals[0];

      if(Token.eqic(key, COMMAND, QUERY, RUN)) {
        if(operation != null || vals.length > 1) HTTPErr.ONEOP.thrw();
        operation = key;
        input = val;
      } else if(key.equalsIgnoreCase(WRAP)) {
        // wrapping flag
        wrap(val, http);
      } else if(key.equalsIgnoreCase(CONTEXT)) {
        // context parameter
        item = Token.token(val);
      } else if(sp.option(key) != null) {
        // serialization parameters
        for(final String v : vals) ser.add(key).add('=').add(v).add(',');
      } else if(!parseOption(http, param, false)) {
        // external variables
        vars.put(key, new String[] { val });
      }
    }
    http.serialization = ser.toString();

    final RESTCode code;
    if(operation == null) {
      code = new RESTRetrieve(input, vars, item);
    } else if(operation.equals(QUERY)) {
      code = new RESTQuery(input, vars, item);
    } else if(operation.equals(RUN)) {
      code = new RESTRun(input, vars, item);
    } else {
      code = new RESTCommand(input);
    }
    code.run(http);
  }
}
