package org.basex.http.rest;

import static org.basex.core.Text.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.http.*;
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
  void run(final HTTPContext http) throws IOException {
    // get root directory for files
    final IOFile root = new IOFile(http.context().mprop.get(MainProp.WEBPATH));

    // check if file is not found, is a folder or points to parent folder
    final IOFile io = new IOFile(root, input);
    if(!io.exists() || io.isDir() || !io.path().startsWith(root.path()))
      HTTPErr.NOT_FOUND_X.thrw(Util.info(RES_NOT_FOUND_X, input));

    // perform query
    query(io.string(), http, io.path());
  }
}
