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
  /** Timestamps of the module file and its imports, indexed by file path. */
  private final HashMap<String, Long> files = new HashMap<>();
  /** File reference. */
  private final IO file;
  /** Web archive the module is stored in (can be {@code null}). */
  private final WebArchive archive;

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
    // archived modules are parsed once (modified archives are reloaded as a whole)
    if(content != null && Checks.all(files.entrySet(),
        entry -> new IOFile(entry.getKey()).timeStamp() == entry.getValue())) return;

    // files modified from now on may have been read before the modification was complete
    final long start = System.currentTimeMillis();
    content = file.readString();
    files.clear();

    functions.clear();
    wsFunctions.clear();
    error = null;

    final QueryContext qc = new QueryContext(ctx);
    try(qc) {
      parse(qc);
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
    } finally {
      // record module and imported modules, even if parsing failed
      if(archive == null) {
        record(file, start);
        for(final byte[] path : qc.modParsed) {
          if(IO.get(Token.string(path)) instanceof final IOFile io) record(io, start);
        }
      }
    }
  }

  /**
   * Records the timestamp of a file, or an invalid timestamp if it was modified while parsing.
   * @param io file
   * @param start start time of parsing
   */
  private void record(final IO io, final long start) {
    final long ts = io.timeStamp();
    files.putIfAbsent(io.path(), ts < start ? ts : -1);
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
    parse(qc);
    return qc;
  }

  /**
   * Parses the module with the specified query context.
   * @param qc query context
   * @throws QueryException query exception
   */
  private void parse(final QueryContext qc) throws QueryException {
    final StaticContext sc = archive == null ? null :
      new StaticContext(qc).resolver((path, uri, base) -> archive.resolve(path, base));
    // modules of the web application may access external resources
    qc.parseResources = true;
    try {
      qc.parse(content, file.path(), sc);
    } finally {
      qc.parseResources = false;
    }
  }
}
