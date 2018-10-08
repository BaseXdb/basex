package org.basex.http.web;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.http.restxq.*;
import org.basex.http.ws.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;

/**
 * This class caches information on a single XQuery module with relevant annotations.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class WebModule {
  /** Supported methods. */
  private final ArrayList<RestXqFunction> functions = new ArrayList<>();
  /** Supported WebSocket methods. */
  private final ArrayList<WsFunction> wsFunctions = new ArrayList<>();
  /** File reference. */
  private final IOFile file;
  /** Parsing timestamp. */
  private long time;

  /**
   * Constructor.
   * @param file xquery file
   */
  public WebModule(final IOFile file) {
    this.file = file;
    time = file.timeStamp();
  }

  /**
   * Checks the module for relevant annotations.
   * @param ctx database context
   * @return {@code true} if module contains relevant annotations
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  public boolean parse(final Context ctx) throws QueryException, IOException {
    functions.clear();
    wsFunctions.clear();

    // loop through all functions
    try(QueryContext qc = qc(ctx)) {
      // loop through all functions
      final String name = file.name();
      for(final StaticFunc sf : qc.funcs.funcs()) {
        // only add functions that are defined in the same module (file)
        if(sf.expr != null && name.equals(new IOFile(sf.info.path()).name())) {
          final RestXqFunction rxf = new RestXqFunction(sf, qc, this);
          if(rxf.parse(ctx)) functions.add(rxf);
          final WsFunction wxq = new WsFunction(sf, qc, this);
          if(wxq.parse(ctx)) wsFunctions.add(wxq);
        }
      }
    }
    return !(functions.isEmpty() && wsFunctions.isEmpty());
  }

  /**
   * Checks if the timestamp is still up-to-date.
   * @return result of check
   */
  public boolean uptodate() {
    return time == file.timeStamp();
  }

  /**
   * Updates the timestamp.
   */
  public void touch() {
    time = file.timeStamp();
  }

  /**
   * Returns all RESTXQ functions.
   * @return functions
   */
  public ArrayList<RestXqFunction> functions() {
    return functions;
  }

  /**
   * Returns all WebSocket functions.
   * @return functions
   */
  public ArrayList<WsFunction> wsFunctions() {
    return wsFunctions;
  }

  /**
   * Retrieves a query context for the given module.
   * @param ctx database context
   * @return query context
   * @throws QueryException query exception
   */
  public QueryContext qc(final Context ctx) throws QueryException {
    final QueryContext qc = new QueryContext(ctx);
    try {
      qc.parse(string(file.read()), file.path());
      return qc;
    } catch(final IOException ex) {
      // may be triggered when reading the file
      throw IOERR_X.get(null, ex);
    }
  }

  /**
   * Returns the specified function from the given query context.
   * @param qc query context.
   * @param func function to be found
   * @return function or {@code null}
   * @throws HTTPException HTTP exception
   */
  public static StaticFunc find(final QueryContext qc, final StaticFunc func) throws HTTPException {
    for(final StaticFunc sf : qc.funcs.funcs()) {
      if(func.info.equals(sf.info)) return sf;
    }
    // will only happen if file has been swapped between caching and parsing
    throw HTTPCode.NO_XQUERY.get();
  }
}
