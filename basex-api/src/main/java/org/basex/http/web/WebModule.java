package org.basex.http.web;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.http.restxq.*;
import org.basex.http.ws.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.util.*;
import org.basex.util.log.*;

/**
 * This class caches information on a single XQuery module with relevant annotations.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WebModule {
  /** Supported methods. */
  private final ArrayList<RestXqFunction> functions = new ArrayList<>();
  /** Supported WebSocket methods. */
  private final ArrayList<WsFunction> wsFunctions = new ArrayList<>();
  /** File reference. */
  private final IO file;
  /** Web archive the module is stored in (can be {@code null}). */
  private final WebArchive archive;

  /** Parsing timestamp, initially {{@code -1}. */
  private long time = -1;
  /** File content. */
  private String content;
  /** Error that occurred while parsing the module (can be {@code null}). */
  private QueryException error;

  /**
   * Constructor.
   * @param file xquery file
   * @param archive web archive (can be {@code null})
   */
  WebModule(final IO file, final WebArchive archive) {
    this.file = file;
    this.archive = archive;
  }

  /**
   * Checks the module for relevant annotations.
   * @param ctx database context
   * @throws IOException I/O exception
   */
  void parse(final Context ctx) throws IOException {
    final long ts = archive != null ? archive.time() : file.timeStamp();
    if(time == ts) return;

    time = ts;
    content = file.readString();

    functions.clear();
    wsFunctions.clear();
    error = null;

    try(QueryContext qc = qc(ctx)) {
      // loop through all functions
      final String name = file.name();
      for(final StaticFunc sf : qc.functions) {
        // only add functions that are defined in the same module (file)
        if(sf.expr != null && name.equals(new IOFile(sf.info.path()).name())) {
          final RestXqFunction rxf = new RestXqFunction(sf, this, qc, 0);
          if(rxf.parseAnnotations(null)) {
            functions.add(rxf);
            // register an additional instance for each further path annotation
            for(int p = 1; p < rxf.paths(); p++) {
              final RestXqFunction func = new RestXqFunction(sf, this, qc, p);
              func.parseAnnotations(null);
              functions.add(func);
            }
          }
          final WsFunction wxq = new WsFunction(sf, this, qc);
          if(wxq.parseAnnotations(null)) wsFunctions.add(wxq);
        }
      }
    } catch(final QueryException ex) {
      // skip modules that cannot be parsed; all other modules stay available
      functions.clear();
      wsFunctions.clear();
      error = ex;
      ctx.log.writeServer(LogType.ERROR, Util.message(ex));
    }
  }

  /**
   * Returns the error that occurred while parsing the module.
   * @return error, or {@code null} if the module was parsed successfully
   */
  QueryException error() {
    return error;
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
   * Parses the module and returns the query context.
   * @param ctx database context
   * @return query context
   * @throws QueryException query exception
   */
  public QueryContext qc(final Context ctx) throws QueryException {
    final QueryContext qc = new QueryContext(ctx);
    final StaticContext sc = archive == null ? null :
      new StaticContext(qc).resolver((path, uri, base) -> archive.resolve(path, base));
    qc.parse(content, file.path(), sc);
    return qc;
  }
}
