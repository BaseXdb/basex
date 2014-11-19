package org.basex.http.rest;

import java.io.*;

import org.basex.core.cmd.*;
import org.basex.http.*;

/**
 * REST-based evaluation of DELETE operations.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class RESTDelete {
  /** Private constructor. */
  private RESTDelete() { }

  /**
   * Creates a new instance of this command.
   * @param rs REST session
   * @return command
   * @throws IOException I/O exception
   */
  static RESTExec get(final RESTSession rs) throws IOException {
    RESTCmd.parseOptions(rs);

    final HTTPContext http = rs.http;
    final String db = http.db();
    if(db.isEmpty()) throw HTTPCode.NO_PATH.get();

    // open database to ensure it exists
    rs.add(new Open(db));
    final String path = http.dbpath();
    if(path.isEmpty()) rs.add(new DropDB(db));
    else rs.add(new Delete(path));

    return new RESTExec(rs);
  }
}
