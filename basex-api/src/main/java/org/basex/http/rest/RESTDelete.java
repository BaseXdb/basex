package org.basex.http.rest;

import java.io.*;

import org.basex.core.cmd.*;
import org.basex.http.*;

/**
 * REST-based evaluation of DELETE operations.
 *
 * @author BaseX Team 2005-13, BSD License
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
    if(http.depth() == 0) throw HTTPCode.NO_PATH.thrw();

    // open database to ensure it exists
    rs.add(new Open(http.db()));
    if(http.depth() == 1) rs.add(new DropDB(http.db()));
    else rs.add(new Delete(http.dbpath()));

    return new RESTExec(rs);
  }
}
