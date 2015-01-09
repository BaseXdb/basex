package org.basex.http.rest;

import java.io.*;

import org.basex.core.cmd.*;
import org.basex.http.*;

/**
 * REST-based evaluation of DELETE operations.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class RESTDelete {
  /** Private constructor. */
  private RESTDelete() { }

  /**
   * Creates a new instance of this command.
   * @param session REST session
   * @return command
   * @throws IOException I/O exception
   */
  static RESTExec get(final RESTSession session) throws IOException {
    RESTCmd.parseOptions(session);

    final HTTPContext http = session.http;
    final String db = http.db();
    if(db.isEmpty()) throw HTTPCode.NO_PATH.get();

    // open database to ensure it exists
    session.add(new Open(db));
    final String path = http.dbpath();
    if(path.isEmpty()) session.add(new DropDB(db));
    else session.add(new Delete(path));

    return new RESTExec(session);
  }
}
