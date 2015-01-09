package org.basex.http.rest;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.http.*;

/**
 * REST session.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class RESTSession {
  /** HTTP context. */
  final HTTPContext http;
  /** Commands to be executed. */
  final ArrayList<Command> cmds = new ArrayList<>();
  /** Database context. */
  final Context context;

  /**
   * Constructor, specifying login data and an output stream.
   * @param http HTTP context
   * @param context context
   */
  RESTSession(final HTTPContext http, final Context context) {
    this.http = http;
    this.context = context;
  }

  /**
   * Adds a command to be executed.
   * @param cmd command
   * @return self reference
   */
  RESTSession add(final Command cmd) {
    cmds.add(cmd);
    return this;
  }

  /**
   * Adds a command to be executed.
   * @param cmd command
   * @param is input stream
   * @return self reference
   */
  RESTSession add(final Command cmd, final InputStream is) {
    cmds.add(cmd);
    cmd.setInput(is);
    return this;
  }
}
