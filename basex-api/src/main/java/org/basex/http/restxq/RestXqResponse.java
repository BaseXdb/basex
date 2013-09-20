package org.basex.http.restxq;

import static org.basex.http.restxq.RestXqText.*;
import static org.basex.util.Token.*;

import org.basex.http.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;

/**
 * This class creates a new HTTP response.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
final class RestXqResponse {
  /** Function to be evaluated. */
  private final RestXqFunction function;
  /** Query context. */
  private final QueryContext query;
  /** HTTP context. */
  private final HTTPContext http;
  /** Optional query error. */
  private final QueryException error;

  /**
   * Constructor.
   * @param rxf function to be evaluated
   * @param ctx query context
   * @param hc HTTP context
   * @param err optional query error
   */
  RestXqResponse(final RestXqFunction rxf, final QueryContext ctx, final HTTPContext hc,
      final QueryException err) {
    function = rxf;
    query = ctx;
    http = hc;
    error = err;
  }

  /**
   * Evaluates the specified function and creates a response.
   * @throws Exception exception (including unexpected ones)
   */
  void create() throws Exception {
    String redirect = null, forward = null;
    RestXqRespBuilder resp = null;

    // bind variables
    final StaticFunc uf = function.function;
    final Expr[] args = new Expr[uf.args.length];
    function.bind(http, args, error);

    // wrap function with a function call
    final StaticFuncCall sfc = new StaticFuncCall(uf.name, args, uf.sc, uf.info).init(uf);
    final MainModule mm = new MainModule(sfc, new VarScope(uf.sc), null, uf.sc);

    // assign main module and http context and register process
    query.mainModule(mm);
    query.context(http, null, null);

    query.context.register(query);
    try {
      // compile and evaluate query
      query.compile();
      final Iter iter = query.iter();
      Item item = iter.next();

      // handle response element
      if(item != null && item.type.isNode()) {
        final ANode node = (ANode) item;
        // send redirect to browser
        if(REST_REDIRECT.eq(node)) {
          final ANode ch = node.children().next();
          if(ch == null || ch.type != NodeType.TXT) function.error(NO_VALUE, node.name());
          redirect = string(ch.string()).trim();
          return;
        }
        // server-side forwarding
        if(REST_FORWARD.eq(node)) {
          final ANode ch = node.children().next();
          if(ch == null || ch.type != NodeType.TXT) function.error(NO_VALUE, node.name());
          forward = string(ch.string()).trim();
          return;
        }
        if(REST_RESPONSE.eq(node)) {
          resp = new RestXqRespBuilder();
          resp.build(node, function, iter, http);
          return;
        }
      }

      // HEAD method must return a single response element
      if(function.methods.size() == 1 && function.methods.contains(HTTPMethod.HEAD))
        function.error(HEAD_METHOD);

      // serialize result
      final SerializerOptions sp = function.output;
      http.serialization = sp;
      http.initResponse();
      final Serializer ser = Serializer.get(http.res.getOutputStream(), sp);
      for(; item != null; item = iter.next()) ser.serialize(item);
      ser.close();

    } finally {
      query.close();
      query.context.unregister(query);

      if(redirect != null) {
        http.res.sendRedirect(redirect);
      } else if(forward != null) {
        http.req.getRequestDispatcher(forward).forward(http.req, http.res);
      } else if(resp != null) {
        if(resp.status != 0) http.status(resp.status, resp.message, resp.error);
        http.res.getOutputStream().write(resp.cache.toArray());
      }
    }
  }
}
