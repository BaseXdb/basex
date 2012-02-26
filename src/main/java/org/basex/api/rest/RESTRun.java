package org.basex.api.rest;

import static javax.servlet.http.HttpServletResponse.*;
import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.api.*;
import org.basex.core.*;
import org.basex.core.cmd.Set;
import org.basex.io.*;
import org.basex.util.*;

/**
 * REST-based evaluation of XQuery files.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class RESTRun extends RESTQuery {
  /**
   * Constructor.
   * @param in input file to be executed
   * @param vars external variables
   * @param it context item
   */
  RESTRun(final String in, final Map<String, String[]> vars, final byte[] it) {
    super(in, vars, it);
  }

  @Override
  void run(final HTTPContext http) throws HTTPException, IOException {
    // get root directory for files
    final Context context = HTTPSession.context();
    final String path = context.mprop.get(MainProp.HTTPPATH);

    // check if file is not found, is a folder or points to parent folder...
    final IOFile root = new IOFile(path);
    final IOFile io = new IOFile(path, input);
    if(!io.exists() || io.isDir() || !io.path().startsWith(root.path()))
        throw new HTTPException(SC_NOT_FOUND, Util.info(FILE_NOT_FOUND_X, input));

    // set query path
    http.session.execute(new Set(Prop.QUERYPATH, io.path()));

    // perform query
    query(string(io.read()), http);
  }
}
