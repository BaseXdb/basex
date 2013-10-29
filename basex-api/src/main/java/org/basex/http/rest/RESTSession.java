package org.basex.http.rest;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.http.*;

/**
 * REST session.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class RESTSession {
  /** HTTP context. */
  public final HTTPContext http;
  /** Commands to be executed. */
  public final ArrayList<Command> cmds = new ArrayList<Command>();
  /** Database context. */
  public final Context context;

  /**
   * Constructor, specifying login data and an output stream.
   * @param hc HTTP context
   * @param ctx context
   */
  public RESTSession(final HTTPContext hc, final Context ctx) {
    http = hc;
    context = ctx;
  }

  /**
   * Adds a command to be executed.
   * @param cmd command
   * @return self reference
   */
  public RESTSession add(final Command cmd) {
    cmds.add(cmd);
    return this;
  }

  /**
   * Adds a command to be executed.
   * @param cmd command
   * @param is input stream
   * @return self reference
   */
  public RESTSession add(final Command cmd, final InputStream is) {
    cmds.add(cmd);
    cmd.setInput(is);
    return this;
  }
}
