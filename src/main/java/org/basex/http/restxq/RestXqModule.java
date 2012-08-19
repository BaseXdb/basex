package org.basex.http.restxq;

import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.http.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.util.*;

/**
 * This class caches information on a single XQuery module with RESTXQ annotations.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class RestXqModule {
  /** Supported methods. */
  private final ArrayList<RestXqFunction> functions = new ArrayList<RestXqFunction>();
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
   * Checks the module for RESTFful annotations.
   * @param http http context
   * @return {@code true} if module contains relevant annotations
   * @throws QueryException query exception
   */
  boolean analyze(final HTTPContext http) throws QueryException {
    functions.clear();

    // loop through all functions
    final QueryContext qc = parse(http);
    for(final UserFunc uf : qc.funcs.funcs()) {
      final RestXqFunction rxf = new RestXqFunction(uf, qc, this);
      if(rxf.analyze()) functions.add(rxf);
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
   * Adds functions that match the current request.
   * @param http http context
   * @param list list of functions
   */
  void add(final HTTPContext http, final ArrayList<RestXqFunction> list) {
    for(final RestXqFunction rxf : functions) {
      if(rxf.matches(http)) list.add(rxf);
    }
  }

  /**
   * Processes the HTTP request.
   * @param http HTTP context
   * @param func function to be processed
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  void process(final HTTPContext http, final RestXqFunction func)
      throws QueryException, IOException {

    // create new XQuery instance
    final QueryContext qc = parse(http);
    // loop through all functions
    for(final UserFunc uf : qc.funcs.funcs()) {
      // compare input info
      if(func.function.info.equals(uf.info)) {
        // find and evaluate relevant function
        final RestXqFunction rxf = new RestXqFunction(uf, qc, this);
        rxf.analyze();
        new RestXqResponse(rxf, qc, http).create();
        break;
      }
    }
  }

  // PRIVATE METHODS ====================================================================

  /**
   * Parses the module and returns the query context.
   * @param http http context
   * @return query context
   * @throws QueryException query exception
   */
  private QueryContext parse(final HTTPContext http) throws QueryException {
    try {
      final String query = string(file.read());
      final QueryContext qc = new QueryContext(http.context());
      qc.sc.baseURI(file.path());
      qc.module(query);
      return qc;
    } catch(final IOException ex) {
      // Unexpected: XQuery module could not be opened
      throw new RuntimeException(Util.message(ex));
    }
  }
}
