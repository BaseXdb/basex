package org.basex.http.restxq;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.http.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;

/**
 * This class caches information on a single XQuery module with RESTXQ annotations.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class RestXqModule {
  /** Supported methods. */
  private final ArrayList<RestXqFunction> functions = new ArrayList<>();
  /** File reference. */
  private final IOFile file;
  /** Library module flag. */
  private final boolean lib;
  /** Parsing timestamp. */
  private long time;

  /**
   * Constructor.
   * @param in xquery module
   * @param l library module flag
   */
  RestXqModule(final IOFile in, final boolean l) {
    file = in;
    time = in.timeStamp();
    lib = l;
  }

  /**
   * Checks the module for RESTXQ annotations.
   * @param http http context
   * @return {@code true} if module contains relevant annotations
   * @throws Exception exception (including unexpected ones)
   */
  boolean parse(final HTTPContext http) throws Exception {
    functions.clear();

    // loop through all functions
    final QueryContext qc = parseModule(http);
    try {
      // loop through all functions
      for(final StaticFunc uf : qc.funcs.funcs()) {
        // consider only functions that are defined in this module
        if(!file.name().equals(new IOFile(uf.info.path()).name())) continue;
        final RestXqFunction rxf = new RestXqFunction(uf, qc, this);
        if(rxf.parse()) functions.add(rxf);
      }
    } finally {
      qc.close();
    }
    return !functions.isEmpty();
  }

  /**
   * Checks if the timestamp is still up-to-date.
   * @return result of check
   */
  boolean uptodate() {
    return time == file.timeStamp();
  }

  /**
   * Updates the timestamp.
   */
  void touch() {
    time = file.timeStamp();
  }

  /**
   * Returns all functions.
   * @return functions
   */
  ArrayList<RestXqFunction> functions() {
    return functions;
  }

  /**
   * Processes the HTTP request.
   * @param http HTTP context
   * @param func function to be processed
   * @param error optional error reference
   * @throws Exception exception
   */
  void process(final HTTPContext http, final RestXqFunction func, final QueryException error)
      throws Exception {

    // create new XQuery instance
    final QueryContext qc = parseModule(http);
    try {
      // loop through all functions
      for(final StaticFunc uf : qc.funcs.funcs()) {
        // compare input info
        if(!func.function.info.equals(uf.info)) continue;
        final RestXqFunction rxf = new RestXqFunction(uf, qc, this);
        rxf.parse();
        new RestXqResponse(rxf, qc, http, error).create();
        break;
      }
    } finally {
      qc.close();
    }
  }

  // PRIVATE METHODS ====================================================================

  /**
   * Parses the module and returns the query context.
   * @param http http context
   * @return query context
   * @throws QueryException query exception
   */
  private QueryContext parseModule(final HTTPContext http) throws QueryException {
    final QueryContext qc = new QueryContext(http.context());
    try {
      qc.parse(string(file.read()), lib, file.path(), null);
      return qc;
    } catch(final IOException ex) {
      throw IOERR.get(null, ex);
    }
  }
}
