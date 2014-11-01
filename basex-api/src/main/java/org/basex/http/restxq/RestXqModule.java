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
      final String name = file.name();
      for(final StaticFunc uf : qc.funcs.funcs()) {
        // only add functions that are defined in the same module (file)
        if(name.equals(new IOFile(uf.info.path()).name())) {
          final RestXqFunction rxf = new RestXqFunction(uf, qc, this);
          if(rxf.parse()) functions.add(rxf);
        }
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
      final RestXqFunction rxf = new RestXqFunction(find(qc, func.function), qc, this);
      rxf.parse();
      new RestXqResponse().create(rxf, qc, http, error);
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
      qc.parse(string(file.read()), file.path(), null);
      return qc;
    } catch(final IOException ex) {
      // may be triggered when reading the file
      throw IOERR_X.get(null, ex);
    } finally {
      qc.close();
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
