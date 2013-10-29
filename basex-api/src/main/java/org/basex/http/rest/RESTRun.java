package org.basex.http.rest;

import static org.basex.core.Text.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.http.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * REST-based evaluation of XQuery files.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public class RESTRun extends RESTQuery {
  /** Path. */
  final String path;

  /**
   * Constructor.
   * @param rs REST session
   * @param vars external variables
   * @param val context value
   * @param pth path to query file
   */
  RESTRun(final RESTSession rs, final Map<String, String[]> vars, final String val,
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
   * @param input query input
   * @param vars external variables
   * @param val context value
   * @return command
   * @throws IOException I/O exception
   */
  static RESTRun get(final RESTSession rs, final String input, final Map<String, String[]> vars,
      final String val) throws IOException {

    // get root directory for files
    final IOFile root = new IOFile(rs.context.globalopts.get(GlobalOptions.WEBPATH));

    // check if file is not found, is a folder or points to parent folder
    final IOFile io = new IOFile(root, input);
    if(!io.exists() || io.isDir() || !io.path().startsWith(root.path()))
      HTTPCode.NOT_FOUND_X.thrw(Util.info(RES_NOT_FOUND_X, input));

    // perform query
    rs.add(new XQuery(io.string()));
    return new RESTRun(rs, vars, val, io.path());
  }
}
