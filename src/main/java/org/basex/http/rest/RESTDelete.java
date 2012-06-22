package org.basex.http.rest;

import static org.basex.util.Token.*;

import java.io.*;

import org.basex.core.cmd.*;
import org.basex.http.*;
import org.basex.server.*;

/**
 * REST-based evaluation of DELETE operations.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class RESTDelete extends RESTCode {
  @Override
  void run(final HTTPContext http) throws HTTPException, IOException {
    // parse database options
    parseOptions(http);
    // open addressed database
    open(http);

    final LocalSession session = http.session();
    if(http.depth() == 0) {
      HTTPErr.NO_PATH.thrw();
    } else if(http.depth() == 1) {
      session.execute(new DropDB(http.db()));
    } else {
      session.execute(new Delete(http.dbpath()));
    }
    // return command info
    http.res.getOutputStream().write(token(session.info()));
  }
}
