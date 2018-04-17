package org.basex.http.restxq;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.io.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.scope.*;
import org.basex.query.value.item.*;
import org.basex.ws.*;

/**
 * This class caches information on a single XQuery module with RESTXQ annotations.
 *
 * @author BaseX Team 2005-18, BSD License
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
   * @throws Exception exception (including unexpected ones)
   */
  boolean parse(final Context ctx) throws Exception {
    functions.clear();

    // loop through all functions
    try(QueryContext qc = qc(ctx)) {
      // loop through all functions
      final String name = file.name();
      for(final StaticFunc sf : qc.funcs.funcs()) {
        // only add functions that are defined in the same module (file)
        if(sf.expr != null && name.equals(new IOFile(sf.info.path()).name())) {
          final RestXqFunction rxf = new RestXqFunction(sf, qc, this);
          if(rxf.parse(ctx)) functions.add(rxf);
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
   * @param conn HTTP connection
   * @param func function to be processed
   * @param ext extended processing information (function, error; can be {@code null})
   * @return {@code true} if function creates no result
   * @throws Exception exception
   */
  boolean process(final HTTPConnection conn, final RestXqFunction func, final Object ext)
      throws Exception {

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
   * @throws Exception exception
   */
  private QueryContext qc(final Context ctx) throws Exception {
    final QueryContext qc = new QueryContext(ctx);
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
   * @param ext extended processing information (function, error; can be {@code null})
   * @return {@code true} if function creates no result
   * @throws Exception exception
   */
  boolean process(final WebsocketConnection conn, final RestXqFunction func, final Object ext)
      throws Exception {

    // create new XQuery instance
    final Context ctx = conn.context;
    try(QueryContext qc = qc(ctx)) {
      final StaticFunc sf = find(qc, func.function);
      // will only happen if file has been swapped between caching and parsing
      if(sf == null) throw HTTPCode.NO_XQUERY.get();

      final RestXqFunction rxf = new RestXqFunction(sf, qc, this);
      rxf.parse(ctx);

      qc.mainModule(MainModule.get(sf, new Expr[0]));
//      conn.sess.getRemote().sendBytes(data);
      Serializer ser = Serializer.get(null);
      Iter iter = qc.iter();
      for(Item it; (it = iter.next()) != null;) {
        conn.sess.getRemote().sendString(it.toString());;
//        ser.serialize(it);
      }
      return true;//new RestXqResponse(rxf, qc, conn).create(ext);
    }
  }
}
