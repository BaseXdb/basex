package org.basex.http.restxq;

import static org.basex.query.QueryError.*;
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
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class RestXqModule {
  /** Supported methods. */
  private final ArrayList<RestXqFunction> functions = new ArrayList<>();
  /** File reference. */
  private final IOFile file;
  /** Parsing timestamp. */
  private long time;

  /**
   * Constructor.
   * @param in xquery module
   */
  RestXqModule(final IOFile in) {
    file = in;
    time = in.timeStamp();
  }

  /**
   * Checks the module for RESTXQ annotations.
   * @return {@code true} if module contains relevant annotations
   * @throws Exception exception (including unexpected ones)
   */
  boolean parse() throws Exception {
    functions.clear();

    // loop through all functions
    try(final QueryContext qc = qc()) {
      // loop through all functions
      final String name = file.name();
      for(final StaticFunc uf : qc.funcs.funcs()) {
        // only add functions that are defined in the same module (file)
        if(name.equals(new IOFile(uf.info.path()).name())) {
          final RestXqFunction rxf = new RestXqFunction(uf, qc, this);
          if(rxf.parse()) functions.add(rxf);
        }
      }
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
    try(final QueryContext qc = qc()) {
      final RestXqFunction rxf = new RestXqFunction(find(qc, func.function), qc, this);
      rxf.parse();
      RestXqResponse.create(rxf, qc, http, error);
    }
  }

  // PRIVATE METHODS ====================================================================

  /**
   * Retrieves a query context for the given module.
   * @return query context
   * @throws QueryException query exception
   */
  private QueryContext qc() throws QueryException {
    final QueryContext qc = new QueryContext(HTTPContext.context());
    try {
      qc.parse(string(file.read()), file.path(), null);
      return qc;
    } catch(final IOException ex) {
      // may be triggered when reading the file
      throw IOERR_X.get(null, ex);
    }
  }

  /**
   * Returns the specified function from the given query context.
   * @param qctx query context.
   * @param func function to be found
   * @return function
   */
  private static StaticFunc find(final QueryContext qctx, final StaticFunc func) {
    for(final StaticFunc sf : qctx.funcs.funcs()) {
      if(func.info.equals(sf.info)) return sf;
    }
    return null;
  }
}
