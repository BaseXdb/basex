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

/**
 * This class caches information on a single XQuery module with relevant annotations.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class WebModule {
  /** Supported methods. */
  private final ArrayList<RestXqFunction> functions = new ArrayList<>();
  /** Supported WebSocket methods. */
  private final ArrayList<WsFunction> wsFunctions = new ArrayList<>();
  /** File reference. */
  private final IOFile file;

  /** Parsing timestamp, initially {{@code -1}. */
  private long time = -1;
  /** File content. */
  private String content;

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
    content = file.string();

    functions.clear();
    wsFunctions.clear();

    try(QueryContext qc = qc(ctx)) {
      // loop through all functions
      final String name = file.name();
      for(final StaticFunc sf : qc.functions.funcs()) {
        // only add functions that are defined in the same module (file)
        if(sf.expr != null && name.equals(new IOFile(sf.info.path()).name())) {
          final RestXqFunction rxf = new RestXqFunction(sf, this, qc);
          if(rxf.parseAnnotations(ctx)) functions.add(rxf);
          final WsFunction wxq = new WsFunction(sf, this, qc);
          if(wxq.parseAnnotations(ctx)) wsFunctions.add(wxq);
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
    qc.parse(content, file.path());
    return qc;
  }
}
