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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class RESTRun extends RESTQuery {
  /** Path. */
  private final String path;

  /**
   * Constructor.
   * @param rs REST session
   * @param vars external variables
   * @param val context value
   * @param pth path to query file
   */
  private RESTRun(final RESTSession rs, final Map<String, String[]> vars, final String val,
                  final String pth) {
    super(rs, vars, val);
    path = pth;
  }

  @Override
  protected void run0() throws IOException {
    query(path);
  }

  /**
   * Creates a new instance of this command.
   * @param rs REST session
   * @param path relative path to query input
   * @param vars external variables
   * @param val context value
   * @return command
   * @throws IOException I/O exception
   */
  static RESTQuery get(final RESTSession rs, final String path, final Map<String, String[]> vars,
      final String val) throws IOException {

    // get root directory for files
    final IOFile root = new IOFile(rs.context.soptions.get(StaticOptions.WEBPATH));

    // check if file is not found, is a folder or points to parent folder
    final IOFile file = new IOFile(root, path);
    if(!file.exists() || file.isDir() || !file.path().startsWith(root.path()))
      throw HTTPCode.NOT_FOUND_X.get(Util.info(RES_NOT_FOUND_X, path));

    // retrieve file contents
    final String input = file.string();
    // interpret as commands if input ends with command script suffix
    if(file.hasSuffix(IO.BXSSUFFIX)) {
      try {
        for(final Command cmd : new CommandParser(input, rs.context).parse()) rs.add(cmd);
      } catch(final QueryException ex) {
        throw new IOException(ex);
      }
    } else {
      // otherwise, interpret input as xquery
      rs.add(new XQuery(input));
    }

    // perform query
    return new RESTRun(rs, vars, val, file.path());
  }
}
