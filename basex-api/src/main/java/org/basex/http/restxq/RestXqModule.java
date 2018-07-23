package org.basex.http.restxq;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import javax.servlet.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.http.ws.*;
import org.basex.http.ws.response.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.scope.*;

/**
 * This class caches information on a single XQuery module with RESTXQ annotations.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class RestXqModule {
  /** Supported methods. */
  private final ArrayList<RestXqFunction> functions = new ArrayList<>();
  /** Supported Websocket Methods. */
  private final ArrayList<WsFunction> wsFunctions = new ArrayList<>();
  /** File reference. */
  private final IOFile file;
  /** Parsing timestamp. */
  private long time;

  /**
   * Constructor.
   * @param file xquery file
   */
  RestXqModule(final IOFile file) {
    this.file = file;
    time = file.timeStamp();
  }

  /**
   * Checks the module for RESTXQ annotations.
   * @param ctx database context
   * @return {@code true} if module contains relevant annotations
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  boolean parse(final Context ctx) throws QueryException, IOException {
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
          if(wxq.parse()) wsFunctions.add(wxq);
        }
      }
    }
    // Check if seperate Method is here necessessary
    return !functions.isEmpty() || !wsFunctions.isEmpty();
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
   * Returns all WebsocketFunctions.
   * @return functions
   */
  ArrayList<WsFunction> wsFunctions() {
    return wsFunctions;
  }

  /**
   * Processes the HTTP request.
   * @param conn HTTP connection
   * @param func function to be processed
   * @param ext extended processing information (function, error; can be {@code null})
   * @return {@code true} if function creates no result
   * @throws QueryException query exception
   * @throws IOException I/O exception
   * @throws ServletException servlet exception
   */
  boolean process(final HTTPConnection conn, final RestXqFunction func, final Object ext)
      throws QueryException, IOException, ServletException {

    // create new XQuery instance
    final Context ctx = conn.context;
    try(QueryContext qc = qc(ctx)) {
      final StaticFunc sf = find(qc, func.function);
      // will only happen if file has been swapped between caching and parsing
      if(sf == null) throw HTTPCode.NO_XQUERY.get();

      final RestXqFunction rxf = new RestXqFunction(sf, qc, this);
      rxf.parse(ctx);
      return new RestXqResponse(rxf, qc, conn).create(ext);
    }
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Retrieves a query context for the given module.
   * @param ctx database context
   * @return query context
   * @throws QueryException query exception
   */
  private QueryContext qc(final Context ctx) throws QueryException {
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
   */
  private static StaticFunc find(final QueryContext qc, final StaticFunc func) {
    for(final StaticFunc sf : qc.funcs.funcs()) {
      if(func.info.equals(sf.info)) return sf;
    }
    return null;
  }


  /**
   * WebsocketStuff
   * */
  /**
   * Processes the HTTP request.
   * @param conn HTTP connection
   * @param func function to be processed
   * @param message String the Message which arrives in onText
   * @param response The WsSerializer
   * @param header The headers to set
   * @param id The id of the WebsocketClient
   * @return {@code true} if function creates no result
   * @throws Exception exception
   */
  public boolean process(final WsConnection conn, final WsFunction func,
      final Object message, final WsResponse response,
      final Map<String, String> header, final String id) throws Exception {
    final Context ctx = conn.context;
    try(QueryContext qc = qc(ctx)) {
      qc.putProperty(HTTPText.WS, id);
      final StaticFunc sf = find(qc, func.function);
      // will only happen if file has been swapped between caching and parsing
      if(sf == null) throw HTTPCode.NO_XQUERY.get();

      final WsFunction wxf = new WsFunction(sf, qc, this);
      wxf.parse();

      final Expr[] args = new Expr[sf.params.length];
      wxf.bind(args, qc, message, header);

      qc.mainModule(MainModule.get(sf, args));
      return response.create(conn, wxf, qc);
    }
  }
}
