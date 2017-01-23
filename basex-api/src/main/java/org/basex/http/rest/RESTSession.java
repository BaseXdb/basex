package org.basex.http.rest;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.http.*;

/**
 * REST session.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
final class RESTSession {
  /** Commands to be executed. */
  final ArrayList<Command> commands = new ArrayList<>();
  /** HTTP connection. */
  final HTTPConnection conn;
  /** Client context. */
  final Context context;

  /**
   * Constructor, specifying login data and an output stream.
   * @param conn HTTP connection
   */
  RESTSession(final HTTPConnection conn) {
    this.conn = conn;
    context = conn.context;
  }

  /**
   * Adds a command to be executed.
   * @param cmd command
   * @return self reference
   */
  RESTSession add(final Command cmd) {
    commands.add(cmd);
    return this;
  }

  /**
   * Adds a command to be executed.
   * @param cmd command
   * @param is input stream
   * @return self reference
   */
  RESTSession add(final Command cmd, final InputStream is) {
    commands.add(cmd);
    cmd.setInput(is);
    return this;
  }
}
