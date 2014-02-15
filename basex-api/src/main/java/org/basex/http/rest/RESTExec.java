package org.basex.http.rest;

import java.io.*;

import org.basex.core.*;
import org.basex.util.*;

/**
 * Executes a simple REST operation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class RESTExec extends RESTCmd {
  /**
   * Constructor.
   * @param rs REST session
   */
  RESTExec(final RESTSession rs) {
    super(rs);
  }

  @Override
  protected void run0() throws IOException {
    // execute command and return info of last command
    for(final Command c : cmds) run(c);
    session.http.res.getOutputStream().write(Token.token(info()));
  }
}
