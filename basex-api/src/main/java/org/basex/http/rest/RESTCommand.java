package org.basex.http.rest;

import java.io.*;

import org.basex.http.*;
import org.basex.io.serial.*;
import org.basex.server.*;

/**
 * REST-based evaluation of database commands.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public class RESTCommand extends RESTCode {
  /** Query input. */
  protected final String input;

  /**
   * Constructor.
   * @param in command to be executed
   */
  RESTCommand(final String in) {
    input = in;
  }

  @Override
  void run(final HTTPContext http) throws IOException {
    // open addressed database
    open(http);
    // set default content type to text
    http.serialization.set(SerializerOptions.METHOD, SerialMethod.TEXT);
    http.initResponse();

    // perform command
    final LocalSession session = http.session();
    session.setOutputStream(http.res.getOutputStream());
    session.execute(input);
  }
}
