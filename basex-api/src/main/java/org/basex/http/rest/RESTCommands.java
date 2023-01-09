package org.basex.http.rest;

import java.io.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.http.*;
import org.basex.io.serial.*;
import org.basex.query.*;

/**
 * REST-based evaluation of database commands.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
final class RESTCommands extends RESTCmd {
  /**
   * Constructor.
   * @param session REST session
   */
  private RESTCommands(final RESTSession session) {
    super(session);
  }

  @Override
  protected void run0() throws IOException {
    // set content type to text
    final HTTPConnection conn = session.conn;
    conn.sopts().set(SerializerOptions.METHOD, SerialMethod.TEXT);
    conn.initResponse();

    final OutputStream os = conn.response.getOutputStream();
    for(final Command cmd : session) run(cmd, os);
  }

  /**
   * Creates a new instance of this command.
   * @param session REST session
   * @param input string input
   * @param single single command
   * @return command
   * @throws BaseXException database exception
   */
  static RESTCommands get(final RESTSession session, final String input, final boolean single)
      throws BaseXException {
    try {
      final CommandParser cp = CommandParser.get(input, session.conn.context);
      if(single) {
        session.add(cp.parseSingle());
      } else {
        for(final Command cmd : cp.parse()) session.add(cmd);
      }
      return new RESTCommands(session);
    } catch(final QueryException ex) {
      throw new BaseXException(ex);
    }
  }
}
