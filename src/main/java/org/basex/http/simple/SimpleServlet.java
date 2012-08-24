package org.basex.http.simple;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.io.*;
import org.basex.server.*;

/**
 * This servlet receives and processes simple HTTP requests.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
abstract class SimpleServlet extends BaseXServlet {
  /**
   * Returns the addressed file reference.
   * @param http http context
   * @return file
   * @throws HTTPException HTTP exception
   */
  protected IOFile file(final HTTPContext http) throws HTTPException {
    // get root directory for files
    final String path = http.context().mprop.get(MainProp.HTTPPATH);
    final String input = http.req.getRequestURI();

    // check if file is not found, is a folder or points to parent folder
    final IOFile root = new IOFile(path);
    final IOFile io = new IOFile(path, input);
    if(!io.exists() || io.isDir() || !io.path().startsWith(root.path()))
      HTTPErr.NOT_FOUND_X.thrw(RES_NOT_FOUND_X, input);
    return io;
  }

  /**
   * Creates a new session and redirects the output.
   * @param http http context
   * @return session
   * @throws IOException IO exception
   */
  protected LocalSession session(final HTTPContext http) throws IOException {
    final LocalSession session = http.session();
    session.setOutputStream(http.res.getOutputStream());
    return session;
  }
}
