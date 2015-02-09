package org.basex.http.restxq;

import static org.basex.http.restxq.RestXqText.*;
import static org.basex.util.Token.*;

import org.basex.http.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.http.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;

/**
 * This class creates a new HTTP response.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class RestXqResponse {
  /** Private constructor. */
  private RestXqResponse() { }

  /**
   * Evaluates the specified function and creates a response.
   * @param function function to be evaluated
   * @param query query context
   * @param http HTTP context
   * @param error optional query error
   * @throws Exception exception (including unexpected ones)
   */
  static void create(final RestXqFunction function, final QueryContext query,
      final HTTPContext http, final QueryException error) throws Exception {

    // bind variables
    final StaticFunc sf = function.function;
    final Expr[] args = new Expr[sf.args.length];
    function.bind(http, args, error);

    // wrap function with a function call
    final MainModule mm = new MainModule(sf, args);

    // assign main module and http context and register process
    query.mainModule(mm);
    query.http(http);
    query.context.register(query);

    String redirect = null, forward = null;
    RestXqRespBuilder resp = null;
    try {
      // evaluate query
      final Iter iter = query.iter();
      Item item = iter.next();

      // handle response element
      if(item instanceof ANode) {
        final ANode node = (ANode) item;
        // send redirect to browser
        if(REST_REDIRECT.eq(node)) {
          final ANode ch = node.children().next();
          if(ch == null || ch.type != NodeType.TXT) throw function.error(NO_VALUE, node.name());
          redirect = string(ch.string()).trim();
          return;
        }
        // server-side forwarding
        if(REST_FORWARD.eq(node)) {
          final ANode ch = node.children().next();
          if(ch == null || ch.type != NodeType.TXT) throw function.error(NO_VALUE, node.name());
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
      if(function.methods.size() == 1 && function.methods.contains(HTTPMethod.HEAD.name()))
        throw function.error(HEAD_METHOD);

      // serialize result
      final SerializerOptions sp = function.output;
      http.sopts(sp);
      http.initResponse();
      try(final Serializer ser = Serializer.get(http.res.getOutputStream(), sp)) {
        for(; item != null; item = iter.next()) ser.serialize(item);
      }

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
