package org.basex.api.restxq;

import static javax.servlet.http.HttpServletResponse.*;
import static org.basex.api.restxq.RestXqText.*;

import java.io.*;
import java.util.*;

import org.basex.api.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.item.*;
import org.basex.query.item.SeqType.Occ;
import org.basex.query.iter.*;
import org.basex.query.path.*;
import org.basex.util.*;

/**
 * This class caches information on a single XQuery module with RESTful annotations.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class RestXqModule {
  /** Supported methods. */
  private final ArrayList<RestXqFunction> functions = new ArrayList<RestXqFunction>();
  /** File reference. */
  private final IOFile file;
  /** Parsing timestamp. */
  private long time;

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
   * @throws QueryException query exception
   */
  boolean analyze() throws QueryException {
    functions.clear();

    // loop through all functions
    final QueryContext qc = parse();
    for(final UserFunc uf : qc.funcs.funcs()) {
      final RestXqFunction rxf = new RestXqFunction(uf, qc);
      if(rxf.analyze()) functions.add(rxf);
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
   * Returns a function that was made to process the specified path and method.
   * @param http http context
   * @return instance
   */
  RestXqFunction find(final HTTPContext http) {
    for(final RestXqFunction rxf : functions) {
      if(rxf.matches(http)) return rxf;
    }
    return null;
  }

  /**
   * Processes the HTTP request.
   * @param http HTTP context
   * Parses new modules and discards obsolete ones.
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  void process(final HTTPContext http) throws QueryException, IOException {
    // create new XQuery instance
    final QueryContext qc = parse();
    // loop through all functions
    for(final UserFunc uf : qc.funcs.funcs()) {
      // recover and evaluate relevant function
      final RestXqFunction rxf = new RestXqFunction(uf, qc);
      if(rxf.analyze() && rxf.matches(http)) {
        process(rxf, qc, http);
        return;
      }
    }
  }

  // PRIVATE METHODS ====================================================================

  /**
   * Parses the module and returns the query context.
   * @return query context
   * @throws QueryException query exception
   */
  private QueryContext parse() throws QueryException {
    try {
      final String query = Token.string(file.read());
      final Context ctx = HTTPSession.context();
      final QueryContext qc = new QueryContext(ctx);
      qc.sc.baseURI(file.path());
      qc.module(query);
      return qc;
    } catch(final IOException ex) {
      // Unexpected: XQuery module could not be opened
      throw new RuntimeException(Util.message(ex));
    }
  }

  /**
   * Evaluates the specified function.
   * @param rxf function container
   * @param qc query context
   * @param http HTTP context
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private void process(final RestXqFunction rxf, final QueryContext qc,
      final HTTPContext http) throws QueryException, IOException {

    // wrap function with a function call
    final UserFunc uf = rxf.function;
    final BaseFuncCall bfc = new BaseFuncCall(null, uf.name, uf.args);
    bfc.init(uf);

    // bind variables
    rxf.bind(http);

    // compile and evaluate function
    final Value result = bfc.comp(qc).value(qc);

    // execute updates
    if(qc.updating()) qc.updates.apply();

    // [CG] RestXq: what happens if a method specified more methods than HEAD?
    if(rxf.methods.size() == 1 && rxf.methods.contains(HTTPMethod.HEAD)) {
      final QNm response = new QNm(RESPONSE, QueryText.REXQURI);
      final ExtKindTest dt = new ExtKindTest(NodeType.ELM, response, null, qc);
      final SeqType type = SeqType.get(NodeType.NOD, Occ.ONE, dt);
      if(!type.instance(result)) rxf.error(HEAD_METHOD);
    }

    // set serialization parameters
    http.initResponse(rxf.output);

    // serialize result
    final Serializer ser = Serializer.get(http.out, rxf.output);
    final ValueIter ir = result.iter();
    for(Item it; (it = ir.next()) != null;) it.serialize(ser);
    ser.close();

    // send OK status
    http.status(SC_OK, null);
  }
}
