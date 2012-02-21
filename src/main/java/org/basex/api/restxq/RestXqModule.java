package org.basex.api.restxq;

import static javax.servlet.http.HttpServletResponse.*;

import java.io.*;

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
  // [CG] RestXq: how to resolve conflicting paths? what is "more specific"?

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
   * @throws HTTPException HTTP exception
   */
  boolean update() throws HTTPException {
    functions.reset();

    // loop through all functions
    final QueryContext qc = parse();
    for(final UserFunc uf : qc.funcs.funcs()) {
      final RestXqFunction func = new RestXqFunction(uf);
      if(func.update(file)) functions.add(func);
    }
    return !functions.empty();
  }

  /**
   * Checks if the file has been modified.
   * If yes, updates the timestamp.
   * @return result of check
   */
  boolean uptodate() {
    return time == file.timeStamp();
  }

  /**
   * Updates the timestamp.
   * If yes, updates the timestamp.
   */
  void touch() {
    time = file.timeStamp();
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
   * @throws HTTPException HTTP exception
   * @throws IOException I/O exception
   */
  void process(final HTTPContext http) throws HTTPException, IOException {
    // create new XQuery instance
    final QueryContext qc = parse();
    // loop through all functions
    for(final UserFunc uf : qc.funcs.funcs()) {
      // recover and evaluate relevant function
      final RestXqFunction rxf = new RestXqFunction(uf);
      if(rxf.update(file) && rxf.supports(http)) {
        process(rxf, qc, http);
        return;
      }
    }
  }

  // PRIVATE METHODS ====================================================================

  /**
   * Parses the module and returns the query context.
   * @return query context
   * @throws HTTPException HTTP exception
   */
  private QueryContext parse() throws HTTPException {
    try {
      final String query = Token.string(file.read());
      final Context ctx = HTTPSession.context();
      final QueryContext qc = new QueryContext(ctx);
      qc.sc.baseURI(file.path());
      qc.module(query);
      return qc;
    } catch(final QueryException ex) {
      throw new HTTPException(SC_NOT_IMPLEMENTED, ex.getMessage());
    } catch(final IOException ex) {
      throw new HTTPException(SC_NOT_IMPLEMENTED, ex.getMessage());
    }
  }

  /**
   * Evaluates the specified function.
   * @param rxf function container
   * @param qc query context
   * @param http HTTP context
   * @throws HTTPException HTTP exception
   * @throws IOException I/O exception
   */
  void process(final RestXqFunction rxf, final QueryContext qc, final HTTPContext http)
      throws HTTPException, IOException {

    try {
      // wrap function with a function call
      final UserFunc uf = rxf.funct;
      final BaseFuncCall bfc = new BaseFuncCall(null, uf.name, uf.args);
      bfc.init(uf);

      // bind variables
      for(final Var arg : uf.args) {
        // [CG] RestXq: bind correct variables, throw exception for unbound variables
        arg.bind(Str.get("[variable]"), qc);
      }

      // compile and evaluate function
      final ValueIter ir = bfc.comp(qc).value(qc).iter();

      // set serialization parameters
      final SerializerProp sp = qc.serParams(false);
      if(rxf.produces != null) sp.set(SerializerProp.S_MEDIA_TYPE, rxf.produces);
      http.initResponse(sp);

      // serialize result
      final Serializer ser = Serializer.get(http.out);
      for(Item it; (it = ir.next()) != null;) it.serialize(ser);
      ser.close();
    } catch(final QueryException ex) {
      throw new HTTPException(SC_NOT_IMPLEMENTED, ex.getMessage());
    }
  }
}
