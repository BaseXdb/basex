package org.basex.http.rest;

import static org.basex.core.Text.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.parse.*;
import org.basex.http.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * REST-based evaluation of XQuery files.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class RESTRun extends RESTQuery {
  /**
   * Constructor.
   * @param session REST session
   * @param vars external variables
   * @param val context value
   */
  private RESTRun(final RESTSession session, final Map<String, String[]> vars, final String val) {
    super(session, vars, val);
  }

  /**
   * Creates a new instance of this command.
   * @param session REST session
   * @param path relative path to query input
   * @param vars external variables
   * @param val context value
   * @return command
   * @throws IOException I/O exception
   */
  static RESTQuery get(final RESTSession session, final String path,
      final Map<String, String[]> vars, final String val) throws IOException {

    // get root directory for files
    final Context context = session.conn.context;
    final String webpath = context.soptions.get(StaticOptions.WEBPATH);
    final String rpath = context.soptions.get(StaticOptions.RESTPATH);
    final IOFile root = new IOFile(webpath).resolve(rpath);

    // check if file is not found, is a folder or points to parent folder
    final IOFile file = new IOFile(root, path);
    if(!file.exists() || file.isDir() || !file.path().startsWith(root.path()))
      throw HTTPCode.NOT_FOUND_X.get(Util.info(RES_NOT_FOUND_X, path));

    // retrieve file contents
    final String input = file.string();
    // interpret as commands if input ends with command script suffix
    if(file.hasSuffix(IO.BXSSUFFIX)) {
      try {
        for(final Command cmd : CommandParser.get(input, context).parse()) {
          session.add(cmd.baseURI(file.path()));
        }
      } catch(final QueryException ex) {
        throw new IOException(ex);
      }
    } else {
      // otherwise, interpret input as xquery
      session.add(new XQuery(input));
    }

    // perform query
    return new RESTRun(session, vars, val);
  }
}
