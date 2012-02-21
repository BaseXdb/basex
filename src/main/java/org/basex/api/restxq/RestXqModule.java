package org.basex.api.restxq;

import org.basex.api.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.item.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.util.*;
import org.basex.util.list.*;


/**
 * This class caches information on a single XQuery module with RESTful annotations.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class RestXqModule {
  /** Supported methods. */
  final ObjList<RestXqFunction> functions = new ObjList<RestXqFunction>();
  /** File reference. */
  final IOFile file;
  /** Parsing timestamp. */
  long time;

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
   * @return {@code true} if module contains relevant annotations
   */
  boolean init() {
    functions.reset();
    try {
      // loop through all functions
      final QueryContext qc = parse();
      for(final UserFunc uf : qc.funcs.funcs()) {
        final RestXqFunction func = new RestXqFunction();
        if(func.init(uf)) functions.add(func);
      }
    } catch(final Exception ex) {
      // [CG] RestXq: decide how to handle exceptions while caching the modules
      Util.errln(ex.getMessage());
    }
    return !functions.empty();
  }

  /**
   * Checks if the file has been modified.
   * If yes, updates the timestamp.
   * @return result of check
   */
  boolean modified() {
    final long ts = file.timeStamp();
    if(time == ts) return false;
    time = ts;
    return true;
  }

  /**
   * Returns a function that was made to process the specified path and method.
   * @param http http context
   * @return instance
   */
  RestXqFunction find(final HTTPContext http) {
    for(final RestXqFunction f : functions) {
      if(f.supports(http)) return f;
    }
    return null;
  }

  /**
   * Processes the HTTP request.
   * @param http HTTP context
   * Parses new modules and discards obsolete ones.
   * @throws Exception exception ([CG] RestXq: limit to relevant exceptions)
   */
  void process(final HTTPContext http) throws Exception {
    // create new XQuery instance
    final QueryContext qc = parse();
    // loop through all functions
    for(final UserFunc uf : qc.funcs.funcs()) {
      // find and evaluate relevant function
      final RestXqFunction func = new RestXqFunction();
      if(func.init(uf) && func.supports(http)) {
        process(uf, qc, http);
        return;
      }
    }
  }

  // PRIVATE METHODS ====================================================================

  /**
   * Parses the module and returns the query context.
   * @return query context
   */
  private QueryContext parse() {
    try {
      final String query = Token.string(file.read());
      final Context ctx = HTTPSession.context();
      final QueryContext qc = new QueryContext(ctx);
      qc.module(query);
      return qc;
    } catch(final Exception ex) {
      // [CG] RestXq: decide how to handle exceptions while caching the modules
      Util.errln(ex.getMessage());
      return null;
    }
  }

  /**
   * Evaluates the specified function.
   * @param uf user-defined function
   * @param qc query context
   * @param http HTTP context
   * @throws Exception exception ([CG] RestXq: limit to relevant exceptions)
   */
  void process(final UserFunc uf, final QueryContext qc, final HTTPContext http)
      throws Exception {

    // wrap function with a function call
    final BaseFuncCall bfc = new BaseFuncCall(null, uf.name, uf.args);
    bfc.init(uf);

    // bind variables
    for(final Var arg : uf.args) {
      // [CG] RestXq: bind correct variables, throw exception for unbound variables
      arg.bind(Str.get("[variable]"), qc);
    }

    // compile function
    bfc.comp(qc);

    // evaluate function
    final ValueIter ir = bfc.value(qc).iter();

    // serialize result
    final Serializer ser = Serializer.get(http.out);
    for(Item it; (it = ir.next()) != null;) ser.item(it);
    ser.close();
  }
}
