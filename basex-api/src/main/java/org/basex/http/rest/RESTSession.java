package org.basex.http.rest;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.http.*;

/**
 * REST session.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class RESTSession implements Iterable<Command> {
  /** HTTP connection. */
  final HTTPConnection conn;
  /** Commands to be executed. */
  private final ArrayList<Command> commands = new ArrayList<>();

  /**
   * Constructor, specifying login data and an output stream.
   * @param conn HTTP connection
   */
  RESTSession(final HTTPConnection conn) {
    this.conn = conn;
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

  /**
   * Clears the command list.
   */
  void clear() {
    commands.clear();
  }

  @Override
  public Iterator<Command> iterator() {
    return commands.iterator();
  }
}
