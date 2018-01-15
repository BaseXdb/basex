package org.basex.http.rest;

import java.io.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.util.*;

/**
 * Executes a simple REST operation.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
final class RESTExec extends RESTCmd {
  /** Create flag. */
  private final boolean create;

  /**
   * Constructor.
   * @param session REST session
   * @param create create flag
   */
  RESTExec(final RESTSession session, final boolean create) {
    super(session);
    this.create = create;
  }

  @Override
  protected void run0() throws IOException {
    for(final Command cmd : session) run(cmd);
    session.conn.res.getOutputStream().write(Token.token(info()));
    if(create) code = HTTPCode.CREATED_X;
  }
}
