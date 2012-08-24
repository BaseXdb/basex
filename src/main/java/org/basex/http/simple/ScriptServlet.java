package org.basex.http.simple;

import org.basex.core.cmd.*;
import org.basex.http.*;
import org.basex.io.*;

/**
 * This servlet receives and processes command scripts.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class ScriptServlet extends SimpleServlet {
  @Override
  protected void run(final HTTPContext http) throws Exception {
    // retrieve file reference
    final IOFile io = file(http);
    // run script
    session(http).execute(new Run(io.path()));
  }
}
