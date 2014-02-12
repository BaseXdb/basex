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
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class RESTCommand extends RESTCmd {
  /**
   * Constructor.
   * @param rs REST session
   */
  private RESTCommand(final RESTSession rs) {
    super(rs);
  }

  @Override
  protected void run0() throws IOException {
    // set content type to text
    final HTTPContext http = session.http;
    http.serialization.set(SerializerOptions.METHOD, SerialMethod.TEXT);
    http.initResponse();

    for(final Command c : cmds) run(c, http.res.getOutputStream());
  }

  /**
   * Creates a new instance of this command.
   * @param rs REST session
   * @param input string input
   * @return command
   * @throws BaseXException database exception
   */
  static RESTCommand get(final RESTSession rs, final String input) throws BaseXException {
    try {
      open(rs);
      rs.add(new CommandParser(input, rs.context).parseSingle());
      return new RESTCommand(rs);
    } catch(final QueryException ex) {
      throw new BaseXException(ex);
    }
  }
}
