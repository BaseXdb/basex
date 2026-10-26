package org.basex.http.web;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import org.basex.core.*;
import org.basex.http.restxq.*;
import org.basex.http.ws.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.scope.*;
import org.basex.query.util.hash.*;
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
  private volatile ArrayList<RestXqFunction> functions = new ArrayList<>();
  /** Supported WebSocket methods. */
  private volatile ArrayList<WsFunction> wsFunctions = new ArrayList<>();
  /** Real paths of the module file and its imports (replaced whenever modules are checked). */
  private volatile Map<String, String> realPaths = new ConcurrentHashMap<>();
  /** Option declarations of the module. */
  private volatile QNmMap<String> options = new QNmMap<>();
  /** Timestamps of the module file and its imports, indexed by file path. */
  private final HashMap<String, Long> files = new HashMap<>();
  /** File reference. */
  private final IO file;
  /** Web archive the module is stored in (can be {@code null}). */
  private final WebArchive archive;

  /** File content. */
  private String content;
  /** Error that occurred while parsing the module (can be {@code null}). */
  private volatile QueryException error;

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
    // detect changed symbolic links and capitalizations
    realPaths = new ConcurrentHashMap<>();
    // archived modules are parsed once (modified archives are reloaded as a whole)
    if(content != null && Checks.all(files.entrySet(),
        entry -> new IOFile(entry.getKey()).timeStamp() == entry.getValue())) return;

    // files modified from now on may have been read before the modification was complete
    final long start = System.currentTimeMillis();
    content = file.readString();
    files.clear();

    final QueryContext qc = new QueryContext(ctx);
    try(qc) {
      // new lists are assigned: requests may still iterate the old ones
      final ArrayList<RestXqFunction> funcs = new ArrayList<>();
      final ArrayList<WsFunction> wsFuncs = new ArrayList<>();
      final AModule module = parse(qc);
      for(final StaticFunc sf : module.funcs) {
        if(sf.expr != null) {
          final RestXqFunction rxf = new RestXqFunction(sf, this, qc, 0);
          if(rxf.parseAnnotations(null)) {
            funcs.add(rxf);
            // register an additional instance for each further path annotation
            for(int p = 1; p < rxf.paths(); p++) {
              final RestXqFunction func = new RestXqFunction(sf, this, qc, p);
              func.parseAnnotations(null);
              funcs.add(func);
            }
          }
          final WsFunction wxq = new WsFunction(sf, this, qc);
          if(wxq.parseAnnotations(null)) wsFuncs.add(wxq);
        }
      }
      functions = funcs;
      wsFunctions = wsFuncs;
      options = module.options;
      error = null;
    } catch(final QueryException ex) {
      // skip modules that cannot be parsed; all other modules stay available
      functions = new ArrayList<>();
      wsFunctions = new ArrayList<>();
      options = new QNmMap<>();
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
   * Returns the option declarations of the module.
   * @return options
   */
  public QNmMap<String> options() {
    return options;
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
   * @return parsed module
   * @throws QueryException query exception
   */
  private AModule parse(final QueryContext qc) throws QueryException {
    qc.resolver = archive;
    // modules of the web application may access external resources
    qc.parseResources = true;
    qc.realPaths = realPaths;
    try {
      return qc.parse(content, file.path());
    } finally {
      qc.parseResources = false;
    }
  }
}
