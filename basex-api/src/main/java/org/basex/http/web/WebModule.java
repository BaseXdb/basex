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
import org.basex.query.ann.*;
import org.basex.query.func.*;
import org.basex.util.*;

/**
 * This class caches information on a single XQuery module with relevant annotations.
 *
 * @author BaseX Team 2005-21, BSD License
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
  private long time = -1;

  /**
   * Constructor.
   * @param file xquery file
   */
  WebModule(final IOFile file) {
    this.file = file;
  }

  /**
   * Checks the module for relevant annotations.
   * @param ctx database context
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  void parse(final Context ctx) throws QueryException, IOException {
    final long ts = file.timeStamp();
    if(time == ts) return;
    time = ts;

    functions.clear();
    wsFunctions.clear();

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
    } catch(final QueryException ex) {
      if(ctx.soptions.get(StaticOptions.RESTXQERRORS)) throw ex;
      // ignore modules that cannot be parsed
      Util.debug(ex);
    }
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
  ArrayList<WsFunction> wsFunctions() {
    return wsFunctions;
  }

  /**
   * Retrieves a query context for the given module.
   * @param ctx database context
   * @return query context
   * @throws QueryException query exception
   */
  QueryContext qc(final Context ctx) throws QueryException {
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
   * @param func function to be found
   * @param qc query context
   * @return function or {@code null}
   * @throws HTTPException HTTP exception
   */
  static StaticFunc get(final StaticFunc func, final QueryContext qc) throws HTTPException {
    for(final StaticFunc sf : qc.funcs.funcs()) {
      if(func.info.equals(sf.info)) {
        // inline arguments of called function
        sf.anns.addUnique(new Ann(sf.info, Annotation._BASEX_INLINE));
        return sf;
      }
    }
    // not to be expected; can only happen if file has been swapped between caching and parsing
    throw HTTPCode.SERVICE_NOT_FOUND.get();
  }
}
