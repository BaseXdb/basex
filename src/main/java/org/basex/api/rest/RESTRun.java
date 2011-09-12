package org.basex.api.rest;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.basex.api.HTTPSession;
import org.basex.core.MainProp;
import org.basex.io.IO;
import org.basex.util.Util;

/**
 * REST-based evaluation of XQuery files.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public class RESTRun extends RESTQuery {
  /**
   * Constructor.
   * @param in input file to be executed
   * @param vars external variables
   */
  RESTRun(final String in, final Map<String, String[]> vars) {
    super(in, vars);
  }

  @Override
  void run(final RESTContext ctx) throws RESTException, IOException {
    // get root directory for files
    final String path = HTTPSession.context().mprop.get(MainProp.HTTPPATH);
    final IO io = IO.get(path + '/' + input);

    // file not found...
    if(!io.exists()) throw new RESTException(HttpServletResponse.SC_NOT_FOUND,
        Util.info(FILEWHICH, input));

    // perform query
    query(string(io.read()), ctx);
  }
}
