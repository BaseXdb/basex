package org.basex.api.rest;

import static javax.servlet.http.HttpServletResponse.*;
import static org.basex.api.rest.RESTText.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.basex.io.serial.SerializerProp;
import org.basex.util.TokenBuilder;

/**
 * This class processes GET requests sent to the REST server.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
final class RESTGet extends RESTCode {
  @Override
  void run(final RESTContext ctx) throws RESTException, IOException {
    final Map<String, String[]> vars = new HashMap<String, String[]>();

    // handle query parameters
    RESTCode code = null;
    final TokenBuilder ser = new TokenBuilder();
    final Map<?, ?> map = ctx.req.getParameterMap();
    final SerializerProp sp = new SerializerProp();
    for(final Entry<?, ?> s : map.entrySet()) {
      final String key = s.getKey().toString();
      final String[] vals = s.getValue() instanceof String[] ?
          (String[]) s.getValue() : new String[] { s.getValue().toString() };
      final String val = vals[0];

      if(key.equals(COMMAND) || key.equals(QUERY) || key.equals(RUN)) {
        if(code != null || vals.length > 1)
          throw new RESTException(SC_BAD_REQUEST, ERR_ONLYONE);
        code = key.equals(QUERY) ? new RESTQuery(val, vars) : key.equals(RUN) ?
            new RESTRun(val, vars) : new RESTCommand(val);
      } else if(key.startsWith("$")) {
        vars.put(key, new String[] { val });
      } else if(key.equals(WRAP)) {
        wrap(val, ctx);
      } else {
        // handle serialization parameters
        if(sp.get(key) != null) {
          for(final String v : vals) ser.add(key).add('=').add(v).add(',');
        } else {
          throw new RESTException(SC_BAD_REQUEST, ERR_PARAM + key);
        }
      }
    }
    ctx.serialization = ser.toString();
    (code == null ? new RESTList() : code).run(ctx);
  }
}
