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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class RESTCommand extends RESTCmd {
  /**
   * Constructor.
   * @param session REST session
   */
  private RESTCommand(final RESTSession session) {
    super(session);
  }

  @Override
  protected void run0() throws IOException {
    // set content type to text
    final HTTPConnection conn = session.conn;
    conn.sopts().set(SerializerOptions.METHOD, SerialMethod.TEXT);
    conn.initResponse();

    for(final Command cmd : session) run(cmd, conn.response.getOutputStream());
  }

  /**
   * Creates a new instance of this command.
   * @param session REST session
   * @param input string input
   * @return command
   * @throws BaseXException database exception
   */
  static RESTCommand get(final RESTSession session, final String input) throws BaseXException {
    try {
      session.add(CommandParser.get(input, session.conn.context).parseSingle());
      return new RESTCommand(session);
    } catch(final QueryException ex) {
      throw new BaseXException(ex);
    }
  }
}
